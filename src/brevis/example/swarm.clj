(ns brevis.example.swarm
  (:require [clojure.zip :as zip])
  (:use [brevis.graphics.basic-3D]
        [brevis.physics.space]
        [brevis.shape box sphere]
        [brevis.core]
        [cantor]))  

; Swarm simulations are models of flocking behavior in collections of organisms. 
;
; These algorithms were first explored computationally in:
;   Reynolds, Craig W. "Flocks, herds and schools: A distributed behavioral model." ACM SIGGRAPH Computer Graphics. Vol. 21. No. 4. ACM, 1987.

;; Todo:
; - spheres

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Globals

(def num-birds 5)

(def memory (atom 0.5))
(def avoidance (atom 0.2))
(def clustering (atom 0.1))
(def centering (atom 0.1))

(def max-acceleration 2)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Birds

(defn bird?
  "Is a thing a bird?"
  [thing]
  (= (:type thing) :bird))

(defn random-bird-position
  "Returns a random valid bird position."
  []
  (vec3 (- (rand 20) 10) 
        (+ 10 -0.5 (rand))
        (- (rand 20) 10)))

(defn make-bird
  "Make a new bird with the specified program. At the specified location."
  [position]  
  (move (make-real {:type :bird
              :color [1 0 0]
              :shape (create-box)})
        position))
  
(defn random-bird
  "Make a new random bird."
  []
  (make-bird (random-bird-position)))

(defn bound-acceleration
  "Keeps the acceleration within a reasonable range."
  [v]
  v
  #_(if (> (length v) max-acceleration)
    (mul (div v (length v)) max-acceleration)
    v))

(defn fly
  "Change the acceleration of a bird."
  [bird dt nbrs]
  (let [closest-bird (first nbrs)
        centroid (div (reduce add (map get-position nbrs)) 
                      (count nbrs))
        d-closest-bird (sub (get-position closest-bird) (get-position bird))
        d-centroid (sub centroid (get-position bird))
        d-center (sub (vec3 0 10 0) (get-position bird))
        new-acceleration (bound-acceleration
                           (add (mul (:acceleration bird) @memory)
                                (mul d-center @centering)
                                (mul d-closest-bird @avoidance)
                                (mul d-centroid @clustering)))]
    #_(println (:uid bird) new-acceleration)
    (assoc bird
           :acceleration new-acceleration)))

(defn update-bird
  "Update a bird based upon its flocking behavior and the physical kinematics."
  [bird dt objects]  
  (let [objects (filter bird? objects)
        nbrs (sort-by-proximity (get-position bird) objects)
        floor (some #(when (= (:type %) :floor) %) objects)]
    (doseq [el (:vertices (:shape bird))]
      (println el))
    (update-object-kinematics 
      (fly bird dt nbrs) dt)))

(add-update-handler :bird update-bird); This tells the simulator how to update these objects

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Collision handling
;;
;; Collision functions take [collider collidee] and return [collider collidee]
;;   Both can be modified; however, two independent collisions are actually computed [a b] and [b a].

(defn bump
  "Collision between two birds. This is called on [bird1 bird2] and [bird2 bird1] independently
so we only modify bird1."
  [bird1 bird2]
  [(assoc bird1 :color [(rand) (rand) (rand)])
   bird2])

(defn land
  "Collision between a bird and the floor."
  [bird floor]
  [(assoc bird
     :position (vec3 (.x (:position bird)) 0 (.z (:position bird)))
     :acceleration (vec3 0 0 0)
     :velocity (vec3 0 0 0))
   floor])

(add-collision-handler :bird :bird bump)
(add-collision-handler :bird :floor land)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (let [initial-obj (init-world)
        birds (repeatedly num-birds random-bird)]
    {:objects (concat initial-obj birds)
     :rotate-mode :none :translate-mode :none
     :dt 0.1
     :rot-x 0 :rot-y 0 :rot-z 0
     :shift-x 0 :shift-y -20 :shift-z -50}))

;; Start ze macheen
(start-gui initialize-simulation update-world)

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
; - nice UI behavior

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Globals

(def num-birds 25)
(def avoidance (atom 0.1))
(def clustering (atom 0.1))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Birds

(defn bird?
  "Is a thing a bird?"
  [thing]
  (= (:type thing) :bird))

(defn random-bird-position
  "Returns a random valid bird position."
  []
  (vec3 (- (rand 20) 10) 10 (- (rand 20) 10)))

(defn random-bird-velocity
  "Returns a random reasonable velocity."
  []
  (vec3 (- (rand 2) 1) (- (rand 2) 1) (- (rand 2) 1)))

(defn make-bird
  "Make a new bird with the specified program. At the specified location."
  [position]
  (-> {}
      (make-real)
      (make-box)
;      (make-sphere)
      (assoc :type :bird
             :color [1 0 0]
             :position position)
      (make-collision-shape)))
  
(defn random-bird
  "Make a new random bird."
  []
  (make-bird (random-bird-position)))

(defn bound-acceleration
  "Keeps the acceleration within a reasonable range."
  [v]
  (if (> (length v) 10)
    (div v 10)
    v))

(defn fly
  "Change the acceleration of a bird."
  [bird dt nbrs]
  (let [closest-bird (first nbrs)
        centroid (div (reduce add (map :position nbrs)) 
                      (count nbrs))
        d-closest-bird (sub (:position closest-bird) (:position bird))
        d-centroid (sub centroid (:position bird))]
    (assoc bird
           :acceleration (bound-acceleration
                           (add (:acceleration bird) 
                                (mul d-closest-bird @avoidance)
                                (mul d-centroid @clustering))))))  

(defn update-bird
  "Update a bird based upon its flocking behavior and the physical kinematics."
  [bird dt objects]  
  (let [objects (filter bird? objects)
        nbrs (sort-by-proximity (:position bird) objects)
        floor (some #(when (= (:type %) :floor) %) objects)]
    (doseq [el (:vertices (:shape bird))]
      (println el))
    (update-object-kinematics 
      (fly bird dt nbrs) dt)))

(add-update-handler :bird update-bird); This tells the simulator how to update these objects

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Collision handling
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
;; World updates

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []
  (let [birds (repeatedly 25 random-bird)
        floor (make-floor)]
    {:objects (conj birds floor)}))

(start-gui initialize-simulation update-world 0.1)

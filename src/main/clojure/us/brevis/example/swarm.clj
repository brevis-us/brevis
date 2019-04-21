(ns us.brevis.example.swarm
  (:gen-class)
  (:require [brevis-utils.parameters :as parameters]
            [us.brevis.physics.collision :as collision]
            [us.brevis.vector :as vector]
            [us.brevis.camera :as camera]
            [us.brevis.physics.utils :as physics]
            [us.brevis.utils :as utils]
            [us.brevis.shape.cone :as cone]
            [us.brevis.shape.sphere :as sphere]
            [us.brevis.core :as core]
            [clj-random.core :as random])

  (:import (graphics.scenery Light)
           (java.util.function Predicate)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Swarm
;;
;; ![](img/brevis_example_swarm.png?raw=true)
;;
;; Swarm simulations are models of flocking behavior in collections of organisms.   
;;
;; These algorithms were first explored computationally in:
;;
;;   Reynolds, Craig W. "Flocks, herds and schools: A distributed behavioral model." ACM SIGGRAPH Computer Graphics. Vol. 21. No. 4. ACM, 1987.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Globals

(def num-birds (atom 20))
;(def num-birds (atom 2000))

(def avoidance-distance (atom 25))
;(def boundary 1000)
(def centering (atom 0.11))
(def boundary 30)

(def speed 1)
(def max-acceleration 10)

(defn lrand-vec3
  "Return a random vec3."
  ([=]
   (lrand-vec3 1 1 1))
  ([x y z]
   (vector/vec3 (random/lrand x) (random/lrand y) (random/lrand z)))
  ([xmn xmx ymn ymx zmn zmx]
   (vector/vec3 (random/lrand xmn xmx) (random/lrand ymn ymx) (random/lrand zmn zmx))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Birds

(defn bird?
  "Is a thing a bird?"
  [thing]
  (= (utils/get-type thing) :bird))

(defn random-bird-position
  "Returns a random valid bird position."
  [] 
  (vector/vec3 (- (random/lrand boundary) (/ boundary 2))
               (- (random/lrand boundary) (/ boundary 2))
               (- (random/lrand boundary) (/ boundary 2))))

(defn make-bird
  "Make a new bird. At the specified location."
  [position]
  (let [new-acceleration (lrand-vec3 -1 1 -1 1 -1 1)]
    (physics/set-acceleration
      (physics/move (physics/make-real {:type :bird
                                        :color (vector/vec4 1 0 0 1)
                                        :initial-acc (vector/vec3 0.001 0 0);(vector/normalize (random-bird-position))
                                        ;:shape (sphere/create-sphere 10)})
                                        :shape (cone/create-cone 10.2 1.5)})
            position)
      new-acceleration)))

(defn random-bird
  "Make a new random bird."
  []
  (make-bird (random-bird-position)))    

(defn bound-acceleration
  "Keeps the acceleration within a reasonable range."
  [v]  
  (if (> (vector/length v) max-acceleration)
    (vector/mul (vector/div v (vector/length v)) max-acceleration)
    v))

(defn bound-velocity
  "Keeps the acceleration within a reasonable range."
  [v]  
  (if (> (vector/length v) speed)
    (vector/mul (vector/div v (vector/length v)) speed)
    v))

(defn periodic-boundary
  "Change a position according to periodic boundary conditions."
  [pos]
  (let [x (vector/x-val pos)
        y (vector/y-val pos)
        z (vector/z-val pos)]
    (vector/vec3 (cond (> x boundary) (- (mod x boundary) boundary)
                       (< x (- boundary)) (mod (- x) boundary)
                       :else x)
                 (cond (> y boundary) (- (mod y boundary) boundary)
                       (< y (- boundary)) (mod (- y) boundary)
                       :else y)
                 (cond (> z boundary) (- (mod z boundary) boundary)
                       (< z (- boundary)) (mod (- z) boundary)
                       :else z))))

(defn periodic-boundary2
  "Change a position according to periodic boundary conditions."
  [x y z]
  [(cond (> x boundary) (- (mod x boundary) boundary)
         (< x (- boundary)) (mod (- x) boundary)
         :else x)
   (cond (> y boundary) (- (mod y boundary) boundary)
         (< y (- boundary)) (mod (- y) boundary)
         :else y)
   (cond (> z boundary) (- (mod z boundary) boundary)
         (< z (- boundary)) (mod (- z) boundary)
         :else z)])

(defn fly
  "Change the acceleration of a bird."
  [bird]
  (let [;nbrs (filter bird? (get-neighbor-objects bird))      
        ;tmp (println (count nbrs))
        ;tmp (do (doseq [nbr nbrs] (print (get-position nbr))) (println)) 
        bird-pos (physics/get-position bird)
        
        ;; Actual closest bird, a little slow
        ;bird-dists (map #(length-vec3 (sub-vec3 (get-position %) bird-pos)) nbrs)
        #_closest-bird #_(when-not (empty? nbrs)
                          (nth nbrs 
                               (reduce #(if (< (nth bird-dists %1) (nth bird-dists %2)) %1 %2) (range (count bird-dists)))))
        
        ;closest-bird (first nbrs)
        
        closest-bird (physics/get-closest-neighbor bird)
        
        new-acceleration (if-not closest-bird
                           ;; No neighbor, move randomly
                           (vector/elmul (vector/vec3 (- (random/lrand) 0.5) (- (random/lrand) 0.5) (- (random/lrand) 0.5))
                                  (vector/mul bird-pos -1.0))
                           (let [dvec (vector/sub bird-pos (physics/get-position closest-bird))
                                 len (vector/length dvec)]
                             (vector/add (vector/sub (physics/get-velocity closest-bird) (physics/get-velocity bird)); velocity matching
                                  (if (<= len @avoidance-distance)
                                    ;; If far from neighbor, get closer
                                    dvec
                                    ;; If too close to neighbor, move away
                                    (vector/add (vector/mul dvec -1.0)
                                         (vector/vec3 (random/lrand 0.1) (random/lrand 0.1) (random/lrand 0.1)))))));; add a small random delta so we don't get into a loop
        new-acceleration (vector/add-vec3 new-acceleration
                                          (vector/mul (vector/mul-vec3 (vector/normalize-vec3 bird-pos) -1)
                                                      @centering))]

        ;new-acceleration (if (zero? (vector/length new-acceleration))
        ;                   new-acceleration
        ;                   (vector/mul new-acceleration (/ 1 (vector/length new-acceleration))))]
    (physics/set-velocity
      (physics/set-acceleration
        (if (or (> (Math/abs (vector/x-val bird-pos)) boundary)
                (> (Math/abs (vector/y-val bird-pos)) boundary)
                (> (Math/abs (vector/z-val bird-pos)) boundary))
          (physics/move bird (periodic-boundary bird-pos) #_(vec3 0 25 0))
          bird)
        (bound-acceleration
          ;(physics/get-acceleration bird)
          new-acceleration
          #_(add (mul (get-acceleration bird) 0.5)
               (mul new-acceleration speed))))
      (bound-velocity (physics/get-velocity bird)))))


;(add-global-update-handler 10 (fn [] (println (get-time) (System/nanoTime))))

(physics/enable-kinematics-update :bird); This tells the simulator to move our objects
(utils/add-update-handler :bird fly); This tells the simulator how to update these objects
;(add-parallel-update-handler :bird fly); This tells the simulator how to update these objects (in parallel)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Collision handling
;;
;; Collision functions take [collider collidee] and return [collider collidee]
;; This is only called once per pair of colliding objects.

(defn bump
  "Collision between two birds."
  [bird1 bird2]  
  [(physics/set-color bird1 (vector/vec4 (random/lrand) (random/lrand) (random/lrand) 1))
   bird2])

(defn land
  "Collision between a bird and the floor."
  [bird floor]
  [(physics/set-velocity (physics/set-acceleration bird (vector/vec3 0 10.5 0)) (vector/vec3 0 10.0 0));; maybe move as well
   floor])

(collision/add-collision-handler :bird :bird bump)
(collision/add-collision-handler :bird :floor land)

(defn light-predicate[]
  (reify
    Predicate
    (test [this arg]
      (instance? Light arg))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (parameters/set-param :gui false)

  (physics/init-world)
  
  #_(set-camera-information (vec3 -10.0 -50.0 -200.0) (vec4 1.0 0.0 0.0 0.0))
  ;(camera/set-camera-information (vector/vec3 -10.0 57.939613 -890.0) (vector/vec4 1.0 0.0 0.0 0.0))

  (utils/set-dt 0.05)
  (physics/set-neighborhood-radius 50)
  (dotimes [_ @num-birds]
    (utils/add-object (random-bird)))
  (Thread/sleep 100)
  (.setVisible (.getFloor (fun.imagej.sciview/get-sciview)) false)
  (.surroundLighting (fun.imagej.sciview/get-sciview))
  (.centerOnScene(fun.imagej.sciview/get-sciview)))



;; Start zee macheen
(defn -main [& args]
  #_(start-nogui initialize-simulation)
  (if-not (empty? args)
    (core/start-nogui initialize-simulation)
    (core/start-gui initialize-simulation)))

(core/autostart-in-repl -main)

(-main)

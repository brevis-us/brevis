(ns us.brevis.example.single-bird
  (:gen-class)
  (:require [brevis-utils.parameters :as parameters]
            [us.brevis.vector :as vector]
            [us.brevis.physics.utils :as physics]
            [us.brevis.utils :as utils]
            [us.brevis.shape.cone :as cone]
            [us.brevis.core :as core]
            [clj-random.core :as random]))

(def this-bird (atom nil))

(defn lrand-vec3
  "Return a random vec3."
  ([=]
   (lrand-vec3 1 1 1))
  ([x y z]
   (vector/vec3 (random/lrand x) (random/lrand y) (random/lrand z)))
  ([xmn xmx ymn ymx zmn zmx]
   (vector/vec3 (random/lrand xmn xmx) (random/lrand ymn ymx) (random/lrand zmn zmx))))

(defn random-acceleration
  "Return a random acceleration vec3"
  []
  (lrand-vec3 -0.001 0.001 -0.001 0.001 -0.001 0.001))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Birds

(defn make-bird
  "Make a new bird. At the specified location."
  [position]
  (physics/move (physics/make-real {:type :bird
                                    :color (vector/vec4 1 0 0 1)
                                    :shape (cone/create-cone 10.2 1.5)})
        position))

(defn fly
  "Change the acceleration of a bird."
  [bird]
  (let [bird-pos (physics/get-position bird)
        dist (vector/length-vec3 bird-pos)]
    (if (> dist 100)
      (physics/set-velocity (physics/move bird (vector/vec3 0 0 0))
                            (random-acceleration))
      bird)))

        ;new-acceleration (if (zero? (vector/length new-acceleration))
        ;                   new-acceleration
        ;                   (vector/mul new-acceleration (/ 1 (vector/length new-acceleration))))]
    ;(physics/orient-object (physics/set-velocity
    ;                         (physics/set-acceleration
    ;                           (if (or (> (Math/abs (vector/x-val bird-pos)) boundary)
    ;                                   (> (Math/abs (vector/y-val bird-pos)) boundary)
    ;                                   (> (Math/abs (vector/z-val bird-pos)) boundary))
    ;                             (physics/move bird (periodic-boundary bird-pos) #_(vec3 0 25 0))
    ;                             bird)
    ;                           (bound-acceleration
    ;                             ;(physics/get-acceleration bird)
    ;                             new-acceleration
    ;                             #_(add (mul (get-acceleration bird) 0.5)
    ;                                  (mul new-acceleration speed))))
    ;                         (bound-velocity (physics/get-velocity bird)))
    ;                       (vector/vec3 0 0 1); This is the up-vector of the shape
    ;                       (physics/get-velocity bird))))


;(add-global-update-handler 10 (fn [] (println (get-time) (System/nanoTime))))

(physics/enable-kinematics-update :bird); This tells the simulator to move our objects
(utils/add-update-handler :bird fly); This tells the simulator how to update these objects

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (parameters/set-param :gui false)

  (physics/init-world)
  
  #_(set-camera-information (vec3 -10.0 -50.0 -200.0) (vec4 1.0 0.0 0.0 0.0))
  ;(camera/set-camera-information (vector/vec3 -10.0 57.939613 -890.0) (vector/vec4 1.0 0.0 0.0 0.0))

  (utils/set-dt 0.005)
  (physics/set-neighborhood-radius 50)

  (reset! this-bird (utils/add-object (make-bird (vector/vec3 0 0 0))))

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

;(-main)

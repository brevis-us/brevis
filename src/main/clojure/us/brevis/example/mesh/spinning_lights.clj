(ns us.brevis.example.mesh.spinning-lights
  (:gen-class)
  (:use [us.brevis.graphics.basic-3D]
        [us.brevis.physics collision core space utils]
        [us.brevis.shape box mesh sphere]
        [us.brevis core osd vector camera utils random]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Mesh demo
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Globals

(def mesh-file "bunny.obj")
(def light-radius 50)
(def num-lights 4)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Make our mesh object

(defn make-real-mesh
  "Make a new bird with the specified program. At the specified location."
  [position]  
  (move (make-real {:type :real-mesh
                    :color (vec4 1 1 1 1)
                    :shape (create-mesh mesh-file true (vec3 10 10 10))})
        position))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Light objects

(defn make-light-sphere
  "Make a new light sphere. At the specified location."
  [position light-id]  
  (add-light)
  (let [color (lrand-vec3 0 1 0 1 0 1)
        color-vec4 (vec3-to-vec4 color)]
    (light-specular light-id color-vec4)
    (light-diffuse light-id (mul-vec4 color-vec4 (/ num-lights)))
    (light-ambient light-id (mul-vec4 color-vec4 (/ num-lights)))
    (assoc (move (make-real {:type :light-sphere
                             :color color-vec4
                             :shape (create-sphere 5)})
                 position)
           :light-id light-id)))

(defn fly
  "Change the acceleration of a light sphere."
  [light-sphere]
  (let [light-pos (get-position light-sphere)
        delta-position (let [theta (* 2 java.lang.Math/PI (lrand))
                             psi (* 2 java.lang.Math/PI (lrand))]
                         (vec3 (* light-radius (java.lang.Math/sin theta) (java.lang.Math/cos psi))
                               (* light-radius (java.lang.Math/sin theta) (java.lang.Math/sin psi))
                               (* light-radius (java.lang.Math/cos theta))))
        new-position (add-vec3 light-pos (mul-vec3 delta-position 0.01))]
    (move-light (:light-id light-sphere) (vec4 (x-val-vec3 new-position) (y-val-vec3 new-position) (z-val-vec3 new-position) 0))
    (move light-sphere new-position)))


(enable-kinematics-update :light-sphere); This tells the simulator to move our objects
(add-update-handler :light-sphere fly); This tells the simulator how to update these objects

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (init-world)
  (init-view)  
  
  (set-camera-information (vec3 0.0 -27.036232 -30.0) (vec4 1.0 0.0 0.0 0.0))
  (disable-collisions)
  
  (set-dt 1)
  (set-neighborhood-radius 250)
  (default-display-text)
  #_(add-object (move (make-floor 500 500) (vec3 0 -10 0)))
  (add-object (make-real-mesh (vec3 0 25 0)))
  
  (dotimes [k num-lights]
    (add-object (make-light-sphere (lrand-vec3 (- light-radius) light-radius (- light-radius) light-radius (- light-radius) light-radius) k))))

;; Start zee macheen
(defn -main [& args]
  #_(start-nogui initialize-simulation)
  (if-not (empty? args)
    (start-nogui initialize-simulation)
    (start-gui initialize-simulation)))

(autostart-in-repl -main)


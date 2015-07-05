(ns brevis.example.mesh.mesh
  (:gen-class)
  (:use [brevis.graphics.basic-3D]
        [brevis.physics collision core space utils]
        [brevis.shape box mesh]
        [brevis core osd vector camera utils random]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Mesh demo
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Globals

(def mesh-file "bunny.obj")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Make our mesh object

(defn make-real-mesh
  "Make a new bird with the specified program. At the specified location."
  [position]  
  (move (make-real {:type :real-mesh
                    :color (vec4 1 0 0 1)
                    :shape (create-mesh mesh-file true (vec3 10 10 10))})
        position))

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
  (add-object (make-real-mesh (vec3 0 25 0))))

;; Start zee macheen
(defn -main [& args]
  #_(start-nogui initialize-simulation)
  (if-not (empty? args)
    (start-nogui initialize-simulation)
    (start-gui initialize-simulation)))

(autostart-in-repl -main)


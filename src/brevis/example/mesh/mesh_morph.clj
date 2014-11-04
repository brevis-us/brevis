(ns brevis.example.mesh.mesh-morph
  (:gen-class)
  (:use [brevis.graphics.basic-3D]
        [brevis.physics collision core space utils]
        [brevis.shape box mesh core]
        [brevis core osd vector camera utils display]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Mesh demo
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Globals

(def mesh-file "bunny.obj")
(def smoosh-factor 0.95)
(def mesh-obj-uuid (atom nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Make our mesh object

(defn make-real-mesh
  "Make a new bird with the specified program. At the specified location."
  [position]  
  (move (make-real {:type :real-mesh
                    :color (vec4 1 0 0 1)
                    :shape (create-mesh mesh-file true (vec3 10 10 10))})
        position))

(defn smoosh-mesh
  "Global updater for smooshing the mesh."
  []
  (when @mesh-obj-uuid
    (let [shape (get-shape (get-object @mesh-obj-uuid))
          mesh (get-mesh shape)]
      (dotimes [i (num-vertices mesh)]
        (let [vert (get-vertex mesh i)]
          #_(println vert)
          (when (pos? (aget vert 0))
            (aset vert 0 (float (* (aget vert 0) smoosh-factor)))
            (set-vertex mesh i vert))))
      (regen-mesh mesh))))

(add-global-update-handler 90 smoosh-mesh)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (init-world)
  (init-view)  
  
  #_(set-camera-information (vec3 -10.0 57.939613 -890.0) (vec4 1.0 0.0 0.0 0.0))
  (set-camera-information (vec3 0.0 -27.036232 -30.0) (vec4 1.0 0.0 0.0 0.0))
  (disable-collisions)
  
  (set-dt 1)
  (set-neighborhood-radius 250)
  (default-display-text)
  #_(add-object (move (make-floor 500 500) (vec3 0 -10 0)))
  (let [obj (make-real-mesh (vec3 0 25 0))]
    (reset! mesh-obj-uuid (get-uid obj))
    (add-object obj)))

;; Start zee macheen
(defn -main [& args]
  #_(start-nogui initialize-simulation)
  (if-not (empty? args)
    (start-nogui initialize-simulation)
    (start-gui initialize-simulation)))

(autostart-in-repl -main)


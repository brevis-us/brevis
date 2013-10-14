(ns brevis.example.tutorial.mesh
  (:use [brevis.graphics.basic-3D]
        [brevis.physics collision core space utils]
        [brevis.shape box sphere cone mesh]
        [brevis core osd vector]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Mesh

(def obj-filename "obj/bunny.obj")

(defn make-bunny
  "Make a new bunny at the specified position."
  [position]  
  (move (make-real {:type :bunny
                    :color (vec4 1 0 0 1)
                    ;:shape (create-sphere)})
                    :shape (create-mesh obj-filename true)})
        position))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (init-world)
  (init-view)
  (set-dt 0.1)
  (set-neighborhood-radius 25)
  (default-display-text)
  #_(disable-collisions)
  (add-object (make-floor 500 500))  
  (add-object (make-bunny (vec3 0 40 0))))

;; Start zee macheen
(defn -main [& args]
  (if-not (empty? args)
    (start-nogui initialize-simulation)
    (start-gui initialize-simulation)))

;; For autostart with Counterclockwise in Eclipse
(when (find-ns 'ccw.complete)
  (-main))

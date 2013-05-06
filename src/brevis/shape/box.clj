(ns brevis.shape.box
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [brevis.shape.core]
        [cantor]))        

(defn create-box
  "Create a box object."
  ([]
     (create-box 1 1 1))
  ([width height depth]
     {:dim (vec3 width height depth)
      :type :box
      }))

(defn make-box
  "Add box attributes to an object map."
  [obj]
  (assoc obj
    :shape (create-box)))

(defn create-collision-box 
  "Create a collision shape for a box."
  [pos shape]
  (box3 (sub pos (div (:dim shape) 2))
	(add pos (div (:dim shape) 2))))

(defn init-box-graphic
  []
  (def box-graphic
    (define-display-list :quads
      [[(vec3 -0.5 -0.5 0.5) (vec3 -0.5 0.5 0.5) (vec3 -0.5 0.5 -0.5) (vec3 -0.5 -0.5 -0.5)]
       [(vec3 0.5 -0.5 0.5) (vec3 0.5 0.5 0.5) (vec3 0.5 0.5 -0.5) (vec3 0.5 -0.5 -0.5)]
       [(vec3 0.5 -0.5 -0.5) (vec3 0.5 -0.5 0.5) (vec3 -0.5 -0.5 0.5) (vec3 -0.5 -0.5 -0.5)]
       [(vec3 0.5 0.5 -0.5) (vec3 0.5 0.5 0.5) (vec3 -0.5 0.5 0.5) (vec3 -0.5 0.5 -0.5)]
       [(vec3 -0.5 0.5 -0.5) (vec3 0.5 0.5 -0.5) (vec3 0.5 -0.5 -0.5) (vec3 -0.5 -0.5 -0.5)]
       [(vec3 -0.5 0.5 0.5) (vec3 0.5 0.5 0.5) (vec3 0.5 -0.5 0.5) (vec3 -0.5 -0.5 0.5)]])))

(defn draw-textured-cube []
  (dotimes [_ 4]
    (rotate 90 0 1 0)
    (textured-quad))
  (rotate 90 1 0 0)
  (textured-quad)
  (rotate 180 1 0 0)
  (textured-quad))

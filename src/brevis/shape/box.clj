(ns brevis.shape.box
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
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
    (create-display-list
     (draw-quads
      (vertex -0.5 -0.5 0.5) (vertex -0.5 0.5 0.5) (vertex -0.5 0.5 -0.5) (vertex -0.5 -0.5 -0.5)
      (vertex 0.5 -0.5 0.5) (vertex 0.5 0.5 0.5) (vertex 0.5 0.5 -0.5) (vertex 0.5 -0.5 -0.5)
      (vertex 0.5 -0.5 -0.5) (vertex 0.5 -0.5 0.5) (vertex -0.5 -0.5 0.5) (vertex -0.5 -0.5 -0.5)
      (vertex 0.5 0.5 -0.5) (vertex 0.5 0.5 0.5) (vertex -0.5 0.5 0.5) (vertex -0.5 0.5 -0.5)
      (vertex -0.5 0.5 -0.5) (vertex 0.5 0.5 -0.5) (vertex 0.5 -0.5 -0.5) (vertex -0.5 -0.5 -0.5)
      (vertex -0.5 0.5 0.5) (vertex 0.5 0.5 0.5) (vertex 0.5 -0.5 0.5) (vertex -0.5 -0.5 0.5)))))

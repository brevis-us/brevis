(ns brevis.shape.cylinder
  (:import [brevis BrShape])
  (:use [brevis vector]
        [brevis.shape.core])) 

(defn create-cylinder
  "Create a cone object."
  ([]
     (create-cylinder 1 1))
  ([length radius]
    (BrShape/createCylinder length radius (:gui @brevis.globals/*gui-state*))))

#_(defn gen-cone-faces
  [length lod]
  (let [interval (/ 360 lod)
        base (range 0 361 interval)]
    (for [theta (rest base)]
      (let [p1 (Math/sin (- theta interval)) 
            p2 (Math/sin theta)]
            ;p2 (Math/cos (- theta interval))]
        [(vec3 0 0 length) (vec3 p1 p2 0) (vec3 p2 p1 0)]))))

#_(defn init-cone
  []
  (def cone-mesh
    (create-display-list 
      (doseq [first-vertex [[0 0 1] [0 0 0]]]
	      (draw-triangle-fan
		      (let [stepsize (/ java.lang.Math/PI 8)
		            radius 0.25]
		        (apply vertex first-vertex)
		        (doseq [angle (range 0 (+ (* 2 java.lang.Math/PI) (/ stepsize 2)) stepsize)];; overcompensate to ensure cones wrap in spite of floating points             
		            (let [x (* (Math/cos angle) radius)
		                  y (* (Math/sin angle) radius)
                      px (* (Math/cos (- angle stepsize)) radius)
                      py (* (Math/sin (- angle stepsize)) radius)]
                  (when-not (zero? angle)
                    (apply normal (compute-normal [(apply vec3 first-vertex) (vec3 x y 0) (vec3 px py 0)])))
		              (vertex x y 0)))))))))

#_(defn draw-cone
  []
  (cone-mesh))


(ns brevis.shape.core
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [cantor]))  

(defn resize-shape
  "Change the dimension of an object's shape."
  [obj new-dim]
  (assoc-in obj [:shape :dim] new-dim))

(defn textured-quad []
  (push-matrix
    (translate -0.5 -0.5 0.5)
    (normal 0 0 -1)
    (draw-quads
      (texture 1 1) (vertex 1 1 0)
      (texture 0 1) (vertex 0 1 0)
      (texture 0 0) (vertex 0 0 0)
      (texture 1 0) (vertex 1 0 0))))

(defn xor [a b] (or (and a (not b)) (and (not a) b)))

#_(defn create-checkers []
  (let [tex (create-byte-texture 128 128)]
    (data/overwrite!
     tex
     (apply concat
            (for [x (range 128) y (range 128)]
              (if (xor (even? (bit-shift-right x 4)) (even? (bit-shift-right y 4)))
                [1 0 0 1]
                [0 0 0 1]))))
    tex))

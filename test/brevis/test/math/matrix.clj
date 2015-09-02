(ns brevis.test.math.matrix
    (:use [clojure.test]
          [brevis.math matrix]))

(let [lhs (seq-to-matrix 2 2 [3 2  1 -1])
     rhs (seq-to-matrix 2 1 [5 0])]
  (.equals (seq-to-matrix 2 1 [1 2])
    (linear-solve lhs rhs)))
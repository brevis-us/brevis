(ns brevis.test.math.logistic-regression
  (:use [clojure.test]
        [brevis.math logistic-regression matrix]))

(defn logistic-regression-test
  []
  (let [X (seq-to-matrix 10 2 (flatten (repeat 2 (range 1 11))))
        theta (seq-to-matrix 2 1 '(0.25 0.75))
        Y (transpose (matrix-mult (transpose theta) (transpose X)))
        ]
;    [X theta Y]
    (logistic-regression X Y 10 theta)
    ))
(ns brevis.logistic-regression
    (:use [brevis matrix]))


(defn sigmoid
  "Hyperbolic-tangent function."
  [num]
  (java.lang.Math/tanh num))

(defn theta-rand
  "Returns number between -1 & 1."
  []
  (let [switch (rand-int 2)]
    (if (= switch 0)
      (rand)
      (unchecked-negate (rand)))))

(defn theta-init
  "Random m x n matrix with all element values between -1 & 1."
  [num]
  (seq-to-matrix num 1 (repeatedly num #(theta-rand))))

(defn logistic-regression
  "Logistic regression. x feature matrix, y label vector."
  [x y]
  (let [num-features (matrix-width x)]
    (loop [stop 0
           theta (theta-init num-features)
           ]
      (if (= stop 500)
        theta
        (recur (inc stop)
               (matrix-mult x 
                            (transpose (sub y 
                                            (matrix-map sigmoid 
                                                        (matrix-mult (transpose theta) 
                                                                     x))))))))))
(ns brevis.logistic-regression
    (:use [brevis.math matrix]
          [brevis random]))

(defn sigmoid
  "Hyperbolic-tangent function."
  [num]
  (java.lang.Math/tanh num))

(defn theta-rand
  "Returns number between -1 & 1."
  []
  (let [switch (lrand-int 2)]
    (if (= switch 0)
      (rand)
      (unchecked-negate (lrand)))))

(defn normalize-data ;should this be abs?
  "subtracts mean vector from all row vectors"
  [mat]
  (let [height (matrix-height mat)
        width (matrix-width mat)
        mean-vector (map #(/ % height) (matrix-to-seq (sum-rows mat)))
        normalized-data (seq-to-matrix width height
                                       (for [x1 (partition width (matrix-to-seq mat))]
                                         (map #(- %1 %2) x1 mean-vector)))
        ]
    normalized-data))

(defn theta-init
  "Random m x n matrix with all element values between -1 & 1."
  [num]
  (seq-to-matrix num 1 (repeatedly num #(theta-rand))))

(defn logistic-regression
  "Logistic regression. x feature matrix, y label vector."
  ([x y stop] (logistic-regression x y stop (theta-init (matrix-width x))))
  ([x y stop theta-start]
    (loop [start 0
           theta theta-start
           ]
      (if (= stop start)
        theta
        (recur (inc start)
               (matrix-mult x 
                            (transpose (sub y 
                                            (matrix-pmap sigmoid 
                                                        (matrix-mult (transpose theta) 
                                                                     x))))))))))



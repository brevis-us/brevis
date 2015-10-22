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

(defn abs
  "Returns absolute value of a number"
  [num]
  (if (pos? num)
    num
    (unchecked-negate num)))

(defn csv-to-nest-seq
  [path]
  (let
    [data (map #(seq (clojure.string/split % #", "))
               (clojure.string/split-lines (slurp path)))
     
     width (count (first data))
       
     output (partition width (map #(let [el (read-string %)]
                                     (if (number? el)
                                       el
                                       %))
                                  (flatten data)))]
    output))

(defn nest-seq-to-csv
  [seq path]
  (let [width (count (first seq))
        string-seq (for [x (partition width (map str (flatten seq)))]
                     (apply str (interpose ", " x)))
        output-string (apply str (interpose "\n" string-seq))]
    (spit path output-string)))

(defn normalize-data ;should this be abs?
  "subtracts mean vector from all row vectors"
  [mat]
  (let [height (matrix-height mat)
        width (matrix-width mat)
        mean-vector (map #(/ % height) (matrix-to-seq (sum-rows mat)))
        normalized-data (seq-to-matrix width height
                                       (flatten 
                                         (for [x1 (partition width (matrix-to-seq mat))]
                                           (map #(- %1 %2) x1 mean-vector))))
        ]
    normalized-data))


(defn theta-init
  "Random m x n matrix with all element values between -1 & 1."
  [num]
  (seq-to-matrix num 1 (repeatedly num #(theta-rand))))

(defn pca
  ""
  [mat]
  (let [data (normalize-data mat)
        sigma (divide (matrix-mult data (transpose data)) (matrix-width data))
        ]))

(defn matrix-to-rvec-seq
  "takes matrix and turns into sequence of row vectors."
  [mat]
  (map #(seq-to-matrix 1 (matrix-width mat) %)
       (partition (matrix-width mat) (matrix-to-seq mat))))

(defn rvec-seq-to-matrix ; most likely will break when seq-to-matrix is fixed
  "takes row vector sequence and returns matrix"
  [rvec-seq]
  (let [new-seq-nest (map #(matrix-to-seq %) rvec-seq)
        height (count new-seq-nest)
        width (count (first new-seq-nest))
        new-mat (seq-to-matrix width height (flatten new-seq-nest))] ;Seq-to-matrix
    new-mat))

(defn rvec-seq-to-seq
  "takes a row vector sequence and returns nested sequence."
  [rvec-seq]
  (map matrix-to-seq rvec-seq))

(defn seq-to-rvec-seq
  "takes a nested sequence and returns a row vector sequence."
  [seq]
  (map #(seq-to-matrix 1 (count (first seq)) %) seq))

(defn rvec-seq-sort
  ([rvec-seq] (rvec-seq-sort rvec-seq :main))
  ([rvec-seq option]
    (let [sorted (sort-by #(apply + %) (rvec-seq-to-seq rvec-seq))]
      (if (= option :seq)
        sorted
        (seq-to-rvec-seq sorted)))))

(defn k-start 
  "simple k-start for k-means with no repeat centroids"
  ([k mat] (k-start k mat (matrix-to-rvec-seq mat)))
  ([k mat rvec-seq]
  (let [start (repeatedly k #(nth rvec-seq (lrand-int (matrix-height mat))))
        copies? (flatten 
                  (for [x start]
                    (map #(= x %) start)))
        truths (count (filter true? copies?))
        total (count start)]
    (if (= truths total)
      start
      (k-start k mat rvec-seq)))))


(defn k-means ; will most likely break if seq-to-matrix is fixed
  "k-means clustering algorithm"
  [k mat]
  (let [rvec-seq (matrix-to-rvec-seq mat)]
    (loop [iterations 0
           means (k-start k mat rvec-seq)]
      (let [new-clusters (group-by 
                           #(first 
                              (apply min-key second 
                                     (map-indexed vector 
                                                  (for [x1 means] 
                                                    (frobenius-norm 
                                                      (sub x1 %)))))) rvec-seq)
            
            new-means (for [x (map second new-clusters)] 
                        (matrix-map #(/ % (count x)) 
                                   (sum-rows (transpose (rvec-seq-to-matrix (flatten x)))))) ; what is up with transpose?
                                    
            mean-distance (apply + (map #(frobenius-norm (sub %1 %2)) (rvec-seq-sort means) (rvec-seq-sort new-means)))
           
            end (if (<= mean-distance 0)
                  true
                  false)]
        
      (if (or (= end true) (> iterations 2000))
        {:clusters new-clusters
         :centroids (rvec-seq-sort new-means )
         ;:old-centroids means
         :iterations iterations
         :distance mean-distance
         }
        (recur (inc iterations)
               new-means))))))


(defn logistic-regression
  "Logistic regression. x feature matrix, y label vector."
  ([x y stop] (logistic-regression x y stop (theta-init (matrix-width x))))
  ([x y stop theta-start]
    (loop [iteration 0
           theta theta-start
           ]
      (if (= stop iteration)
        theta
        (recur (inc iteration)
               (matrix-mult (transpose x) 
                            (sub y 
                                 (matrix-pmap sigmoid 
                                             (matrix-mult x 
                                                          theta
                                                          )))))))))



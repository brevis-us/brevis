(ns brevis.evolution.roulette
  (:use [brevis random]))

;; from incanter.core, dont want to import incanter because of size
(defn cumulative-sum
  " Returns a sequence of cumulative sum for the given collection. For instance
    The first value equals the first value of the argument, the second value is
    the sum of the first two arguments, the third is the sum of the first three
    arguments, etc.

    Examples:
      (use 'incanter.core)
      (cumulative-sum (range 100))
  "
  ([coll]
   (loop [in-coll (rest coll)
          cumu-sum [(first coll)]
          cumu-val (first coll)]
     (if (empty? in-coll)
       cumu-sum
       (let [cv (+ cumu-val (first in-coll))]
         (recur (rest in-coll) (conj cumu-sum cv) cv))))))

(defn make-roulette-wheel
   "Make a roulette wheel from an array [currently assume positive entries only]."
   [v selection-strength]
   (let [v (map #(+ 1 (- selection-strength) (* selection-strength %)) v)
         min-v (apply min v)
         v (map #(+ min-v %) v)
         sum (apply + v)
         probs (map #(/ % sum) v)]
         ;probs-sum (apply + probs)
         ;probs (map #(+ selection-strength (/ % probs-sum)) probs)]
     (cumulative-sum probs)))

(defn roulette
  "Roll a roulette wheel. Return the index."
  [wheel]
  (let [r (lrand)]
    (first (drop-while #(< (nth wheel %) r)
                       (range (count wheel))))))

(defn select-with
  "Roulette selection with on key function with selection-strength."
  [population key-fn 
   & {:keys [selection-strength 
             n]
      :or {selection-strength nil
           n 1}}]  
  (let [wheel (make-roulette-wheel (map key-fn population) (if selection-strength selection-strength 1))
        selected (for [k (range n)]      
                   (let [idx (roulette wheel)]
                     (nth population idx)))]
      selected))

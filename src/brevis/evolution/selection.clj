(ns brevis.evolution.selection
  (:use [brevis random]
        [brevis.evolution.roulette :exclude [select-with]]))

(defn tournament-max
  "Return the winner of a tournament for maximum value of the key function."
  [tournament-size key-fn population]
  (let [tournament (repeatedly tournament-size #(lrand-nth population))]
    (apply (partial max-key key-fn) tournament)))

(defn tournament-min
  "Return the winner of a tournament for minimum value of the key function."
  [tournament-size key-fn population]
  (let [tournament (repeatedly tournament-size #(lrand-nth population))]
    (apply (partial min-key key-fn) tournament)))

(defn select-with-method
  "Select with a given method."
  [selection-method key-fn population]
  (cond (= :tournament-max (:method selection-method))
        (tournament-max (:tournament-size selection-method) key-fn population)
        (= :tournament-min (:method selection-method))
        (tournament-min (:tournament-size selection-method) key-fn population)))

(defn select-with
  "Selection with a selection method. This returns a list of individuals even if you want just one."
  [population key-fn 
   & {:keys [selection-method
             n]
      :or {selection-method {:method :tournament-max
                             :tournament-size 7}
           n 1}}]
  (cond (or (= selection-method :tournament-max)
            (= selection-method :tournament-min))
        (let [selected (for [k (range n)]      
                         (select-with-method selection-method key-fn population))]
          selected)
        :else; roulette
        (let [wheel (make-roulette-wheel (map key-fn population) (if (:selection-strength selection-method) 
                                                                   (:selection-strength selection-method) 1))
              selected (for [k (range n)]      
                         (let [idx (roulette wheel)]
                           (nth population idx)))]
          selected)))

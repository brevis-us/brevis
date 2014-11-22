(ns brevis.evolution.selection
  (:use [brevis random]))

(defn tournament-max
  "Return the winner of a tournament for maximum value of the key function."
  [tournament-size key-fn population]
  (let [tournament (repeatedly tournament-size #(lrand-nth population))]
    (apply (partial max-key key-fn) tournament)))

(defn select-with-method
  "Select with a given method."
  [selection-method key-fn population]
  (cond (= :tournament-max (:method selection-method))
        (tournament-max (:tournament-size selection-method) key-fn population)))

(defn select-with
  "Selection with a selection method. This returns a list of individuals even if you want just one."
  [population key-fn 
   & {:keys [selection-method
             n]
      :or {selection-method {:method :tournament-max
                             :tournament-size 7}
           n 1}}]
  (let [selected (for [k (range n)]      
                   (select-with-method selection-method key-fn population))]
    selected)) 

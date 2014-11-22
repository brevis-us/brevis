(ns brevis.example.ea.ones-max
  (:gen-class)
  (:use [brevis core vector utils parameters random]
        [brevis.evolution.ea simple-ea]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Example for simple ea. 
;; Evolve a soution to the ones-max problem.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Parameters

(swap! params assoc
       :population-size 1000
       :fitness-function (fn [x] (apply + (map #(if % 1 0) x)))
       :mutation-probability 0.9
       :init-genome-fn (fn [] (repeatedly 100 #(lrand-nth [true false])))
       :termination-fn (fn [] (or (>= (get-time) 100); max generations
                                  (some #(= 100 %) (map :fitness (all-objects))))); or some fitness is 100
       :mutate-genome-fn (fn [x] 
                           (for [el x] (if (zero? (lrand-int 100)) (not el) el))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(autostart-in-repl -main)


(ns brevis.example.ea.ones-max
  (:gen-class)
  (:use [brevis core vector utils parameters random plot]
        [brevis.evolution.ea simple-ea]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Example for simple ea. 
;; Evolve a soution to the ones-max problem.
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Parameters

;; If one didn't want to write the plot to file at the end of the simulation.
#_(swap! params assoc
        :population-size 1000
        :fitness-function (fn [x] (apply + (map #(if % 1 0) x)))
        :mutation-probability 0.9
        :init-genome-fn (fn [] (repeatedly 100 #(lrand-nth [true false])))
        :termination-fn (fn [] (or (>= (get-time) 100); max generations
                                   (some #(= 100 %) (map :fitness (all-objects))))); or some fitness is 100
        :mutate-genome-fn (fn [x] 
                            (for [el x] (if (zero? (lrand-int 100)) (not el) el))))

(swap! params assoc
        :population-size 1000
        :fitness-function (fn [x] (apply + (map #(if % 1 0) x)))
        :mutation-probability 0.9
        :init-genome-fn (fn [] (repeatedly 100 #(lrand-nth [true false])))
        :termination-fn (fn [] 
                          (let [terminate? (or (>= (get-time) 100); max generations
                                               (some #(= 100 %) (map :fitness (all-objects))))]; or some fitness is 100
                            (when terminate?
                              (write-plot-to-file (first (all-plotters)) "ones_max_fitness.png"))
                            terminate?))
        :mutate-genome-fn (fn [x] 
                            (for [el x] (if (zero? (lrand-int 100)) (not el) el))))

(add-multiplot-handler
    :xy-fns [(fn [] 
               (let [v (map :fitness (all-objects))]
                 [(* (get-time) (get-dt)) (apply max v)]))
             (fn [] 
               (let [v (map :fitness (all-objects))]
                 [(* (get-time) (get-dt)) (/ (apply + v) (count v))]))]
    :interval 200
    :legends ["best" "average"]
    :title "Fitness")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(autostart-in-repl -main)


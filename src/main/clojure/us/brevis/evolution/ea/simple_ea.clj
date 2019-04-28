(ns us.brevis.evolution.ea.simple-ea
  (:require [us,brevis-utils.parameters :as params])
  (:use [us,brevis core vector utils random]
        [us.brevis.evolution selection]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## This is a simple generational EA

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Parameters

(swap! params/params assoc
       :population-size 100
       :max-generations 100
       :fitness-function (fn [x] Double/POSITIVE_INFINITY)
       :mutation-probability 0.9
       :init-genome-fn (fn [] nil)
       :mutate-genome-fn (fn [x] x)
       :best-selector max-key
       :selection-method {:method :tournament-max
                          :tournament-size 17})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Update code

(defn initialize-population
  "Initialize a population. Provide a function for initializing individual genomes."
  []
  (dotimes [k (params/get-param :population-size)]    
    (add-object (assoc (make-abstract {:type :ea-individual})
                       :genome ((params/get-param :init-genome-fn))))))

(defn update-population
  "Update a population by evaluating all individuals."
  []
  ; Evaluate
  (doseq [obj (all-objects)]
    (let [fitness ((params/get-param :fitness-function) (:genome obj))]
      (set-object (get-uid obj)
                  (assoc obj
                         :fitness fitness))))
  ; Select/vary
  (let [child-genomes (for [k (range (params/get-param :population-size))]
                        (let [parent-genome (:genome (first (select-with (all-objects) 
                                                                         :fitness 
                                                                         :selection-method (params/get-param :selection-method))))
                              r (lrand)]
                          (cond (< r (params/get-param :mutation-probability))
                                ((params/get-param :mutate-genome-fn) parent-genome)
                                :else
                                parent-genome)))
        uids (all-object-uids)]
    (dotimes [k (count child-genomes)]
      (set-object (nth uids k)
                  (assoc (get-object (nth uids k)) 
                         :genome (nth child-genomes k))))))

(defn report-population
  "Report on the state of the population."
  []
  (if (params/get-param :report-fn)
    ((params/get-param :report-fn))
    (let [best (apply (partial (params/get-param :best-selector) :fitness) (all-objects))
        fitnesses (map :fitness (all-objects))]
      (println "Generation" (get-time))
      (println "Best genome:" (:genome best))
      (println "Best fitness:" (:fitness best))
      (println "Avg fitness:" (float (/ (apply + fitnesses) (count fitnesses)))))))

;(add-global-update-handler 90 update-population)
;(add-global-update-handler 91 report-population)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

#_(defn initialize-simulation
   "This is the function where you add everything to the world."
   []  
   (disable-neighborhoods)
   (disable-collisions)
  
   (set-dt 1)
  
   (if (get-param :initialize-population-fn)
     ((get-param :initialize-population-fn))
     (initialize-population))
  
   (if (get-param :termination-fn)
     (add-terminate-trigger (get-param :termination-fn))  
     (add-terminate-trigger (get-param :max-generations))))

;; Start zee macheen
#_(defn -main [& args]
   (start-nogui initialize-simulation))

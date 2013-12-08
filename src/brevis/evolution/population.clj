(ns brevis.evolution.population
  (:use [brevis.random]))

(defn make-population
  "Return a population data structure."
  []
  {:individuals []})

(defn initialize-population
  "Initialize a population of size N with the
supplied method for generating new individuals
NOTE: keep track of the random seed you are
using for make-individual in order to facilitate
reproduction of results."
  [p N make-individual]
  (assoc p
         :individuals (into [] (doall (repeatedly N make-individual)))))

(defn save-population
  "Save a population to file."
  [p filename]
  (spit filename
        (with-out-str
          (let [individuals (:individuals p)]
            (print "{")
            (doseq [[k v] (dissoc p :individuals)]
              (println k v))
            (print ":individuals [")
            (doseq [ind individuals]
              (println ind))
            (println "]}")))))

(defn load-population
  "Load a population from file."
  [filename]
  (let [p (read-string (slurp filename))]
    p))
          
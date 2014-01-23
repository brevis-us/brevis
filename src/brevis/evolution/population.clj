#_"This file is part of brevis.                                                                                                                                                 
                                                                                                                                                                                     
    brevis is free software: you can redistribute it and/or modify                                                                                                           
    it under the terms of the GNU General Public License as published by                                                                                                             
    the Free Software Foundation, either version 3 of the License, or                                                                                                                
    (at your option) any later version.                                                                                                                                              
                                                                                                                                                                                     
    brevis is distributed in the hope that it will be useful,                                                                                                                
    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                                                                                   
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                                                                                    
    GNU General Public License for more details.                                                                                                                                     
                                                                                                                                                                                     
    You should have received a copy of the GNU General Public License                                                                                                                
    along with brevis.  If not, see <http://www.gnu.org/licenses/>.                                                                                                          
                                                                                                                                                                                     
Copyright 2012, 2013 Kyle Harrington"     

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

(defn select-tournament
  "Select an individual by tournament"
  [p tournament-size individual-comparator]
  (first (sort individual-comparator
               (take tournament-size 
                     (repeatedly #(lrand-nth (:individuals p)))))))
          
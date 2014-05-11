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

(ns brevis.utils
  (:require [me.raynes.conch :refer [programs with-programs let-programs]])
  (:use [brevis globals]
        [brevis.physics core utils]))

(programs mkdir tar)

(defn add-terminate-trigger
  "Add a termination trigger. If it is a number, then it is a threshold on time, 
otherwise it should be a function that returns true/false"
  [trigger]
  (add-global-update-handler 10000 
    (fn [] 
      (when (and (not (nil? trigger))
                 (or (and (number? trigger)                      
                          (> (get-time) trigger))
                     (and (not (number? trigger))
                          (trigger))))
        (swap! *gui-state* assoc :close-requested true)))))

(defn get-objects
  "Return all objects in the simulation."
  []
  (seq (.toArray (.getObjects  @*java-engine*))))

;; from lspector's taggp (avail on github)
(defn pmapall
  "Like pmap but: 1) coll should be finite, 2) the returned sequence
   will not be lazy, 3) calls to f may occur in any order, to maximize
   multicore processor utilization, and 4) takes only one coll so far."
  [f coll]
  (if false;@*brevis-parallel*
    (map f coll)
    (let [agents (map #(agent % :error-handler (fn [agnt except] (println except))) coll)]
      (dorun (map #(send % f) agents))
      (apply await agents)
      (doall (map deref agents)))))

#_(defn save-simulation-state
   "[EXPERIMENTAL:PROBABLY WONT SAVE WHAT YOU NEED] Save the state of the simulation to filename."
   [filename]
   #_(mkdir (str filename "_brevis"))
   (spit filename
         (with-out-str
           (doseq [obj (all-objects)]
             (println (str obj))))
         :append true))

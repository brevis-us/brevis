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
  (:use [brevis globals]
        [brevis.physics core]))

#_(defn reset-core
  "Reset the core variables."
  []
  #_(reset! *collision-handlers* {})
  (reset! *gui-message-board* (sorted-map))
  (reset! *collisions* {})
  #_(reset! *update-handlers* {})
  (reset! *physics* nil)
  (reset! *objects* {}))

#_(defn disable-collisions "Disable collision detection." [] (reset! collisions-enabled false))
#_(defn enable-collisions "Enable collision detection." [] (reset! collisions-enabled true))

#_(defn disable-neighborhoods "Disable neighborhood detection." [] (reset! neighborhoods-enabled false))
#_(defn enable-neighborhoods "Enable neighborhood detection." [] (reset! neighborhoods-enabled true))

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

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
  (:import [brevis Engine])
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
  (seq (.toArray (.getObjects ^Engine @*java-engine*))))

(defn set-parallel
  "set parallel flag"
  [new-flag]
  (.setParallel @*java-engine* new-flag))
                                          
(defn get-parallel
  "get parallel flag"
  []
  (.getParallel @*java-engine*))

(defn enable-fps-display
  "Print FPS."
  []
  (swap! *gui-state* assoc :display-fps true))

(defn disable-fps-display
  "Stop printing FPS."
  []
  (swap! *gui-state* assoc :display-fps false))

#_(defn save-simulation-state
   "[EXPERIMENTAL:PROBABLY WONT SAVE WHAT YOU NEED] Save the state of the simulation to filename."
   [filename]
   #_(mkdir (str filename "_brevis"))
   (spit filename
         (with-out-str
           (doseq [obj (all-objects)]
             (println (str obj))))
         :append true))

(defn disable-skybox
  "Disable rendering of the skybox."
  []
  (swap! *gui-state* assoc :disable-skybox true))

(defn enable-skybox
  "Enable rendering of the skybox."
  []
  (swap! *gui-state* dissoc :disable-skybox))


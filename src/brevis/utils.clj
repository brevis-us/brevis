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

(defn reset-core
  "Reset the core variables."
  []
  #_(reset! *collision-handlers* {})
  (reset! *gui-message-board* (sorted-map))
  (reset! *collisions* {})
  #_(reset! *update-handlers* {})
  (reset! *physics* nil)
  (reset! *objects* {}))

(defn disable-collisions "Disable collision detection." [] (reset! collisions-enabled false))
(defn enable-collisions "Enable collision detection." [] (reset! collisions-enabled true))

(defn disable-neighborhoods "Disable neighborhood detection." [] (reset! neighborhoods-enabled false))
(defn enable-neighborhoods "Enable neighborhood detection." [] (reset! neighborhoods-enabled true))

(defn get-objects
  "Return all objects in the simulation."
  []
  (seq (.toArray (.getObjects  @*java-engine*))))

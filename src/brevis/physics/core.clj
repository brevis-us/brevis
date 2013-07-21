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

(ns brevis.physics.core
  (:import (brevis Engine)))

;; ## Globals

;; Hash map keyed on pairs of types with values of the respective collision function.
;; 
;; Keys are of the form [:ball :floor]
;;
;; Collision functions take [collider collidee] and return [collider collidee].
;;
;; Both can be modified; however, two independent collisions are actually computed [a b] and [b a]."
(def #^:dynamic *collision-handlers*
  (atom {}))

(def #^:dynamic *collisions* (atom #{}))

;; Hash map keyed on type with values of the respective update function.
;;
;; An update function should take 3 arguments:
;;
;; [object dt neighbors] and return an updated version of object                                                                                                 
;; given that dt amount of time has passed.
(def #^:dynamic *update-handlers*
  (atom {}))

#_(def simulation-boundary
  (box3 (vec3 100 100 100)
        (vec3 -100 -100 -100)))

(def #^:dynamic *dt* (atom 1))
(def #^:dynamic *neighborhood-radius* (atom 8.0))
(def #^:dynamic *physics* (atom nil))
(def #^:dynamic *objects* (atom {}))
(def #^:dynamic *added-objects* (atom {}))
(def #^:dynamic *deleted-objects* (atom #{}))
(def collisions-enabled (atom true))
(def neighborhoods-enabled (atom true))
(def #^:dynamic *brevis-parallel* (atom true))

#_(def #^:dynamic *java-engine* (atom nil))

(def #^:dynamic *java-engine* (atom (Engine.)))

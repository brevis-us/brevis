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

(ns brevis.example.mesh.mesh
  (:gen-class)
  (:use [brevis.graphics.basic-3D]
        [brevis.physics collision core space utils]
        [brevis.shape box mesh]
        [brevis core osd vector camera utils]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Mesh demo
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Globals

(def mesh-file "bunny.obj")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Make our mesh object

(defn make-real-mesh
  "Make a new bird with the specified program. At the specified location."
  [position]  
  (move (make-real {:type :real-mesh
                    :color (vec4 1 0 0 1)
                    :shape (create-mesh mesh-file true (vec3 10 10 10))})
        position))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (init-world)
  (init-view)  
  
  (set-camera-information (vec3 0.0 -27.036232 -30.0) (vec4 1.0 0.0 0.0 0.0))
  (disable-collisions)
  
  (set-dt 1)
  (set-neighborhood-radius 250)
  (default-display-text)
  #_(add-object (move (make-floor 500 500) (vec3 0 -10 0)))
  (add-object (make-real-mesh (vec3 0 25 0))))

;; Start zee macheen
(defn -main [& args]
  #_(start-nogui initialize-simulation)
  (if-not (empty? args)
    (start-nogui initialize-simulation)
    (start-gui initialize-simulation)))

(autostart-in-repl -main)


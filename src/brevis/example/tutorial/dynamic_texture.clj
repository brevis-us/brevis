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

(ns brevis.example.tutorial.dynamic-texture
  (:import java.lang.Math)  
  (:use [brevis.graphics.basic-3D]
        [brevis.physics collision core space utils]
        [brevis.shape box sphere cone]
        [brevis.core])
  (:use [penumbra opengl compute]
        [penumbra.opengl core])
  (:require [penumbra.data :as data]))

;; ## Tutorial: shape
;; 
;; ![](img/brevis_example_tutorial_dynamic_texture.png?raw=true)
;;
;; Use this tutorial to learn how to quickly draw shapes, change their colors, and move shapes around the world.
;; 

(defn xor [a b] (or (and a (not b)) (and (not a) b)))

(defn floor-pattern
  "A parametric floor pattern function."
  [v]
  (apply concat
         (for [x (range 128) y (range 128)]
           (if (xor (even? (bit-shift-right x 4)) (even? (bit-shift-right y 4)))
             [(double (- 1 v))
              (double (- 1 v))
              (double (- 1 v)) (double 1.0)]
             [(double v) (double v) (double v) (double 1.0)]))))

(defn make-dynamic-floor
  "Make a floor object."
  [w h]
  (let [floor-checkers 
        (let [tex (create-byte-texture 128 128)]
          (data/overwrite!
            tex
            (floor-pattern 0))
          tex)]
    (move (make-real {:color [0.8 0.8 0.8]
                      :shininess 20
                      :type :dynamic-floor
                      :density 8050
                      :texture floor-checkers
                      :shape (create-box w 0.1 h)})
          (vec3 0 -3 0))))

(defn update-floor
  "Update a demoshape."
  [demoshape dt objects]
  (let [floor-parm (mod (get-time) 1.0)]    
    (data/overwrite! (:texture demoshape)
                     (floor-pattern floor-parm))
    demoshape))
      
(add-update-handler :dynamic-floor update-floor); This tells the simulator how to update these objects

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (init-world);; Make the world 
  (set-dt 0.01)
  (add-object (make-dynamic-floor 500 500)))

;; This function is invoked by the command-line
(defn -main [& args]
  (start-gui initialize-simulation))

;; Start zee macheen
#_(-main)

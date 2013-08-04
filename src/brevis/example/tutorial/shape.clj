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

(ns brevis.example.tutorial.shape
  (:import java.lang.Math)  
  (:use [brevis.graphics.basic-3D]
        [brevis.physics collision core space utils vector]
        [brevis.shape box sphere cone]
        [brevis.core]
        ))    

;; ## Tutorial: shape
;;
;; ![](img/brevis_example_tutorial_shape.png?raw=true)
;; 
;; Use this tutorial to learn how to quickly draw shapes, change their colors, and move shapes around the world.
;; 

(defn shape-position
  "Return the shape of the position for a given time."
  [time num-shapes shape-num]
  (vec3  (* 50 (cos (+ (* 2 Math/PI (/ shape-num num-shapes) time))))
         10
         (* 50 (sin (+ (* 2 Math/PI (/ shape-num num-shapes) time))))))

(defn make-shapes
  "Make the shapes that will be used in our simulation."
  []
  (let [shapes [(create-sphere 5); A sphere of radius 5 
                (create-box 5 10 2)]]; A box of width, length, height
    (for [num (range (count shapes))]
      (let [position (shape-position (get-time) (count shapes) num)]            
        (move (make-real {:type :demoshape
                          :color [0 (- 1 num) num 1]
                          :shape (nth shapes num)
                          :shape-number num})
              position)))))

(defn update-demoshape
  "Update a demoshape."
  [demoshape dt objects]
  (let [new-position (shape-position (get-time) (count objects) (inc (:shape-number demoshape)))]
    (move demoshape new-position)))
      
(add-update-handler :demoshape update-demoshape); This tells the simulator how to update these objects

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (init-world);; Make the world 
  (set-dt 0.01)
  (add-object (make-floor 500 500))
  (let [shapes (make-shapes)];; Make our object
    (doseq [shape shapes]
      (add-object shape))));; Add the object to the simulation

;; This function is invoked by the command-line
(defn -main [& args]
  (start-gui initialize-simulation))

;; Start zee macheen
(when (find-ns 'ccw.complete)
  (-main))

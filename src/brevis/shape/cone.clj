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

(ns brevis.shape.cone
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [brevis.shape.core]
        [cantor])) 

(defn create-cone
  "Create a cone object."
  ([]
     (create-cone 1 1))
  ([length base]
     {:dim (vec3 (Math/max length base) (Math/max length base) (Math/max length base))
      :type :cone
      }))

(defn gen-cone-faces
  [length lod]
  (let [interval (/ 360 lod)
        base (range 0 361 interval)]
    (for [theta (rest base)]
      (let [p1 (Math/sin (- theta interval)) 
            p2 (Math/sin theta)]
            ;p2 (Math/cos (- theta interval))]
        [(vec3 0 0 length) (vec3 p1 p2 0) (vec3 p2 p1 0)]))))

(defn init-cone
  []
  (def cone-mesh
    (create-display-list 
      (doseq [first-vertex [[0 0 1] [0 0 0]]]
	      (draw-triangle-fan
		      (let [stepsize (/ java.lang.Math/PI 8)
		            radius 0.25]
		        (apply vertex first-vertex)
		        (doseq [angle (range 0 (+ (* 2 java.lang.Math/PI) (/ stepsize 2)) stepsize)];; overcompensate to ensure cones wrap in spite of floating points             
		            (let [x (* (Math/cos angle) radius)
		                  y (* (Math/sin angle) radius)
                      px (* (Math/cos (- angle stepsize)) radius)
                      py (* (Math/sin (- angle stepsize)) radius)]
                  (when-not (zero? angle)
                    (apply normal (compute-normal [(apply vec3 first-vertex) (vec3 x y 0) (vec3 px py 0)])))
		              (vertex x y 0)))))))))

(defn draw-cone
  []
  (cone-mesh))


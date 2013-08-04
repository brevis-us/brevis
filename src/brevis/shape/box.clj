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

(ns brevis.shape.box
  (:import [brevis BrShape])
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [brevis.shape.core]
        [cantor]))        

#_(defn create-box
  "Create a box object."
  ([]
     (create-box 1 1 1))
  ([width height depth]
     {:dim (vec3 width height depth)
      :type :box
      }))

(defn create-box
  "Create a box object."
  ([]
     (create-box 1 1 1))
  ([width height depth]
    (BrShape/createBox width height depth)))

(defn create-collision-box 
  "Create a collision shape for a box."
  [pos shape]
  (box3 (sub pos (div (:dim shape) 2))
	(add pos (div (:dim shape) 2))))

(defn draw-textured-cube []
  (dotimes [_ 4]
    (rotate 90 0 1 0)
    (textured-quad))
  (rotate 90 1 0 0)
  (textured-quad)
  (rotate 180 1 0 0)
  (textured-quad))

(defn init-box-graphic
  []
  (def box-mesh 
    (create-display-list 
      (draw-textured-cube)))
  #_(def box-graphic
    (define-display-list :quads
      [[(vec3 -0.5 -0.5 0.5) (vec3 -0.5 0.5 0.5) (vec3 -0.5 0.5 -0.5) (vec3 -0.5 -0.5 -0.5)]
       [(vec3 0.5 -0.5 0.5) (vec3 0.5 0.5 0.5) (vec3 0.5 0.5 -0.5) (vec3 0.5 -0.5 -0.5)]
       [(vec3 0.5 -0.5 -0.5) (vec3 0.5 -0.5 0.5) (vec3 -0.5 -0.5 0.5) (vec3 -0.5 -0.5 -0.5)]
       [(vec3 0.5 0.5 -0.5) (vec3 0.5 0.5 0.5) (vec3 -0.5 0.5 0.5) (vec3 -0.5 0.5 -0.5)]
       [(vec3 -0.5 0.5 -0.5) (vec3 0.5 0.5 -0.5) (vec3 0.5 -0.5 -0.5) (vec3 -0.5 -0.5 -0.5)]
       [(vec3 -0.5 0.5 0.5) (vec3 0.5 0.5 0.5) (vec3 0.5 -0.5 0.5) (vec3 -0.5 -0.5 0.5)]])))

(defn draw-box
  []
  (box-mesh))

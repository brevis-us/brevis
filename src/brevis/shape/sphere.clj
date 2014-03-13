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

(ns brevis.shape.sphere
  (:import [brevis BrShape])
  (:use ;[penumbra opengl compute]
        ;[penumbra.opengl core]
        [brevis.shape.core]
        [brevis.graphics multithread]
        [brevis vector])) 

(defn create-sphere
  "Create a sphere object."
  ([]
     (create-sphere 1))
  ([radius]
    (begin-with-graphics-thread)
    (let [sphere (BrShape/createSphere radius (:gui @brevis.globals/*gui-state*))]
      (.setDimension sphere (vec3 radius radius radius) (:gui @brevis.globals/*gui-state*))
      (end-with-graphics-thread)
      sphere)))

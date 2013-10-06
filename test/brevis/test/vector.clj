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

(ns brevis.test.vector  
  (:use [clojure.test]
        [brevis vector]))

(deftest test-add-vec3
  (let [v1 (vec3 1 1 1)
        v2 (vec3 -1 -1 -1)]
    (is (zero? (length (add v1 v2))))))

(deftest test-add-vec4
  (let [v1 (vec4 1 1 1 1)
        v2 (vec4 -1 -1 -1 -1)]
    (is (zero? (length (add v1 v2))))))



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

(ns brevis.shape.mesh
  (:import [brevis BrShape])
  (:use [brevis.shape.core]))        

(defn create-mesh
  "Create a mesh object."
  [filename is-resource?]
  #_(println "create-mesh" filename)
  (BrShape/createMeshFromFile filename is-resource? (:gui @brevis.globals/*gui-state*))
  #_(BrShape. filename)
  #_(BrShape/loadMesh (BrShape.) filename))

(defn shape-from-mesh
  "Create a mesh object."
  [mesh]
  (println mesh)
  (BrShape/createMeshFromBrMesh mesh)
  #_(BrShape. filename)
  #_(BrShape/loadMesh (BrShape.) filename))

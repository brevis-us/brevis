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
  (:import [brevis BrShape]
           [brevis.graphics BrMesh])
  (:use [brevis.vector]
        [brevis.shape.core]
        [brevis.graphics multithread]))        

(defn create-mesh
  "Create a mesh object."
  ([filename is-resource?]
    (create-mesh filename is-resource? (vec3 1 1 1)))
  ([filename is-resource? scale]
    (begin-with-graphics-thread)
    (let [result (BrShape/createMeshFromFile filename is-resource? (:gui @brevis.globals/*gui-state*) scale)]
      (end-with-graphics-thread)
      result)))

(defn shape-from-mesh
  "Create a mesh object."
  [mesh]
  #_(println mesh)
  (BrShape/createMeshFromBrMesh mesh)
  #_(BrShape. filename)
  #_(BrShape/loadMesh (BrShape.) filename))

(defn num-vertices
  "Return the number of vertices in a mesh."
  [mesh]
  (.numVertices ^BrMesh mesh))

(defn get-vertex
  "Return a vertex on a mesh."
  [mesh idx]
  (.getVertex ^BrMesh mesh ^int idx))

(defn set-vertex
  "Return a vertex on a mesh."
  [mesh idx v]
  (.setVertex ^BrMesh mesh ^int idx ^floats v))

(defn num-faces
  "Return the number of faces in a mesh."
  [mesh]
  (.numFaces ^BrMesh mesh))

(defn get-face
  "Return a face on a mesh."
  [mesh idx]
  (.getFace ^BrMesh mesh ^int idx))

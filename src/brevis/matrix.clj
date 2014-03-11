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


(ns brevis.matrix
  (:use [brevis vector math])
  (:import [org.lwjgl.util.vector Vector3f Vector4f Matrix4f]
           [java.nio FloatBuffer]
           [org.lwjgl BufferUtils])
  #_(:import [javax.vecmath Vector3d Vector4d Matrix4d]))

(defonce x-axis (vec3 1 0 0))
(defonce y-axis (vec3 0 1 0))
(defonce z-axis (vec3 0 0 1))
(defonce origin (vec4 0 0 0 1))

#_(defn mat4
   "Create a 4x4 matrix."  
   ([ v0 v1 v2 v3 v4 v5 v6 v7 v8 v9 v10 v11 v12 v13 v14 v15 ]
     (Matrix4f.  v0 v1 v2 v3 v4 v5 v6 v7 v8 v9 v10 v11 v12 v13 v14 v15))
   ([]
     (mat4 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0)))

(defn mat4
   "Create a 4x4 matrix."  
   ([float-buffer]     
     (let [mat (Matrix4f.)]
       (.load mat float-buffer)))
   ([ v0 v1 v2 v3 v4 v5 v6 v7 v8 v9 v10 v11 v12 v13 v14 v15 ]
     (mat4 (FloatBuffer/wrap (float-array [v0 v1 v2 v3 v4 v5 v6 v7 v8 v9 v10 v11 v12 v13 v14 v15]))))
   ([]
     (mat4 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0)))

#_(defonce identity4 (.setIdentity (mat4)))

(defn position
  "Return the position of xform."
  [xform]
  (let [position (.transform xform origin (vec4 0 0 0 0))]
    position))

(defn translate
  "Translate xform by vector"
  [xform v]
  #_(println xform v (position xform)) 
  (.setTranslation xform (add v (position xform)))
  xform)

(defn rotate
  "Rotate xform about axis by angle."
  [xform axis angle]
  (.rotate xform angle axis)
  xform) 
    
(defn rotate-x
  "Rotate xform about x-axis by angle."
  [xform angle]
  (rotate xform x-axis angle)
  #_(.rotX xform angle)
  xform)

(defn rotate-y
  "rotate xform about y-axis by angle."
  [xform angle]
  (rotate xform y-axis angle)
  #_(.rotY xform angle)  
  xform)

(defn rotate-z
  "Rotate xform about z-axis by angle."
  [xform angle]
  (rotate xform z-axis angle)
  #_(.rotZ xform angle)
  xform)
  
(defn transform
  "Transform a vector."
  [xform v]
  (let [outv (vec4 0 0 0 0)]
    #_(def tmp001 {:xform xform :v v :outv outv})
    (Matrix4f/transform xform v outv)
    outv))

(defn identity-mat
  "Convert a matrix to an identity matrix."
  [m]
  (.setIdentity m)
  m)

;; derived from Cantor's rotation-matrix
(defn rotation-matrix
   "Returns a matrix which 3-D vectors about
the specified axis."
   ([theta x y z]
     (let [theta (radians theta)
           s (Math/sin theta)
           c (Math/cos theta)
           t (- 1 c)]       
       (mat4
         (+ c (* t x x)) (- (* t x y) (* s z)) (+ (* t x z) (* s y)) 0
         (+ (* t x y) (* s z)) (+ (* t y y) c) (- (* t y z) (* s x)) 0
         (- (* t x z) (* s y)) (+ (* t y z) (* s x)) (+ (* t z z) c) 0
         0 0 0 1))))

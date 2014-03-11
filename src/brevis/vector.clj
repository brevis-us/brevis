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

(ns brevis.vector
  #_(:import [org.ejml.data DenseMatrix64F]
           [org.ejml.ops CommonOps])
  (:use [brevis.math])
  (:import [org.lwjgl.util.vector Vector3f Vector4f])
  #_(:import [javax.vecmath Vector3f Vector4f]))
;; Temporary way of making Java's Vector3f's look like Cantor's vec3's

(defn vec3
  "Make a Vector3f"
  [x y z]
  (Vector3f. x y z))

(defn vec3?
  "Test if this is a vec3."
  [v]
  (= (class v) org.lwjgl.util.vector.Vector3f))

(defn vec4
  "Make a Vector4f"
  [x y z w]
  (Vector4f. x y z w))

(defn vec4-to-vec3
  "convert a vec4 to a vec3"
  [v]
  (vec3 (.x v) (.y v) (.z v)))

(defn vec3-to-vec4
  "Convert a vec3 to a vec4 by padding the 4th dim with 1."
  [v]
  (vec4 (.x v) (.y v) (.z v) 1))

(defn sub
  "Wrap's Vector3f sub."
  [v1 v2]
  #_((if (vec3? v1) Vector3f/sub Vector4f/sub)
    v1 v2 nil)
  (if (vec3? v1) 
    (Vector3f/sub v1 v2 nil)
    (Vector4f/sub v1 v2 nil)))    

(defn div
  "Divide a Vector3f by a scalar."
  [v s]
  (let [vr (if (vec3? v) (Vector3f. v) (Vector4f. v))]             
    (.scale vr (double (/ s)))
    vr))
    
(defn add
  "Add Vector3f's"
  ([v]
    v)
  ([v1 v2]
    (if (vec3? v1) 
      (Vector3f/add v1 v2 nil)
      (Vector4f/add v1 v2 nil)))
  ([v1 v2 & vs]
    (loop [vs vs
           v (add v1 v2)]
      (if (empty? vs)
        v
        (recur (rest vs)
               (add v (first vs)))))))

(defn mul
  "Multiply a Vector3f by a scalar."
  [v s]
  (let [vr (if (vec3? v) (Vector3f. v) (Vector4f. v))]
    (.scale vr (double s))
    vr))

(defn elmul
  "Multiply a Vector3f by a scalar."
  [v w]
  (let [vr (if (vec3? v) (Vector3f. v) (Vector4f. v))]             
    (.setX vr (double (* (.x w) (.x v))))
    (.setY vr (double (* (.y w) (.y v))))
    (.setZ vr (double (* (.z w) (.z v))))
    vr))

(defn dot
  "Dot product of 2 vectors."
  [v1 v2]
  (if (vec3? v1) 
    (Vector3f/dot v1 v2) 
    (Vector4f/dot v1 v2))) 

(defn length
  "Return the length of a vector."
  [v]
  (.length v))

(defn cross
  "Cross product of vectors."
  [v1 v2]  
  (Vector3f/cross v1 v2 nil))

(defn normalize
  "Normalize a vector."
  [v]
  (let [nv (if (vec3? v) (Vector3f. v) (Vector4f. v))]          
    (when-not (zero? (length v))
      (.normalise nv))
    nv))

(defn map-vec3
  "Map over a vec3"
  [f v]
  (vec3 (f (.x v)) (f (.y v)) (f (.z v))))

(defn map-vec4
  "Map over a vec4"
  [f v]
  (vec4 (f (.x v)) (f (.y v)) (f (.z v)) (f (.w v))))

(defn vec3-to-seq
  "Quick hacks for seq-ing vectors."
  [v]
  [(.x v) (.y v) (.z v)])

(defn vec4-to-seq
  "Quick hacks for seq-ing vectors."
  [v]
  [(.x v) (.y v) (.z v) (.w v)])

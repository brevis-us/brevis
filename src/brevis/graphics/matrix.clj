(ns brevis.graphics.matrix
  (:require [brevis.vector :as vector]
            [brevis.graphics.matrix :as m]
            [brevis-utils.math.core :as math])
  (:import [org.lwjgl.util.vector Vector3f Vector4f Matrix4f]
           [java.nio FloatBuffer]
           [org.lwjgl BufferUtils]
           #_[org.ojalgo.matrix BasicMatrix PrimitiveMatrix]
           #_[brevis.math MatrixUtils]))

(defonce x-axis (vector/vec3 1 0 0))
(defonce y-axis (vector/vec3 0 1 0))
(defonce z-axis (vector/vec3 0 0 1))
(defonce origin (vector/vec4 0 0 0 1))

(defn mat4
   "Create a 4x4 matrix."  
   ([^java.nio.FloatBuffer float-buffer]     
     (let [mat (Matrix4f.)]
       (.load mat float-buffer)))
   ([ v0 v1 v2 v3 v4 v5 v6 v7 v8 v9 v10 v11 v12 v13 v14 v15 ]
     (mat4 (FloatBuffer/wrap (float-array [v0 v1 v2 v3 v4 v5 v6 v7 v8 v9 v10 v11 v12 v13 v14 v15]))))
   ([]
     (mat4 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0)))

#_(defonce identity4 (.setIdentity (mat4)))

(defn position
  "Return the position of xform."
  [^Matrix4f xform]
  (let [position (.transform xform origin (vector/vec4 0 0 0 0))]
    position))

(defn translate
  "Translate xform by vector"
  [^Matrix4f xform ^Vector4f v]
  (.setTranslation xform (vector/add v (position xform)))
  xform)

(defn rotate
  "Rotate xform about axis by angle."
  [^Matrix4f xform ^Vector3f axis ^double angle]
  (.rotate xform angle axis)
  xform) 
    
(defn rotate-x
  "Rotate xform about x-axis by angle."
  [^Matrix4f xform ^double angle]
  (rotate xform x-axis angle)
  xform)

(defn rotate-y
  "rotate xform about y-axis by angle."
  [^Matrix4f xform ^double angle]
  (rotate xform y-axis angle)
  xform)

(defn rotate-z
  "Rotate xform about z-axis by angle."
  [^Matrix4f xform ^double angle]
  (rotate xform z-axis angle)
  xform)
  
(defn transform
  "Transform a vector."
  [^Matrix4f xform ^Vector4f v]
  (let [outv (vector/vec4 0 0 0 0)]
    (Matrix4f/transform xform v outv)
    outv))

(defn identity-mat4
  "Convert a matrix to an identity matrix."
  [^Matrix4f m]
  (.setIdentity m)
  m)
(def identity-mat identity-mat4); deprecation in progress

;; derived from Cantor's rotation-matrix
(defn rotation-matrix
   "Returns a matrix which 3-D vectors about
the specified axis."
   ([theta x y z]
     (let [theta (math/radians theta)
           s (Math/sin theta)
           c (Math/cos theta)
           t (- 1 c)]       
       (mat4
         (+ c (* t x x)) (- (* t x y) (* s z)) (+ (* t x z) (* s y)) 0
         (+ (* t x y) (* s z)) (+ (* t y y) c) (- (* t y z) (* s x)) 0
         (- (* t x z) (* s y)) (+ (* t y z) (* s x)) (+ (* t z z) c) 0
         0 0 0 1))))

(defn normal-to-rotation-matrix
  "Convert a normal vector, vector as a sequence"
  [normal]
  (let [imin (reduce #(if (< (java.lang.Math/abs (nth normal %1)) (java.lang.Math/abs (nth normal %2))) %1 %2) (range 3))
        dt (nth normal imin)
        v2 (let [tmp [0 0 0]] 
             (assoc tmp imin 1) 
             (map #(* dt %) tmp))
        v3 (vector/vec3-to-seq (vector/cross (apply vector/vec3 normal) (apply vector/vec3 v2)))]
    (m/mat4 (first normal) (first v2) (first v3) 0
            (second normal) (second v2) (second v3) 0
            (last normal) (last v2) (last v3) 0
            0 0 0 1)))    

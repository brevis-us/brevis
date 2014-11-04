(ns brevis.matrix
  (:use [brevis math])
  (:require [brevis.vector :as vector])
  (:import [org.lwjgl.util.vector Vector3f Vector4f Matrix4f]
           [java.nio FloatBuffer]
           [org.lwjgl BufferUtils]
           [org.ejml.data DenseMatrix64F]
           [org.ejml.ops CommonOps]
           [org.ejml.alg.dense.mult MatrixVectorMult])
  #_(:import [javax.vecmath Vector3d Vector4d Matrix4d]))

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
     (let [theta (radians theta)
           s (Math/sin theta)
           c (Math/cos theta)
           t (- 1 c)]       
       (mat4
         (+ c (* t x x)) (- (* t x y) (* s z)) (+ (* t x z) (* s y)) 0
         (+ (* t x y) (* s z)) (+ (* t y y) c) (- (* t y z) (* s x)) 0
         (- (* t x z) (* s y)) (+ (* t y z) (* s x)) (+ (* t z z) c) 0
         0 0 0 1))))

;;;;;; EJML 

(defn matrix
  "Make a matrix."
  ([^Long length]
    (DenseMatrix64F. length))
  ([^Long rows ^Long cols]
    (DenseMatrix64F. rows cols)))
    

(defn matrix-mult
  "Matrix multiply from EJML"
  [m1 m2]
  nil)

(defn matrix-vector-mult
  "Multiply a matrix by a vector."
  [m v]
  (let [outv (DenseMatrix64F. (.getNumElements v) 1)]
    (MatrixVectorMult/mult m v outv)
    outv))

(defn sub
  "Subtract one matrix from another."
  [m1 m2]
  (let [outm (DenseMatrix64F. m1)]
    (CommonOps/subEquals outm m2)
    outm))

(defn sum
  "Sum of all elements in a matrix."
  [m]
  (CommonOps/elementSum m))

(defn sum-abs
  "Sum of all elements in a matrix."
  [m]
  (CommonOps/elementSumAbs m))

(defn add
  "Add one matrix to another."
  [^DenseMatrix64F m1 ^DenseMatrix64F m2]
  (let [outm (DenseMatrix64F. m1)]
    (CommonOps/addEquals outm m2)
    outm))

(defn positive-only
  "Return a matrix with only positive entries."
  [mat]
  (let [outm (DenseMatrix64F. mat)]
    (doseq [i (range (.getNumRows outm))
            j (range (.getNumCols outm))]    
      (when (neg? (.unsafe_get ^DenseMatrix64F outm i j))
        (.unsafe_set ^DenseMatrix64F outm i j 0)))
    outm))

(defn det
  "Return the determinant of a matrix."
  [m]
  (CommonOps/det ^DenseMatrix64F m))

(defn element-max
  "Return the maximum value in the matrix."
  [m]
  (CommonOps/elementMax ^DenseMatrix64F m))

(defn element-absmax
  "Return the maximum absolute value in the matrix."
  [m]
  (CommonOps/elementMaxAbs ^DenseMatrix64F m))

(defn element-min
  "Return the minimum value in the matrix."
  [m]
  (CommonOps/elementMin ^DenseMatrix64F m))

(defn element-absmin
  "Return the minimum absolute value in the matrix."
  [m]
  (CommonOps/elementMinAbs ^DenseMatrix64F m))

(defn fill
  "Fill a matrix with a single value."
  [m val]
  (let [outm (DenseMatrix64F. m)]
    (CommonOps/fill outm val)
    outm))

(defn identity-matrix
  "Return an identity matrix."
  ([w]
    (CommonOps/identity w))
  ([r c]
    (CommonOps/identity r c)))

(defn invert
  "Invert a matrix. Returns nil if m cannot be inverted."
  [m]
  (let [outm (DenseMatrix64F. m)]
    (when (CommonOps/invert outm)
      outm)))

(defn scale
  "Scale a matrix."
  [m s]
  (let [outm (DenseMatrix64F. m)]
    (CommonOps/scale s outm)
    outm))

(defn trace
  "Return the trace of a matrix."
  [m]
  (CommonOps/trace m))

(defn transpose
  "Transpose a matrix."
  [m]
  (let [outm (DenseMatrix64F. m)]
    (CommonOps/transpose outm)
    outm))


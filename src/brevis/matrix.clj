(ns brevis.matrix
  (:use [brevis math])
  (:require [brevis.vector :as vector])
  (:import [org.lwjgl.util.vector Vector3f Vector4f Matrix4f]
           [java.nio FloatBuffer]
           [org.lwjgl BufferUtils]
           #_[org.ejml.data DenseMatrix64F]
           #_[org.ejml.ops CommonOps]
           #_[org.ejml.alg.dense.mult MatrixVectorMult]
           [org.ojalgo.matrix BasicMatrix PrimitiveMatrix]
           [brevis.math MatrixUtils]
           )
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
    (matrix length length))
  ([^Long rows ^Long cols]
    (MatrixUtils/makeMatrix2D rows cols)))
    
(defn identity-matrix
  "Return an identity matrix."
  ([w]
    (MatrixUtils/identity w w))
  ([r c]
    (MatrixUtils/identity r c)))

(defn matrix-mult
  "Matrix multiply"
  [^BasicMatrix m1 ^BasicMatrix m2]
  (.multiplyRight m1 m2)); this needs to change to .multiply when switching to v38

(defn elmult
  "Matrix multiply by elements"
  [^BasicMatrix m1 ^BasicMatrix m2]
  (.multiplyElements m1 m2))

#_(defn matrix-vector-mult
   "Multiply a matrix by a vector."
   [m v]
   (let [outv (DenseMatrix64F. (.getNumElements v) 1)]
     (MatrixVectorMult/mult m v outv)
     outv))

(defn sub
  "Subtract one matrix from another."
  [^BasicMatrix m1 ^BasicMatrix m2]
  (.subtract m1 m2))

#_(defn sum
   "Sum of all elements in a matrix."
   [m]
   (CommonOps/elementSum m))

#_(defn sum-abs
   "Sum of all elements in a matrix."
   [m]
   (CommonOps/elementSumAbs m))

(defn add
  "Add one matrix to another."
  [^BasicMatrix m1 ^BasicMatrix m2]
  (.add m1 m2))

#_(defn positive-only
   "Return a matrix with only positive entries."
   [mat]
   (let [outm (DenseMatrix64F. mat)]
     (doseq [i (range (.getNumRows outm))
             j (range (.getNumCols outm))]    
       (when (neg? (.unsafe_get ^DenseMatrix64F outm i j))
         (.unsafe_set ^DenseMatrix64F outm i j 0)))
     outm))

(defn determinant
  "Return the determinant of a matrix."
  [^BasicMatrix m]
  (.getDeterminant m))

(defn element-max
  "Return the maximum value in the matrix."
  [^BasicMatrix m]
  (.getOperatorNorm m))

(defn eigenvalues
  "Return the eigenvalues of a matrix."
  [^BasicMatrix m]
  (.getEigenvalues m))

(defn matrix-rank
  "Return the matrix's rank."
  [^BasicMatrix m]
  (.getRank m))

#_(defn element-absmax
   "Return the maximum absolute value in the matrix."
   [m]
   (CommonOps/elementMaxAbs ^DenseMatrix64F m))

#_(defn element-min
   "Return the minimum value in the matrix."
   [m]
   (CommonOps/elementMin ^DenseMatrix64F m))

#_(defn element-absmin
   "Return the minimum absolute value in the matrix."
   [m]
   (CommonOps/elementMinAbs ^DenseMatrix64F m))

#_(defn fill
   "Fill a matrix with a single value."
   [m val]
   (let [outm (DenseMatrix64F. m)]
     (CommonOps/fill outm val)
     outm))


(defn invert
  "Invert a matrix. Returns nil if m cannot be inverted."
  [^BasicMatrix m]
  (.invert m))

(defn scale
  "Scale a matrix."
  [^BasicMatrix m s]
  (.multiply m s))

(defn trace
  "Return the trace of a matrix."
  [^BasicMatrix m]
  (.getTrace m))

(defn transpose
  "Transpose a matrix."
  [^BasicMatrix m]
  (.transpose m))

(defn set-element
  "Set the element of a matrix at row r column c with value val. This is slow, and might have problems with really large values."
  [^BasicMatrix m r c val]
  (.add m r c (- val (.get m r c))))

(defn get-element
  "Return the element at r c."
  [^BasicMatrix m r c]
  (.get m r c))

(defn matrix-to-seq
  "Return a flat seq of a matrix."
  [^BasicMatrix m]
  (seq (.toListOfElements m)))

(defn seq-to-matrix
 "Return a matrix"
 [r c coll]
 (MatrixUtils/collectionToMatrix r c coll))

(defn divide
  "Divide by a scalar."
  [^BasicMatrix m d]
  (.divide m d))

(defn eldivide
  "Elementwise division."
  [^BasicMatrix m1 ^BasicMatrix m2]
  (.divideElements m1 m2))

(defn matrix-width
  "Return the number of cols in a matrix."
  [^BasicMatrix m]
  (.countColumns m))

(defn matrix-height
  "Return the number of rows in a matrix."
  [^BasicMatrix m]
  (.countRows m))

(defn count-matrix
  "Return the number of elements in a matrix."
  [^BasicMatrix m]
  (.count m))

(defn frobenius-norm
  "Return the Frobenius norm. The Frobenius norm is the square root of the sum of the squares of each element, or the square root of the sum of the square of the singular values."
  [^BasicMatrix m]
  (.getFrobeniusNorm m))

(defn matrix-map
  "Map a function over a matrix."
  [f mat]
  (seq-to-matrix (matrix-height mat) (matrix-width mat) (map f (matrix-to-seq mat))))



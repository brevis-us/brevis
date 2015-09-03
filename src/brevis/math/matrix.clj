(ns brevis.math.matrix
  (:use [brevis math])
  (:require [brevis.vector :as vector])
  (:import [org.ojalgo.matrix BasicMatrix PrimitiveMatrix]
           [brevis.math MatrixUtils]
           )
  #_(:import [javax.vecmath Vector3d Vector4d Matrix4d]))

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

(defn matrix-pmap
  "Map a function over a matrix in parallel."
  [f mat]
  (seq-to-matrix (matrix-height mat) (matrix-width mat) (pmap f (matrix-to-seq mat))))

(defn select-columns 
  "Select the columns by index. Takes a sequence of column indices starting with 0 to numColumns - 1"
  [^BasicMatrix mat indices]
  (.selectColumns mat (int-array indices)))

(defn select-rows
  "Select the rows by index. Takes a sequence of row indices starting with 0 to numRows - 1"
  [^BasicMatrix mat indices]
  (.selectRows mat (int-array indices)))

(defn merge-columns
  "Appends input matrix to the bottom of target. This is destructive"
  [^BasicMatrix target ^BasicMatrix input]
  (.mergeColumns target input))

(defn merge-rows
  "Appends input matrix to the right of target. This is destructive"
  [^BasicMatrix target ^BasicMatrix input]
  (.mergeRows target input))

(defn sum-rows
  "sums all rows, returns 1 row"
  [mat]
  (let [height (matrix-height mat)
        width (matrix-width mat)]
    (seq-to-matrix 1 width (map #(apply + %) (partition height (matrix-to-seq mat))))))

(defn sum-columns
  "sums all columns, returns 1 column"
  [mat]
  (let [height (matrix-height mat)
        width (matrix-width mat)]
    (seq-to-matrix height 1 (map #(apply + %) (partition width (matrix-to-seq (transpose mat)))))))

(defn sum-matrix
  "sums all elements of a matrix, returns clojure float"
  [mat]
  (apply + (matrix-to-seq mat)))
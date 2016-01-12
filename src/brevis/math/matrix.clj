(ns brevis.math.matrix
  (:use [brevis math])
  (:require [brevis.vector :as vector]
            [clojure.core.matrix :as mat]
            [clojure.core.matrix.operators :as matops]
            [clojure.core.matrix.linear :as linear]))

(mat/set-current-implementation :vectorz); we "hard code" this implementation because of solve

(defn matrix
  "Make a matrix."
  ([^Long length]
    (matrix length length))
  ([^Long rows ^Long cols]
    (mat/new-matrix rows cols)))
    
#_(defn identity-matrix
   "Return an identity matrix."
   ([w]
     (MatrixUtils/identity w w))
   ([r c]
     (MatrixUtils/identity r c))); deprecated to favor core.matrix version

(def identity-matrix mat/identity-matrix)

(defn matrix-mult
  "Matrix multiply"
  [m1 m2]
  (mat/mmul m1 m2)); switched from .multiplyRight when switching to v38

(defn elmult
  "Matrix multiply by elements"
  [m1 m2]
  (matops/* m1 m2))

(defn sub
  "Subtract one matrix from another."
  [m1 m2]
  (matops/- m1 m2))

(defn sum
  "Sum of all elements in a matrix."
  [m]
  (mat/esum m)
  #_(CommonOps/elementSum m))

#_(defn sum-abs
   "Sum of all elements in a matrix."
   [m]
   (CommonOps/elementSumAbs m))

(defn add
  "Add one matrix to another."
  [ m1  m2]
  (matops/+ m1 m2))

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
  [ m]
  (mat/det m))

(defn element-max
  "Return the maximum value in the matrix."
  [ m]
  (mat/emax m))

(defn eigenvalues
  "Return the eigenvalues of a matrix."
  [ m]
  (mat/main-diagonal (:A (linear/eigen m {:return [:A]}))))

(defn matrix-rank
  "Return the matrix's rank."
  [ m]
  (linear/rank m))

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
  [ m]
  (mat/inverse m)
  #_(.invert m))

(defn scale
  "Scale a matrix."
  [ m s]
  (mat/mul m s)
  #_(.multiply m s))

(defn trace
  "Return the trace of a matrix."
  [ m]
  (mat/trace m)
  #_(.getTrace m))

(defn transpose
  "Transpose a matrix."
  [ m]
  (mat/transpose m)
  #_(.transpose m))

(defn set-element
  "Set the element of a matrix at row r column c with value val. This is slow, and might have problems with really large values."
  [ m r c val]
  (mat/mset m r c val)
  #_(.add m r c (- val (.get m r c))))

(defn get-element
  "Return the element at r c."
  [ m r c]
  (mat/mget m r c)
  #_(.get m r c))

(defn matrix-to-seq
  "Return a flat seq of a matrix."
  [ m]
  (mat/as-vector m)
  #_(seq (.toListOfElements m)))

(defn seq-to-matrix
 "Return a matrix. Collection will be read in order of top-down, left-right (i.e. coll is read as column by column)"
 [r c coll]
 (mat/reshape (mat/matrix coll) [r c])
 #_(MatrixUtils/collectionToMatrix r c coll))

(defn divide
  "Divide by a scalar."
  [ m d]
  (mat/div m d)
  #_(.divide m d))

(defn eldivide
  "Elementwise division."
  [ m1  m2]
  (divide m1 m2)
  #_(.divideElements m1 m2))

(defn matrix-width
  "Return the number of cols in a matrix."
  [ m]
  (first (mat/shape m))
  #_(.countColumns m))

(defn matrix-height
  "Return the number of rows in a matrix."
  [ m]
  (second (mat/shape m))
  #_(.countRows m))

(defn count-matrix
  "Return the number of elements in a matrix."
  [ m]
  (mat/ecount m)
  #_(.count m))

(defn frobenius-norm
  "Return the Frobenius norm. The Frobenius norm is the square root of the sum of the squares of each element, or the square root of the sum of the square of the singular values."
  [ m]
  (linear/norm m)
  #_(.getFrobeniusNorm m))

(defn matrix-map
  "Map a function over a matrix."
  [f m]
  (mat/emap f m)
  #_(seq-to-matrix (matrix-height mat) (matrix-width mat) (map f (matrix-to-seq mat))))

(defn matrix-pmap
  "Map a function over a matrix in parallel."
  [f m]
  (matrix-map f m)
  #_(seq-to-matrix (matrix-height mat) (matrix-width mat) (pmap f (matrix-to-seq mat))))

(defn select-columns 
  "Select the columns by index. Takes a sequence of column indices starting with 0 to numColumns - 1"
  [ m indices]
  ; could probably use views
  (keep-indexed #(when ((set indices) %1) %2) 
                (mat/columns m))
  #_(.selectColumns mat (int-array indices)))

(defn select-rows
  "Select the rows by index. Takes a sequence of row indices starting with 0 to numRows - 1"
  [ m indices]
  (keep-indexed #(when ((set indices) %1) %2) 
                (mat/rows m))
  #_(.selectRows mat (int-array indices)))

#_(defn merge-columns
   "Appends input matrix to the bottom of target. This is destructive"
   [ target  input]
   (.mergeColumns target input))

#_(defn merge-rows
   "Appends input matrix to the right of target. This is destructive"
   [ target  input]
   (.mergeRows target input))

(defn sum-matrix
  "sums all elements of a matrix, returns clojure float"
  [m]
  (mat/esum m)
  #_(apply + (matrix-to-seq mat)))

(defn sum-rows
  "sums all rows, returns 1 row"
  [m]
  (map sum-matrix (mat/rows m))
  #_(let [height (matrix-height mat)
         width (matrix-width mat)]
     (seq-to-matrix 1 width (map #(apply + %) (partition height (matrix-to-seq mat))))))

(defn sum-columns
  "sums all columns, returns 1 column"
  [m]
  (map sum-matrix (mat/columns m))
  #_(let [height (matrix-height mat)
          width (matrix-width mat)]
      (seq-to-matrix height 1 (map #(apply + %) (partition width (matrix-to-seq (transpose mat)))))))

;; Solving

(defn linear-solve
  "Solve a linear system."
  [lhs rhs]
  (linear/solve lhs rhs)
  #_(.solve lhs rhs))

;; Decompositions

(defn singular-value-decomposition
  "From ojAlgo:
   Singular Value: [A] = [Q1][D][Q2]T Decomposes [A] into [Q1], [D] and [Q2] where:
[Q1] is an orthogonal matrix. The columns are the left, orthonormal, singular vectors of [this]. Its columns are the eigenvectors of [A][A]T, and therefore has the same number of rows as [this].
[D] is a diagonal matrix. The elements on the diagonal are the singular values of [this]. It is either square or has the same dimensions as [this]. The singular values of [this] are the square roots of the nonzero eigenvalues of [A][A]T and [A]T[A] (they are the same)
[Q2] is an orthogonal matrix. The columns are the right, orthonormal, singular vectors of [this]. Its columns are the eigenvectors of [A][A]T, and therefore has the same number of rows as [this] has columns.
[this] = [Q1][D][Q2]T
A singular values decomposition always exists."
  [A]
  (linear/svd A)
  #_(let [sv (org.ojalgo.matrix.decomposition.SingularValue/make A)
         success? (.decompose sv A)]
     (when success?
       {:condition (.getCondition sv)
        :D (.getD sv)
        :frobenius-norm (.getFrobeniusNorm sv)
        ;:ky-fan-norm-fn #(.getKyFanNorm sv %)
        :operator-norm (.getOperatorNorm sv)
        :Q1 (.getQ1 sv)
        :Q2 (.getQ2 sv)
        :rank (.getRank sv)
        :singular-values (.getSingularValues sv)
        :trace-norm (.getTraceNorm sv)
        :ordered? (.isOrdered sv)})))

#_(let [m (seq-to-matrix 4 5 [1 0 0 0  0 0 0 4  0 3 0 0  0 0 0 0  2 0 0 0])]
   (singular-value-decomposition m)); example matrix from wikipedia svd
  

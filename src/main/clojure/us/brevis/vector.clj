(ns us.brevis.vector
  (:use [brevis-utils.math.core])
  (:import (org.joml Vector4f)
           (sc.iview.vector JOMLVector3 Vector3)))

; Perhaps these could be multimethods

(defn vec3
  "Make a Vector3"
  [^double x ^double y ^double z]
  (JOMLVector3. x y z))

(defn vec3?
  "Test if this is a vec3."
  [v]
  (= (class v) Vector3))

(defn vec4
  "Make a Vector4f"
  [^double x ^double y ^double z ^double w]
  (Vector4f. x y z w))

(defn vec4?
  "Test if this is a vec4."
  [v]
  (= (class v) Vector4f))

(defn vec4-to-vec3
  "convert a vec4 to a vec3"
  [^Vector4f v]
  (vec3 (.x v) (.y v) (.z v)))

(defn vec3-to-vec4
  "Convert a vec3 to a vec4 by padding the 4th dim with 1."
  [^Vector3 v]
  (vec4 (.xf v) (.yf v) (.zf v) 1))

(defn sub-vec3
   "Wrap's Vector3 minus. Should be a little faster than normal sub"
   [^Vector3 v1 ^Vector3 v2]
  (.minus v1 v2))

(defn sub-vec4
   "Wrap's Vector4f sub."
   [^Vector4f v1 ^Vector4f v2]
  (.sub v1 v2))

(defn sub
   "Subtract 2 vectors."
   [v1 v2]
   (if (vec3? v1)
     (sub-vec3 v1 v2)
     (sub-vec4 v1 v2)))

(defn div-vec3
  "Divide a Vector3 by a scalar."
  [^Vector3 v s]
  (.multiply v ^float (float (/ s))))

(defn div-vec4
  "Divide a Vector4f by a scalar."
  [^Vector4f v s]
  (.mul v ^float (float (/ s))))

(defn div
  "Divide a vector by a scalar."
  [v s]
  (if (vec3? v)
    (div-vec3 v s)
    (div-vec4 v s)))

(defn add-vec3
  "Add Vector3's"
  ([]
   (vec3 0 0 0))
  ([v]
   v)
  ([v1 v2]
   (.add ^Vector3 v1 ^Vector3 v2))
  ([v1 v2 & vs]
   (loop [vs vs
           v (add-vec3 v1 v2)]
      (if (empty? vs)
        v
        (recur (rest vs)
               (add-vec3 v (first vs)))))))

(defn add-vec4
  "Add Vector4f's"
   ([]
    (vec4 0 0 0 0))
  ([v]
   v)
  ([v1 v2]
   (.add ^Vector4f v1 ^Vector4f v2))
  ([v1 v2 & vs]
   (loop [vs vs
           v (add-vec4 v1 v2)]
      (if (empty? vs)
        v
        (recur (rest vs)
               (add-vec4 v (first vs)))))))

(defn add
  "Add vectors"
  ([v]
   v)
  ([v1 v2]
   (if (vec3? v1)
      (add-vec3 v1 v2)
      (add-vec4 v1 v2)))
  ([v1 v2 & vs]
   (loop [vs vs
           v (add v1 v2)]
      (if (empty? vs)
        v
        (recur (rest vs)
               (add v (first vs)))))))

(defn mul-vec3
  "Multiply a Vector3 by a scalar."
  [^Vector3 v s]
  (.multiply v ^float (float s)))

(defn mul-vec4
  "Multiply a Vector4f by a scalar."
  [^Vector4f v s]
  (.mul v ^float (float s)))

(defn mul
  "Multiply a Vector3 by a scalar."
  [v s]
  (if (vec3? v)
    (mul-vec3 v s)
    (mul-vec4 v s)))

(defn elmul-vec3
  [^Vector3 v ^Vector3 w]
  (.elmul v w))

(defn elmul-vec4
  "Multiply a Vector3 by a scalar."
  [^Vector4f v ^Vector4f w]
  (.mul v w))

(defn elmul
  "Multiply a vector by a scalar."
  [v w]
  (if (vec3? v)
    (elmul-vec3 v w)
    (elmul-vec4 v w)))

(defn dot-vec3
  "Dot product of 2 vectors."
  [^Vector3 v1 ^Vector3 v2]
  (.dot v1 v2))

(defn dot-vec4
  "Dot product of 2 vectors."
  [v1 v2]
  (.dot ^Vector4f v1 ^Vector4f v2))

(defn dot
  "Dot product of 2 vectors."
  [v1 v2]
  (if (vec3? v1)
    (dot-vec3 v1 v2)
    (dot-vec4 v1 v2)))

(defn length-vec3
  "Return the length of a vector."
  [^Vector3 v]
  (.getLength v))

(defn length-vec4
  "Return the length of a vector."
  [^Vector4f v]
  (.length v))

(defn length
  "Return the length of a vector."
  [v]
  (if (vec3? v)
    (length-vec3 v)
    (length-vec4 v)))

(defn cross-vec3
  "Cross product of vectors."
  [^Vector3 v1 ^Vector3 v2]
  (.cross v1 v2))

(defn normalize-vec3
  "Normalize a vector."
  [^Vector3 v]
  (if-not (zero? (length-vec3 v))
    (.normalize v)
    (.copy v)))

(defn normalize-vec4
  "Normalize a vector."
  [^Vector4f v]
  (let [nv (Vector4f. v)]          
    (if-not (zero? (length-vec4 v))
      (.normalize nv)
      nv)))

(defn normalize
  "Normalize a vector."
  [v]
  (if (vec3? v)
    (normalize-vec3 v)
    (normalize-vec4 v)))

(defn map-vec3
  "Map over a vec3"
  [f ^Vector3 v]
  (vec3 (f (.xf v)) (f (.yf v)) (f (.zf v))))

(defn map-vec4
  "Map over a vec4"
  [f ^Vector4f v]
  (vec4 (f (.x v)) (f (.y v)) (f (.z v)) (f (.w v))))

(defn vec3-to-seq
  "Quick hacks for seq-ing vectors."
  [^Vector3 v]
  [(.xf v) (.yf v) (.zf v)])

(defn vec4-to-seq
  "Quick hacks for seq-ing vectors."
  [^Vector4f v]
  [(.x v) (.y v) (.z v) (.w v)])

(defn x-val-vec3
  "Return the x-value of a vector."
  [v]
  (.xf ^Vector3 v))

(defn x-val-vec4
  "Return the x-value of a vector."
  [v]
  (.x ^Vector4f v))

(defn x-val
  "Return the x-value of a vector."
  [v]
  (if (vec3? v)
    (x-val-vec3 v)
    (x-val-vec4 v)))

(defn y-val-vec3
  "Return the y-value of a vector."
  [v]
  (.yf ^Vector3 v))

(defn y-val-vec4
  "Return the y-value of a vector."
  [v]
  (.y ^Vector4f v))

(defn y-val
  "Return the y-value of a vector."
  [v]
  (if (vec3? v)
    (y-val-vec3 v)
    (y-val-vec4 v)))

(defn z-val-vec3
  "Return the z-value of a vector."
  [v]
  (.zf ^Vector3 v))

(defn z-val-vec4
  "Return the z-value of a vector."
  [v]
  (.z ^Vector4f v))

(defn z-val
  "Return the z-value of a vector."
  [v]
  (if (vec3? v)
    (z-val-vec3 v)
    (z-val-vec4 v)))

(ns brevis.vector
  #_(:import [org.ejml.data DenseMatrix64F]
           [org.ejml.ops CommonOps])
  (:use [brevis.math])
  (:import [org.lwjgl.util.vector Vector3f Vector4f]))

(defn vec3
  "Make a Vector3f"
  [^double x ^double y ^double z]
  (Vector3f. x y z))

(defn vec3?
  "Test if this is a vec3."
  [v]
  (= (class v) org.lwjgl.util.vector.Vector3f))

(defn vec4
  "Make a Vector4f"
  [^double x ^double y ^double z ^double w]
  (Vector4f. x y z w))

(defn vec4-to-vec3
  "convert a vec4 to a vec3"
  [^Vector3f v]
  (vec3 (.x v) (.y v) (.z v)))

(defn vec3-to-vec4
  "Convert a vec3 to a vec4 by padding the 4th dim with 1."
  [^Vector4f v]
  (vec4 (.x v) (.y v) (.z v) 1))

(defn sub
   "Wrap's Vector3f sub."
   [v1 v2]
   #_((if (vec3? v1) Vector3f/sub Vector4f/sub)
     v1 v2 nil)
   (if (vec3? v1) 
     (Vector3f/sub ^Vector3f v1 ^Vector3f v2 nil)
     (Vector4f/sub ^Vector4f v1 ^Vector4f v2 nil)))

#_(defn sub
   "Wrap's Vector3f sub."
   ([^Vector3f v1 ^Vector3f v2] (Vector3f/sub v1 v2 nil))
   ([^Vector4f v1 ^Vector4f v2] (Vector4f/sub v1 v2 nil)))

(defn sub-vec3
   "Wrap's Vector3f sub. Should be a little faster than normal sub"
   [^Vector3f v1 ^Vector3f v2] (Vector3f/sub v1 v2 nil))

(defn sub-vec4
   "Wrap's Vector3f sub."
   [^Vector4f v1 ^Vector4f v2] (Vector4f/sub v1 v2 nil))

(defn div
  "Divide a vector by a scalar."
  [v s]
  (let [vr (if (vec3? v) ^Vector3f (Vector3f. v) ^Vector4f (Vector4f. v))]             
    (.scale vr (double (/ s)))
    vr))

(defn div-vec3
  "Divide a Vector3f by a scalar."
  [v s]
  (let [^Vector3f vr (Vector3f. v)]
    (.scale vr ^double (double (/ s)))
    vr))

(defn div-vec4
  "Divide a Vector4f by a scalar."
  [v s]
  (let [^Vector4f vr (Vector4f. v)]
    (.scale vr ^double (double (/ s)))
    vr))
    
(defn add
  "Add Vector3f's"
  ([v]
    v)
  ([v1 v2]
    (if (vec3? v1) 
      (Vector3f/add ^Vector3f v1 ^Vector3f v2 nil)
      (Vector4f/add ^Vector4f v1 ^Vector4f v2 nil)))
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
  (let [vr (if (vec3? v) ^Vector3f (Vector3f. v) ^Vector4f (Vector4f. v))]
    (.scale vr (double s))
    vr))

(defn mul-vec3
  "Multiply a Vector3f by a scalar."
  [v s]
  (let [^Vector3f vr (Vector3f. v)]
    (.scale vr ^double (double s))
    vr))

(defn mul-vec4
  "Multiply a Vector4f by a scalar."
  [v s]
  (let [^Vector4f vr (Vector4f. v)]
    (.scale vr ^double (double s))
    vr))

(defn elmul
  "Multiply a Vector3f by a scalar."
  [v w]
  (let [vr (if (vec3? v) ^Vector3f (Vector3f. v) ^Vector4f (Vector4f. v))]             
    (.setX vr (double (* (.x w) (.x v))))
    (.setY vr (double (* (.y w) (.y v))))
    (.setZ vr (double (* (.z w) (.z v))))
    vr))

(defn elmul-vec3
  "Multiply a Vector3f by a scalar."
  [^Vector3f v ^Vector3f w]
  (let [^Vector3f vr (Vector3f. v)]
    (.setX vr ^double (double (* (.x w) (.x v))))
    (.setY vr ^double (double (* (.y w) (.y v))))
    (.setZ vr ^double (double (* (.z w) (.z v))))
    vr))

(defn elmul-vec4
  "Multiply a Vector3f by a scalar."
  [^Vector4f v ^Vector4f w]
  (let [^Vector4f vr (Vector4f. v)]
    (.setX vr ^double (double (* (.x w) (.x v))))
    (.setY vr ^double (double (* (.y w) (.y v))))
    (.setZ vr ^double (double (* (.z w) (.z v))))
    vr))

(defn dot
  "Dot product of 2 vectors."
  [v1 v2]
  (if (vec3? v1) 
    (Vector3f/dot ^Vector3f v1 ^Vector3f v2) 
    (Vector4f/dot ^Vector4f v1 ^Vector4f v2))) 

(defn length
  "Return the length of a vector."
  [v] (.length v))

(defn length-vec3
  "Return the length of a vector."
  [^Vector3f v] (.length v))

(defn length-vec3
  "Return the length of a vector."
  [^Vector4f v] (.length v))  

(defn cross
  "Cross product of vectors."
  [^Vector3f v1 ^Vector3f v2]  
  (Vector3f/cross v1 v2 nil))

(defn normalize
  "Normalize a vector."
  [v]
  (let [nv (if (vec3? v) ^Vector3f (Vector3f. v) ^Vector4f (Vector4f. v))]          
    (when-not (zero? (length v))
      (.normalise nv))
    nv))

(defn normalize-vec3
  "Normalize a vector."
  [^Vector3f v]
  (let [nv (Vector3f. v)]          
    (when-not (zero? (length v))
      (.normalise nv))
    nv))

(defn normalize-vec4
  "Normalize a vector."
  [^Vector4f v]
  (let [nv (Vector4f. v)]          
    (when-not (zero? (length v))
      (.normalise nv))
    nv))

(defn map-vec3
  "Map over a vec3"
  [f ^Vector3f v]
  (vec3 (f (.x v)) (f (.y v)) (f (.z v))))

(defn map-vec4
  "Map over a vec4"
  [f ^Vector4f v]
  (vec4 (f (.x v)) (f (.y v)) (f (.z v)) (f (.w v))))

(defn vec3-to-seq
  "Quick hacks for seq-ing vectors."
  [^Vector3f v]
  [(.x v) (.y v) (.z v)])

(defn vec4-to-seq
  "Quick hacks for seq-ing vectors."
  [^Vector4f v]
  [(.x v) (.y v) (.z v) (.w v)])

(defn x-val
  "Return the x-value of a vector."
  [v]
  (if (vec3? v) (.x ^Vector3f v) (.x ^Vector4f v)))

(defn y-val
  "Return the y-value of a vector."
  [v]
  (if (vec3? v) (.y ^Vector3f v) (.y ^Vector4f v)))

(defn z-val
  "Return the z-value of a vector."
  [v]
  (if (vec3? v) (.z ^Vector3f v) (.z ^Vector4f v)))
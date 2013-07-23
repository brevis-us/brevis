(ns brevis.physics.vector
  (:import [javax.vecmath Vector3d Vector4d]))
;; Temporary way of making Java's Vector3d's look like Cantor's vec3's

(defn sub
  "Wrap's Vector3d sub."
  [v1 v2]
  (let [v (Vector3d.)]    
    (.sub v v1 v2)
    v))

(defn vec3
  "Make a Vector3d"
  [x y z]
  (Vector3d. x y z))

(defn vec4
  "Make a Vector4d"
  [x y z w]
  (Vector4d. x y z w))

(defn div
  "Divide a Vector3d by a scalar."
  [v s]
  (Vector3d. (double (/ (.x v) s))
             (double (/ (.y v) s))
             (double (/ (.z v) s))))
    
(defn add
  "Add 2 Vector3d's"
  ([v1 v2]
    (let [v (Vector3d.)]      
      (.add v v1 v2)
      v))
  ([v1 v2 & vs]
    (loop [vs vs
           v (add v1 v2)]
      (if (empty? vs)
        v
        (recur (rest vs)
               (add v (first vs)))))))

(defn mul
  "Multiply a Vector3d by a scalar."
  [v s]
  (Vector3d. (double (* s (.x v)))
             (double (* s (.y v)))
             (double (* s (.z v)))))

(defn dot
  "Dot product of 2 vectors."
  [v1 v2]
  (+ (* (.x v1) (.x v2)) 
     (* (.y v1) (.y v2)) 
     (* (.z v1) (.z v2)))) 

(defn length
  "Return the length of a vector."
  [v]
  (.length v))

(defn cross
  "Cross product of vectors."
  [v1 v2]
  (let [v (Vector3d.)]
    (.cross v v1 v2)))



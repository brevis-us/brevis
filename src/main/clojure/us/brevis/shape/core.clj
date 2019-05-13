(ns us.brevis.shape.core
  (:use [us.brevis vector])
  (:import (sc.iview.vector Vector3)
           (us.brevis BrObject BrShape)))

(defn compute-normal
  "Compute the normal for some vertices of an arbitrary polygon."
  [vertices]
  (loop [vidx (range (count vertices))
         ^Vector3 normal (vec3 0 0 0)]
    (if (empty? vidx)
      (div normal (length normal))
      ;(mul (div normal (length normal)) -1.0)
      (recur (rest vidx)
             (let [^Vector3 curr (nth vertices (first vidx))
                   ^Vector3 next (nth vertices (mod (first vidx) (count vertices)))]
               (vec3 (+ (.xf normal) (* (- (.yf curr) (.yf next))
                                        (+ (.zf curr) (.zf next))))
                     (+ (.yf normal) (* (- (.zf curr) (.zf next))
                                        (+ (.xf curr) (.xf next))))
                     (+ (.zf normal) (* (- (.xf curr) (.xf next))
                                        (+ (.yf curr) (.yf next))))))))))

(defn get-shape 
  "Return the shape of an object."
  [obj]
  (.getShape ^BrObject obj))

(defn resize-shape
  "Change the dimension of an object's shape."
  [obj new-dim]
  (.resize ^BrShape (.getShape ^BrObject obj) new-dim)
  obj)

(defn get-mesh
  "Return a shape's mesh."
  [shp]
  (.getMesh ^BrShape shp))

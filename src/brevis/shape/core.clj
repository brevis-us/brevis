(ns brevis.shape.core
  (:import [java.awt.image BufferedImage]
           [java.awt Color])
  (:use [brevis vector]))  

(defn compute-normal
  "Compute the normal for some vertices of an arbitrary polygon."
  [vertices]
  (loop [vidx (range (count vertices))
         ^Vector3f normal (vec3 0 0 0)]         
    (if (empty? vidx)
      (div normal (length normal))
      ;(mul (div normal (length normal)) -1.0)
      (recur (rest vidx)
             (let [^Vector3f curr (nth vertices (first vidx))
                   ^Vector3f next (nth vertices (mod (first vidx) (count vertices)))]
               (vec3 (+ (.x normal) (* (- (.y curr) (.y next))
                                       (+ (.z curr) (.z next))))
                     (+ (.y normal) (* (- (.z curr) (.z next))
                                       (+ (.x curr) (.x next))))
                     (+ (.z normal) (* (- (.x curr) (.x next))
                                       (+ (.y curr) (.y next))))))))))

(defn get-shape 
  "Return the shape of an object."
  [obj]
  (.getShape obj))

(defn resize-shape
  "Change the dimension of an object's shape."
  [obj new-dim]
  (.resize (.getShape obj) new-dim)
  obj)

(defn get-mesh
  "Return a shape's mesh."
  [shp]
  (.getMesh shp))

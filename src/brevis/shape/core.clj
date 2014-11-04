(ns brevis.shape.core
  (:import [java.awt.image BufferedImage]
           [java.awt Color])
  (:use ;[penumbra opengl compute]
        ;[penumbra.opengl core]
        [brevis vector])
  #_(:require #_[penumbra.data :as data]))  

(defn compute-normal
  "Compute the normal for some vertices of an arbitrary polygon."
  [vertices]
  (loop [vidx (range (count vertices))
         normal (vec3 0 0 0)]         
    (if (empty? vidx)
      (div normal (length normal))
      ;(mul (div normal (length normal)) -1.0)
      (recur (rest vidx)
             (let [curr (nth vertices (first vidx))
                   next (nth vertices (mod (first vidx) (count vertices)))]
               (vec3 (+ (.x normal) (* (- (.y curr) (.y next))
                                       (+ (.z curr) (.z next))))
                     (+ (.y normal) (* (- (.z curr) (.z next))
                                       (+ (.x curr) (.x next))))
                     (+ (.z normal) (* (- (.x curr) (.x next))
                                       (+ (.y curr) (.y next))))))))))

#_(defn- define-quads-display-list
   "Create a quads display list."
   [polys]
   (create-display-list
      (draw-quads
        (loop [rem polys]
          (apply normal (compute-normal (first rem)))
          (doseq [v (first rem)]
            (apply vertex v))))))

#_(defn- define-triangles-display-list
   "Create a tris display list."
   [polys]
   (create-display-list
      (draw-triangles
        (loop [rem polys]
          (apply normal (compute-normal (first rem)))
          (doseq [v (first rem)]
            (apply vertex v))))))

#_(defn define-display-list
   "Penumbra suffers from slowdowns if you do per-vertex shapes on the fly. Compute a display list beforehand and save some CPU."
   [poly-type polys]
   (cond
     (= :quads poly-type) (define-quads-display-list polys)
     (= :triangles poly-type) (define-triangles-display-list polys)))    

#_(defn resize-shape
  "Change the dimension of an object's shape."
  [obj new-dim]
  (assoc-in obj [:shape :dim] new-dim))

(defn get-shape 
  "Return the shape of an object."
  [obj]
  (.getShape obj))

(defn resize-shape
  "Change the dimension of an object's shape."
  [obj new-dim]
  (.resize (.getShape obj) new-dim)
  obj)

#_(defn textured-quad []
   (push-matrix
     (material :front-and-back)
     (translate -0.5 -0.5 0.5)
     (normal 0 0 -1)
     (draw-quads
       (texture 1 1) (vertex 1 1 0)
       (texture 0 1) (vertex 0 1 0)
       (texture 0 0) (vertex 0 0 0)
       (texture 1 0) (vertex 1 0 0))))

(defn xor [a b] (or (and a (not b)) (and (not a) b)))

(defn init-checkers []
  (def #^:dynamic *checkers* 
	  (let [tex (BufferedImage. 128 128 BufferedImage/TYPE_INT_ARGB)]
     (for [x (range 128) y (range 128)]       
       (.setRGB tex x y
         (.getRGB (if (xor (even? (bit-shift-right x 4)) (even? (bit-shift-right y 4)))
                    (Color. 0.9 0.9 0.9 1.0)
                    (Color. 0.8 0.8 0.8 1.0)))))
     tex)))	                

(defn get-mesh
  "Return a shape's mesh."
  [shp]
  (.getMesh shp))

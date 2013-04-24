(ns brevis.shape.sphere
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [brevis.shape.core]
        [cantor])) 

(defn create-sphere
  "Create a sphere object."
  ([]
     (create-sphere 1))
  ([radius]
     {:dim (vec3 radius radius radius)
      :type :sphere
      }))

;; currently mostly taken from ztellman's penumbra

(defn gen-sphere-vertices
  [lod]
  (for [theta (range 0 361 (/ 360 lod))]
    (for [phi (range -90 91 (/ 180 (/ lod 2)))]
      (cartesian (polar3 theta phi)))))

(defonce sphere-vertices (gen-sphere-vertices 25))

(defn init-sphere
  []
  (def sphere-mesh 
       (create-display-list (doseq [arcs (partition 2 1 sphere-vertices)]                              
                              (draw-quad-strip
                                (doseq [[a b] (map list (first arcs) (second arcs))]
                                  (normal (div a (length a)))
                                  (vertex a) (vertex b)))))))

;; figure out the vertices as we go
#_(defn draw-sphere
  []
  (doseq [arcs (partition 2 1 sphere-vertices)]
    (with-enabled :auto-normal
      (draw-quad-strip
        (doseq [[a b] (map list (first arcs) (second arcs))]
          (vertex a) (vertex b))))))

(defn draw-sphere
  []
  (sphere-mesh))


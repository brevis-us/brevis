(ns brevis.shape.cone
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [brevis.shape.core]
        [cantor])) 

(defn create-cone
  "Create a cone object."
  ([]
     (create-cone 1 1))
  ([length base]
     {:dim (vec3 (Math/max length base) (Math/max length base) (Math/max length base))
      :type :cone
      }))

(defn gen-cone-faces
  [length lod]
  (let [interval (/ 360 lod)
        base (range 0 361 interval)]
    (for [theta (rest base)]
      (let [p1 (Math/sin (- theta interval)) 
            p2 (Math/cos (- theta interval))]
        [(vec3 0 0 length) (vec3 p1 p2 0) (vec3 p2 p1 0)]))))

#_(defonce sphere-vertices (gen-sphere-vertices 50))

(defn init-cone
  []
  (def cone-mesh
    (define-display-list :triangles (gen-cone-faces 1 5))))

;; figure out the vertices as we go
#_(defn draw-sphere
  []
  (doseq [arcs (partition 2 1 sphere-vertices)]
    (with-enabled :auto-normal
      (draw-quad-strip
        (doseq [[a b] (map list (first arcs) (second arcs))]
          (vertex a) (vertex b))))))

(defn draw-cone
  []
  (cone-mesh))


(ns brevis.shape.sphere)

#_(defn sphere-vertices
  [lod]
  (for [theta (range 0 361 (/ 360 lod))]
    (for [phi (range -90 91 (/ 180 (/ lod 2)))]
      (cartesian (polar3 theta phi)))))

#_(defn draw-sphere
  [vertices]
  (doseq [arcs (partition 2 1 vertices)]
    (draw-quad-strip
     (doseq [[a b] (map list (first arcs) (second arcs))]
       (vertex a) (vertex b)))))

#_(defn init-sphere
  []
  (def sphere-mesh 
       (create-display-list (draw-sphere (sphere-vertices 12)))))

#_(defn draw-joint
  "Draw a joint with a red sphere."
  [j]
  (let [dvec (new DVector3)]
    (.getAnchor j dvec)
    (let [pos (vec3 (.get0 dvec) (.get1 dvec) (.get2 dvec))
          radius 0.2]
;      (println "joint at" pos)
      (push-matrix       
       (translate pos);(sub pos (vec3 radius radius radius)))
       (color (vec3 1 0 0))
       (scale (vec3 (* radius 2) (* radius 2) (* radius 2)))
       (call-display-list sphere-mesh)))))

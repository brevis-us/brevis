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

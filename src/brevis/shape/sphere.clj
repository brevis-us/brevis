(ns brevis.shape.sphere
  (:import [brevis BrShape])
  (:use ;[penumbra opengl compute]
        ;[penumbra.opengl core]
        [brevis.shape.core]
        [brevis.graphics multithread]
        [brevis vector])) 

(defn create-sphere
  "Create a sphere object."
  ([]
     (create-sphere 1))
  ([radius]
    (begin-with-graphics-thread)
    (let [sphere (BrShape/createSphere radius (:gui @brevis.globals/*gui-state*))]
      (.setDimension sphere (vec3 radius radius radius) (:gui @brevis.globals/*gui-state*))
      (end-with-graphics-thread)
      sphere)))

(ns brevis.shape.cone
  (:import [brevis BrShape])
  (:use ;[penumbra opengl compute]
        ;[penumbra.opengl core]
        [brevis vector]
        [brevis.graphics multithread]
        [brevis.shape.core])) 

(defn create-cone
  "Create a cone object."
  ([]
     (create-cone 1 1))
  ([length base]
    (begin-with-graphics-thread)
    (let [result (BrShape/createCone length base (:gui @brevis.globals/*gui-state*))]
      (end-with-graphics-thread)
      result)))
      

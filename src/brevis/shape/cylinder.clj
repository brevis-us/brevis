(ns brevis.shape.cylinder
  (:import [brevis BrShape])
  (:use [brevis vector]        
        [brevis.graphics multithread]
        [brevis.shape.core])) 

(defn create-cylinder
  "Create a cone object."
  ([]
     (create-cylinder 1 1))
  ([length radius]
    (begin-with-graphics-thread)
    (let [result (BrShape/createCylinder length radius (:gui @brevis.globals/*gui-state*))]
      (end-with-graphics-thread)
      result)))

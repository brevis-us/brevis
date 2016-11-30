(ns brevis.shape.cone
  (:import [brevis BrShape])
  (:require [brevis.parameters :as parameters])
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
    (let [result (BrShape/createCone length base (parameters/get-param :gui) #_(:gui @brevis.globals/*gui-state*))]
      (end-with-graphics-thread)
      result)))
      

(ns brevis.shape.sphere
  (:import [brevis BrShape])
  (:require [brevis.parameters :as parameters])
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
    (let [sphere (BrShape/createSphere radius (parameters/get-param :gui) )]
      (.setDimension sphere (vec3 radius radius radius) (parameters/get-param :gui) )
      (end-with-graphics-thread)
      sphere)))

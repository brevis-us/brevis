(ns brevis.shape.box
  (:import [brevis BrShape])
  (:require [brevis-utils.parameters :as parameters])
  (:use ;[penumbra opengl compute]
        ;[penumbra.opengl core]
        [brevis vector]
        [brevis.graphics multithread]
        [brevis.shape.core]))

(defn create-box
  "Create a box object."
  ([]
     (create-box 1 1 1))
  ([width height depth]
    (begin-with-graphics-thread)
    (let [box (BrShape/createBox width height depth (parameters/get-param :gui) )]
      (.setDimension box (vec3 width height depth) (parameters/get-param :gui) )
      (end-with-graphics-thread)
      box)))

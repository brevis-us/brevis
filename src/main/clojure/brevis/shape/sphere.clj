(ns brevis.shape.sphere
  (:import [brevis BrShape])
  (:require [brevis-utils.parameters :as parameters])
  (:use [brevis.shape.core]
        [brevis vector])) 

(defn create-sphere
  "Create a sphere object."
  ([]
     (create-sphere 1))
  ([radius]
    (let [sphere (BrShape/createSphere radius (parameters/get-param :gui) )]
      (.setDimension sphere (vec3 radius radius radius) (parameters/get-param :gui) )
      sphere)))

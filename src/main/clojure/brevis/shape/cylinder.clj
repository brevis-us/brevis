(ns brevis.shape.cylinder
  (:import [brevis BrShape])
  (:require [brevis-utils.parameters :as parameters])
  (:use [brevis vector]
        [brevis.shape.core])) 

(defn create-cylinder
  "Create a cone object."
  ([]
     (create-cylinder 1 1))
  ([length radius]
    (create-cylinder length radius radius))
  ([length radius1 radius2]
    (let [result (BrShape/createCylinder length radius1 radius2 (parameters/get-param :gui) )]
      result)))

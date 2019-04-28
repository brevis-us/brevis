(ns us.brevis.shape.cylinder
  (:import [us.brevis BrShape])
  (:require [brevis-utils.parameters :as parameters])
  (:use [us.brevis vector]
        [us.brevis.shape.core]))

(defn create-cylinder
  "Create a cone object."
  ([]
   (create-cylinder 1 1))
  ([length radius]
   (create-cylinder length radius radius))
  ([length radius1 radius2]
   (let [result (BrShape/createCylinder length radius1 radius2 (parameters/get-param :gui))]
      result)))

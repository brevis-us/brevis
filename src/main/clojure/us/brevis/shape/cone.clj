(ns us.brevis.shape.cone
  (:import [us.brevis BrShape])
  (:require [brevis-utils.parameters :as parameters])
  (:use [us.brevis vector]
        [us.brevis.shape.core]))

(defn create-cone
  "Create a cone object."
  ([]
   (create-cone 1 1))
  ([length base]
   (let [result (BrShape/createCone length base (parameters/get-param :gui))]
      result)))
      

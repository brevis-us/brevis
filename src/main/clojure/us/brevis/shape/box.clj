(ns us.brevis.shape.box
  (:import [us.brevis BrShape])
  (:require [brevis-utils.parameters :as parameters])
  (:use [us.brevis vector]
        [us.brevis.shape.core]))

(defn create-box
  "Create a box object."
  ([]
   (create-box 1 1 1))
  ([width height depth]
   (let [box (BrShape/createBox width height depth (parameters/get-param :gui))]
      (.setDimension box (vec3 width height depth) (parameters/get-param :gui))
      box)))

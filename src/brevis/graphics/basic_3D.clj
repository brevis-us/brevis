(ns brevis.graphics.basic-3D
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [cantor]
        [brevis.shape.box])
  (:require [penumbra.app :as app]
            [penumbra.text :as text]
            [penumbra.data :as data]
            [cantor.range]
            [penumbra.opengl.frame-buffer :as fb]))  

(defn draw-shape
  "Draw a shape. Call this after translating, scaling, and setting color."
  [obj]
  (push-matrix
   (apply color (:color obj))
   (translate (:position obj))
   (apply scale (:dim (:shape obj)))
   (rotate (.x (:velocity obj)) 1 0 0)
   (rotate (.y (:velocity obj)) 0 1 0)
   (rotate (.z (:velocity obj)) 0 0 1)
   (call-display-list (cond (= (:type (:shape obj)) :box) box-graphic))))

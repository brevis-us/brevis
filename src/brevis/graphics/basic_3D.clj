(ns brevis.graphics.basic-3D
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [cantor]
        [brevis.physics.space]
        [brevis.shape.box])
  (:require [penumbra.app :as app]
            [penumbra.text :as text]
            [penumbra.data :as data]
            [cantor.range]
            [penumbra.opengl.frame-buffer :as fb]))  

(defn draw-shape
  "Draw a shape. Call this after translating, scaling, and setting color."
  [obj]
  (let [pos (get-position obj)
        vel (get-velocity obj)
        col (:color obj)]
	  (push-matrix
	   #_(apply color (:color obj))
	   (material :front-and-back
	    :ambient-and-diffuse (into [] (conj col 1)); [1 1 1 1]
	    :specular            [0.5 0.4 0.4 1]
	    :shininess           64)
	   (translate pos)
	   (apply scale (:dim (:shape obj)))
	   (rotate (.x vel) 1 0 0)
	   (rotate (.y vel) 0 1 0)
	   (rotate (.z vel) 0 0 1)
	   (draw-textured-cube)
	   #_(call-display-list (cond (= (:type (:shape obj)) :box) box-graphic)))))

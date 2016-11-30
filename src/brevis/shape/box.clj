(ns brevis.shape.box
  (:import [brevis BrShape])
  (:require [brevis.parameters :as parameters])
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
      ;(println "create-box" (.getDimension box))
      (end-with-graphics-thread)
      box)))

#_(defn draw-textured-cube
   "This function is depricated, use the Java Basic3D class."
   []
   (dotimes [_ 4]
     (rotate 90 0 1 0)
     (textured-quad))
   (rotate 90 1 0 0)
   (textured-quad)
   (rotate 180 1 0 0)
   (textured-quad))

#_(defn init-box-graphic
   []
   (def box-mesh 
     (create-display-list 
       (draw-textured-cube))))

#_(defn draw-box
   []
   (box-mesh))

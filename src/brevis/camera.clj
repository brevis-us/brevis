(ns brevis.camera
  (:use [brevis globals]))

(defn set-current-camera-position
  [position]
  (.setPosition (:camera @brevis.globals/*gui-state*) position))

(defn set-current-camera-rotation
  [rotation]
  (.setRotation (:camera @brevis.globals/*gui-state*) rotation))

(defn get-camera-information
  []
  (.toString (:camera @brevis.globals/*gui-state*)))

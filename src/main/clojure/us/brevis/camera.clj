(ns us.brevis.camera
  (:require [us.brevis.globals :as globals]))

;; ## Camera

(defn move-from-look
  "Move a camera according to some dX, dY, dZ."
  [cam dx dy dz]
  (.moveFromLook cam dx dy dz))

(defn rotate-from-look
  "Rotate a camera according to some dR (roll), dP (pitch), dW (yaw)."
  [cam dr dp dw]
  (.rotateFromLook cam dr dp dw))

(defn set-current-camera-position
  [position]
  (.setPosition (:camera @globals/*gui-state*) position))

(defn set-current-camera-rotation
  [rotation]
  (.setRotation (:camera @globals/*gui-state*) rotation))

(defn set-camera-information
  [position rotation]
  (.setPosition (:camera @globals/*gui-state*) position)
  (.setRotation (:camera @globals/*gui-state*) rotation))

(defn get-camera-information
  []
  (.toString (:camera @globals/*gui-state*)))

(defn get-camera-command
  []
  (let [position (.getPosition (:camera @globals/*gui-state*))
        rotation (.getRotation (:camera @globals/*gui-state*))]
    (str "(set-camera-information "
         "(vec3 " (.x position) " " (.y position) " " (.z position) ") "
         "(vec4 " (.x rotation) " " (.y rotation) " " (.z rotation) " " (.w rotation) ")"
         ")")))

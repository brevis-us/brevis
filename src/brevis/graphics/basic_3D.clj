(ns brevis.graphics.basic-3D
  (:import [java.lang.Math]
           [java.nio ByteBuffer ByteOrder]
           [org.lwjgl.opengl GL11]
           [brevis BrObject]
           [brevis.graphics Basic3D BrSky])
  (:use [brevis globals]
        [brevis.physics utils]
        [brevis.shape core box sphere cone])
  (:require [clojure.java.io])) 

(defn init-sky
 []
 (def #^:dynamic *sky*
   (BrSky.)))

;; ## Shape handling code
;;

(defn vector3d-to-seq
  [v]
  "Return a seq that contains the vector3d's data"
  [(.x v) (.y v) (.z v)])

(defn vector4d-to-seq
  [v]
  "Return a seq that contains the vector3d's data"
  [(.x v) (.y v) (.z v) (.w v)])

(defn use-camera
  "Set the camera parameters."
  [cam]
  (.orthographicMatrix cam)
  (.perspectiveMatrix cam)
  (.translate cam))

(defn camera-set-position
  [cam new-position]
  (.setPosition cam new-position))

(defn camera-look-at
  "Camera look at a given location from the current camera location."
  [cam target-vec]
  (println "camera-look-at" (.getPosition cam) target-vec)
  (.lookAt cam (.getPosition cam) target-vec))

(defn draw-shape
  "Draw a shape. Call this after translating, scaling, and setting color."
  [^BrObject obj]
  (Basic3D/drawShape obj (.getDimension (.getShape obj)))
  #_(Basic3D/drawShape obj (double-array [0 1 0 0]) (.getDimension (.getShape obj))))
	  
(defn draw-shape-shadow
  "Draw a shape. Call this after translating, scaling, and setting color."
  [obj]
  (Basic3D/castShadow (.getMesh (.getShape obj)) (double-array [0 1 0 0]))
  )

(defn add-light
  "Add a GL light."
  []
  (Basic3D/addLight))

(defn move-light
  "Move the n-th light."
  [n ^org.lwjgl.util.vector.Vector4f pos]
  (Basic3D/lightMove (int n) (float-array [(.x pos) (.y pos) (.z pos) (.w pos)])))

(defn light-diffuse 
  "Set the diffuse lighting for a GlLight"
  [n ^org.lwjgl.util.vector.Vector4f col]
  (Basic3D/lightDiffuse (int n) (float-array [(.x col) (.y col) (.z col) (.w col)])))

(defn light-specular
  "Set the specular lighting for a GlLight"
  [n ^org.lwjgl.util.vector.Vector4f col]
  (Basic3D/lightSpecular (int n) (float-array [(.x col) (.y col) (.z col) (.w col)])))

(defn light-ambient
  "Set the ambient lighting for a GlLight"
  [n ^org.lwjgl.util.vector.Vector4f col]
  (Basic3D/lightAmbient (int n) (float-array [(.x col) (.y col) (.z col) (.w col)])))

(defn disable-skybox
  "Disable rendering of the skybox."
  []
  (swap! *gui-state* assoc :disable-skybox true))

(defn enable-skybox
  "Enable rendering of the skybox."
  []
  (swap! *gui-state* dissoc :disable-skybox))

(defn change-skybox
  "Files must contain: front, left, back, right, up, down"
  [files]
  (.changeSkybox brevis.graphics.basic-3D/*sky* files))

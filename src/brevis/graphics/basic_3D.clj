#_"This file is part of brevis.                                                                                                                                                 
                                                                                                                                                                                     
    brevis is free software: you can redistribute it and/or modify                                                                                                           
    it under the terms of the GNU General Public License as published by                                                                                                             
    the Free Software Foundation, either version 3 of the License, or                                                                                                                
    (at your option) any later version.                                                                                                                                              
                                                                                                                                                                                     
    brevis is distributed in the hope that it will be useful,                                                                                                                
    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                                                                                   
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                                                                                    
    GNU General Public License for more details.                                                                                                                                     
                                                                                                                                                                                     
    You should have received a copy of the GNU General Public License                                                                                                                
    along with brevis.  If not, see <http://www.gnu.org/licenses/>.                                                                                                          
                                                                                                                                                                                     
Copyright 2012, 2013 Kyle Harrington"

(ns brevis.graphics.basic-3D
  (:import [java.lang.Math]
           [java.nio ByteBuffer ByteOrder]
           [org.lwjgl.opengl GL11]
           [brevis.graphics Basic3D])
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        ;[cantor]
        [brevis globals]
        [brevis.physics utils]
        [brevis.shape core box sphere cone])
  (:require [penumbra.app :as app]
            [penumbra.text :as text]
            [penumbra.data :as data]
            ;[cantor.range]
            [clojure.java.io]
            [penumbra.opengl.frame-buffer :as fb])) 

#_(defn init-sky
  []
  (def #^:dynamic *sky*
    (load-texture-from-file (clojure.java.io/resource "img/sky.jpg"))))


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

(defn draw-shape
  "Draw a shape. Call this after translating, scaling, and setting color."
  [obj]
  (Basic3D/drawShape obj (double-array [0 1 0 0]) (.getDimension (.getShape obj))))
	  
(defn draw-shape-shadow
  "Draw a shape. Call this after translating, scaling, and setting color."
  [obj]
  (Basic3D/castShadow (.getMesh (.getShape obj)) (double-array [0 1 0 0]))
  )

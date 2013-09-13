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
        [brevis.physics utils]
        [brevis.shape core box sphere cone])
  (:require [penumbra.app :as app]
            [penumbra.text :as text]
            [penumbra.data :as data]
            ;[cantor.range]
            [penumbra.opengl.frame-buffer :as fb]))  

(defn init-sky
  []
  (def #^:dynamic *sky*
    (load-texture-from-file "resources/img/sky.jpg")))
;    (load-texture-from-file "resources/img/sky.jpg")))

(defn init-shader
  []
  #_(defpipeline shader-program
    :attributes {alpha float}
    :vertex {pixel-alpha alpha
             :position (* :model-view-projection-matrix :vertex)}
    :fragment (float4 tint pixel-alpha))
  (def shader-program
    (create-program
      :declarations '[(uniform float3 tint)
                      (attribute float alpha)
                      (varying float pixel-alpha)]
      :vertex '(do
                 (<- pixel-alpha alpha)
                 (<- :position (* :model-view-projection-matrix :vertex)))
      :fragment '(<- :frag-color (float4 tint pixel-alpha)))))

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

(defn- do-draw-shape
  "Actually draw a primitive shape."
  [obj]
  (let [posvec (get-position obj)
        pos (vector3d-to-seq posvec)
        colvec (get-color obj)
        col (vector4d-to-seq colvec)
        dim (vector3d-to-seq (get-dimension obj))        
        rot (vector4d-to-seq (get-rotation obj))
        shin 80]
    #_(println "do-draw-shape" pos rot (get-velocity obj))
		  (push-matrix
       (shade-model :smooth)
       (depth-test :less)
		   #_(apply color (:color obj))
       (apply color col)
		   (material :front;-and-back
               :ambient-and-diffuse (into [] (conj col 1)); [1 1 1 1]
               :specular [1 1 1 1]
               :shininess shin)       
		   (translate pos)       		   
       (apply scale dim)
       (apply rotate rot)
       #_(when (pos? (.getTextureId obj))
         (GL11/glBindTexture GL11/GL_TEXTURE_2D (.getTextureId obj)))
		   (cond
        ;#_(= (:type (:shape obj)) :box)  (draw-box)
        ;(= (:type (:shape obj)) :box)  (Basic3D/drawBox 1.0 1.0 1.0)
        (= (.getType (.getShape obj)) "box")  (Basic3D/drawBox 1.0 1.0 1.0)        
        ;#_(= (:type (:shape obj)) :cone) (draw-cone)
        (= (.getType (.getShape obj)) "cone")  (Basic3D/drawCone 0.8 0.01 1.2 25 25)
        ;:else                          (draw-sphere);(= (:type (:shape obj)) :sphere) 
        :else                          (Basic3D/drawSphere 2.0 20 20))
     #_(GL11/glBindTexture GL11/GL_TEXTURE_2D 0)
     )))

#_(defn draw-shape
  "Draw a shape. Call this after translating, scaling, and setting color."
  [obj]
  (if (:texture obj)
    (with-enabled :texture-2d
      (with-texture (:texture obj)      
        (do-draw-shape obj)))
    (with-disabled :texture-2d
      (do-draw-shape obj))))

(defn draw-shape
  "Draw a shape. Call this after translating, scaling, and setting color."
  [obj]
  (do-draw-shape obj)
  #_(if (get-texture obj)
    (with-enabled :texture-2d
      (with-texture (get-texture obj)      
        (do-draw-shape obj)))
    (with-disabled :texture-2d
      (do-draw-shape obj))))
	  

(ns brevis.graphics.basic-3D
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [cantor]
        [brevis.physics.space]
        [brevis.shape core box sphere])
  (:require [penumbra.app :as app]
            [penumbra.text :as text]
            [penumbra.data :as data]
            [cantor.range]
            [penumbra.opengl.frame-buffer :as fb]))  

(defn init-sky
  []
  (def #^:dynamic *sky*
    (load-texture-from-file "/Users/kyleharrington/Documents/workspace/brevis/resources/img/sky.jpg")))

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
;; < This is currently under revision. >

(defn draw-shape
  "Draw a shape. Call this after translating, scaling, and setting color."
  [obj]
  (if (:texture obj)          
    (with-enabled :texture-2d
      (with-texture brevis.shape.core/*checkers*;(:texture obj)      
        (draw-shape (dissoc obj :texture))))
	  (let [pos (get-position obj)
	        vel (get-velocity obj)
	        col (:color obj)]
     ;(with-pipeline shader-program [{:tint [1. 1. 0.]} (app/size)]
		  (push-matrix
		   #_(apply color (:color obj))
		   (material :front-and-back
		    :ambient-and-diffuse (into [] (conj col 1)); [1 1 1 1]
        :specular [1 1 1 1]
;		    :specular            [0.5 0.4 0.4 1]
		    :shininess           80)
		   (translate pos)
		   (apply scale (:dim (:shape obj)))
		   (rotate (.x vel) 1 0 0)
		   (rotate (.y vel) 0 1 0)
		   (rotate (.z vel) 0 0 1)       
		   (cond
	      (= (:type (:shape obj)) :box) (draw-textured-cube)
	      (= (:type (:shape obj)) :sphere) (draw-sphere)))
		   #_(call-display-list (cond (= (:type (:shape obj)) :box) box-graphic)))))
	  
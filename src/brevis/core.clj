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

(ns brevis.core
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [brevis globals utils input osd display vector]
        [brevis.graphics.basic-3D]
        [brevis.physics core space utils]
        [brevis.shape core box sphere cone])       
  (:require [penumbra.app :as app]            
            [clojure.math.numeric-tower :as math]
            [penumbra.text :as text]
            [penumbra.data :as data]
            [penumbra.opengl.frame-buffer :as fb]
            [penumbra.opengl.effects :as glfx])
  (:import (brevis.graphics Basic3D) 
           (java.awt AWTException Robot Rectangle Toolkit)
           (java.awt.geom AffineTransform)
           (java.awt.image AffineTransformOp BufferedImage)
           (java.nio ByteBuffer)
           (java.io File IOException)
           (javax.imageio ImageIO)
           (org.lwjgl.opengl Display GL11)
           (org.lwjgl BufferUtils)))

;; ## Todo:
;;
;; - Picking algorithm for choosing 3D objects with mouse

;; ## Window and Graphical Environment

(defn init
  "Initialize the brevis window and the graphical environment."
  [state]
  (app/title! "brevis")
  (app/vsync! true)
  
  #_(Basic3D/initGL)
  
  ;(app/key-repeat! false)
  (enable :blend)
  (enable :depth-test)
  #_(clear-color [0 0 0 0.5])  
  (init-box-graphic)
  #_(init-sphere)
  #_(init-cone)
  (init-checkers)
  (init-sky)
  (enable :lighting)
  (enable :light0)
  (light 0 
         :specular [0.4 0.4 0.4 1.0];:specular [1 1 1 1.0]
         :position [0 -1 0 0];;directional can be enabled after the penumbra update         
         ;:position [250 250 -100 1]         
         :diffuse [1 1 1 1])
  (enable :light1)
  (light 1
         :specular [0.2 0.2 0.2 1.0]
         :position [0 -1 0 0]
         ;:position [250 250 -100 1]
         :diffuse [1 1 1 1])
  (glfx/light-model :light-model-ambient [0.5 0.5 0.5 1.0])
  ;(glfx/gl-enable :point-smooth)
  ;(glfx/gl-enable :line-smooth)
  ;(enable :polygon-smooth)  
  (blend-func :src-alpha :one-minus-src-alpha)  
  (enable :normalize)
  #_(init-shader)    
  (java-init-world)
  #_(glfx/enable-high-quality-rendering)
  state
  #_(glfx/enable-high-quality-rendering))

#_(defn init
  "Initialize the brevis window and the graphical environment."
  [state]
  (app/title! "brevis")
  (app/vsync! true)
  
  (Basic3D/initGL)
  
  #_(init-checkers)
  #_(init-sky)
  (enable :lighting)
  (enable :light0)
  (light 0 
         :specular [0.4 0.4 0.4 1.0];:specular [1 1 1 1.0]
         :position [0 -1 0 0];;directional can be enabled after the penumbra update         
         ;:position [250 250 -100 1]         
         :diffuse [1 1 1 1])
  (enable :light1)
  (light 1
         :specular [0.2 0.2 0.2 1.0]
         :position [0 -1 0 0]
         ;:position [250 250 -100 1]
         :diffuse [1 1 1 1])
  (glfx/light-model :light-model-ambient [0.5 0.5 0.5 1.0])
  ;(glfx/gl-enable :point-smooth)
  ;(glfx/gl-enable :line-smooth)
  ;(enable :polygon-smooth)  
  (blend-func :src-alpha :one-minus-src-alpha)  
  (enable :normalize)
  #_(init-shader)    
  (java-init-world)
  #_(glfx/enable-high-quality-rendering)
  state
  #_(glfx/enable-high-quality-rendering))

(defn make-init
  "Make an initialize function based upon a user-customized init function."
  [user-init]
  #_(println "make-init")
  (fn [state]
    (init state)
    (user-init)
    state))

(defn reshape
  "Reshape after the window is resized."
  [[x y w h] state]
  (frustum-view  45 (/ w h) 0.1 2000)
  (load-identity)
  #_(translate 0 0 -40)
  (light 0 
         ;:specular [1 1 1 1]
         :position [1 100 10 1]
         :diffuse [1 1 1 1])
  #_(light 1
         :ambient [1 1 1 1]
         :specular [1 1 1 1]
         :position [100 -100 10 0]
         :diffuse [1 1 1 1])
  (reset! *gui-state*
          (assoc @*gui-state*
                 :window-x x
                 :window-y y
                 :window-width w
                 :window-height h))
  (assoc state
    :window-x x
    :window-y y
    :window-width w
    :window-height h))

(defn draw-sky
  "Draw a skybox"
  []
  (let [w 2000
        h 2000
        d 2000
        pos [0 0 0] ;(vec3 (- (/ w 2)) (- (/ h 2)) (- (/ d 2)))
        ]
    (when *sky*
      ;(with-enabled :texture-cube-map-seamless
      (with-disabled :lighting      
        (with-enabled :depth-test
          (with-enabled :texture-2d
            (with-texture *sky*
              (depth-test :lequal)
              #_(depth-range 1 1)
              ;GL11.glShadeModel (GL11.GL_FLAT);
               ; GL11.glDepthRange (1,1);
              (push-matrix
	            #_(color 0 0 1)
	            #_(material :front-and-back
	                      :ambient-and-diffuse [1 1 1 1]
	                      :specular [0 0 0 0];[1 1 1 1]
	;                      :shininess           80
	                      )
	            (material :front-and-back
	                      :shininess 0
	;                      :specular [0 0 0 1]
	                      :ambient-and-diffuse [0 0 1 0.5])
	            ;          :ambient-and-diffuse [1 1 1 1])
	            (translate pos)
	            (apply scale [w h d])
	            (draw-textured-cube)))))))))

(defn get-min-vec
  "Return the minimum vec3 of a collection, component-wise."
  [vectors]
  (reduce #(vec3 (Math/min (.x %1) (.x %2)) (Math/min (.y %1) (.y %2)) (Math/min (.z %1) (.z %2))) 
          (vec3 java.lang.Double/POSITIVE_INFINITY java.lang.Double/POSITIVE_INFINITY java.lang.Double/POSITIVE_INFINITY)
          vectors))

(defn get-max-vec
  "Return the maximum vec3 of a collection, component-wise."
  [vectors]
  (reduce #(vec3 (Math/max (.x %) (.x %2)) (Math/max (.y %1) (.y %2)) (Math/max (.z %1) (.z %2))) 
          (vec3 java.lang.Double/NEGATIVE_INFINITY java.lang.Double/NEGATIVE_INFINITY java.lang.Double/NEGATIVE_INFINITY)
          vectors))

(defn camera-score
  "What is the score of a current camera position relative to the world."
  [state]
  0)

(defn auto-camera
  "Automatically focus the camera to maximize the number of objects in view."
  [state]
  (let [obj-vecs (map get-position @*objects*)
        min-vec (get-min-vec obj-vecs)
        max-vec (get-max-vec obj-vecs)
        mid-vec (div (add min-vec max-vec) 2)
        comp-x (cos (* 2 Math/PI (/ (:rot-x state) 360)))
        comp-y (cos (* 2 Math/PI (/ (:rot-y state) 360)))
        comp-z (cos (* 2 Math/PI (/ (:rot-z state) 360)))
        next-state state]
    (if (> (camera-score next-state) (camera-score state))
      next-state state)))                            
  
(defn enable-video-recording
  "Turn on video recording."
  [video-name]
  (def video-counter (atom 0))
  (swap! *gui-state* 
         assoc :record-video true
               :video-name video-name))

(defn disable-video-recording
  "Turn off video recording."
  []
  (swap! *gui-state* dissoc :record-video))

(defn init-view
  "Initialize the gui-state global to the default."
  []
  (reset! *gui-state* default-gui-state))

(defn drawable?
  "Is an object drawable?"
  [obj]
  (.isDrawable obj))

(defn display
  "Display the world."
  [[dt t] state]
  (let [state (if (:auto-camera state) (auto-camera state) state)]      
    #_(clear)    
    (enable :lighting)
    (enable :light0)
    #_(enable :light1)
    (enable :texture-2D)
    (disable :texture-gen-s)
		(disable :texture-gen-t)
    (shade-model :smooth)
    (enable :blend)
    (blend-func :src-alpha :one-minus-src-alpha)  
    (enable :normalize)
    (enable :depth-test)
    (depth-test :lequal)
    #_(enable :cull-face)
    #_(cull-face :black)
    ;GL11.glFrontFace (GL11.GL_CCW);
    (viewport 0 0 (:window-width @*gui-state*) (:window-height @*gui-state*))
    (gl-matrix-mode :projection)
    (gl-load-identity-matrix)
    ;should if on width>height
    ;(frustum-view 60.0 (/ (double (:window-width @*gui-state*)) (:window-height @*gui-state*)) 1.0 1000.0)
    (frustum-view 60.0 (/ (double (:window-width @*gui-state*)) (:window-height @*gui-state*)) 0.1 3000)
    #_(light 0 
         :specular [0.4 0.4 0.4 1.0];:specular [1 1 1 1.0]
         :position [0 1 0 0];;directional can be enabled after the penumbra update         
         ;:position [250 250 -100 1]         
         :diffuse [1 1 1 1])
    (color 1 1 1)
    (clear-color 0.5 0.5 0.5 0)
    #_(clear)
    (gl-matrix-mode :modelview)
    ;(GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST)
    (gl-load-identity-matrix)
    (set-camera
      (:shift-x @*gui-state*) (:shift-y @*gui-state*) (:shift-z @*gui-state*)
      (:rot-x @*gui-state*) (:rot-y @*gui-state*) (:rot-z @*gui-state*))
    #_(light 0 
           :position [0 1 0 0])
    (light 0
           :ambient [0 0 0 1]
         ;:specular [0.4 0.4 0.4 1.0];:specular [1 1 1 1.0]
         :position [1 1 1 0];;directional can be enabled after the penumbra update         
         ;:position [250 250 -100 1]         
         ;:diffuse [1 1 1 1]
         )    
	  (draw-sky)
   (enable :lighting)
   (disable :texture-2D)
   ;(shade-model :flat)
   ;(enable :depth-test)
   ;(depth-test :less)
   (color 1 1 1)
    #_(println "display drawing n objects:" (count (get-objects)))
	  (doseq [obj (get-objects)]
     (when (drawable? obj)
       (draw-shape obj)))
   
   #_(doseq [obj (get-objects)]
     (when (drawable? obj)
       (draw-shape-shadow obj)))
   
   (when @enable-display-text
     (update-display-text [dt t] state))
	  (app/repaint!)
   (when @*screenshot-filename*
     (screenshot @*screenshot-filename* state)
     (reset! *screenshot-filename* nil))
   (when (:record-video @*gui-state*)
     (screenshot (str (:video-name @*gui-state*) "_" @video-counter ".png") state)
     (swap! video-counter inc))   
     ;(screenshot (str (:video-name @*gui-state*) "_" (get-time) ".png") state))
   ))

;; ## Start a brevis instance

(defn start-gui 
  "Start the simulation with a GUI."
  ([initialize]
    (start-gui initialize java-update-world))    
;    (start-gui initialize update-world))
  ([initialize update]
    #_(reset-core)
    (reset! *gui-message-board* (sorted-map))
    (when (.contains (System/getProperty "os.name") "indows")
      (reset! enable-display-text false))
	  (reset! *app-thread*
           (Thread. (fn [] (app/start
                             {:reshape reshape, :init (make-init initialize), :mouse-drag mouse-drag, :key-press key-press :mouse-wheel mouse-wheel, :update update, :display display
                              :key-release key-release
                              ;:mouse-move    (fn [[[dx dy] [x y]] state] (println )
                              ;:mouse-up       (fn [[x y] button state] (println button) state)
                              ;:mouse-click   (fn [[x y] button state] (println button) state)
                              ;:mouse-down    (fn [[x y] button state] (println button) state)
                              ;:mouse-wheel   (fn [dw state] (println dw) state)
                              }        
                             @*gui-state*))))
   (.start @*app-thread*)))

;; ## Non-graphical simulation loop (may need updating)

(defn simulation-loop
  "A simulation loop with no graphics."
  [state]
  ((:init state))
  (let [write-interval 10]
    (loop [state (assoc state
                        :simulation-time 0)
           t 0
           twrite 0
           wallwrite (java.lang.System/nanoTime)]
      (when (> t (+ twrite write-interval))
        (let [fps (double (/ (- t twrite) (- (java.lang.System/nanoTime) wallwrite) 0.0000000001))]
          (println "Walltime" (java.lang.System/nanoTime) 
                   "Simulation time" t
                   "FPS" fps)))
      (if (:terminated? state)
        state
        (recur ((:update state) [t (get-dt)] state)
               (+ t (get-dt))
               (if (> t (+ twrite write-interval)) t twrite)
               (if (> t (+ twrite write-interval)) (java.lang.System/nanoTime) wallwrite))))))

(defn start-nogui 
  "Start the simulation with a GUI."
  ([initialize]
    (start-nogui initialize update-world))
  ([initialize update]
    #_(reset-core)
	  (simulation-loop
	   {:init initialize, :update update})))      

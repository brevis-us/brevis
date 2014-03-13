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
        [brevis.graphics basic-3D multithread]
        [brevis.physics core space utils]
        [brevis.shape core box sphere cone])       
  (:require [penumbra.app :as app]            
            [clojure.math.numeric-tower :as math]
            [penumbra.text :as text]
            [penumbra.data :as data]
            [penumbra.opengl.frame-buffer :as fb]
            [penumbra.opengl.effects :as glfx])
  (:import (brevis.graphics Basic3D) 
           (brevis BrInput)
           (java.awt AWTException Robot Rectangle Toolkit)
           (java.awt.geom AffineTransform)
           (java.awt.image AffineTransformOp BufferedImage)
           (java.nio ByteBuffer)
           (java.io File IOException)
           (java.util.concurrent.locks ReentrantLock)
           (java.util.concurrent TimeUnit)
           (javax.imageio ImageIO)
           (org.lwjgl.opengl SharedDrawable)
           (org.lwjgl.input Keyboard Mouse)
           (org.lwjgl.opengl Display GL11 DisplayMode GLContext)
           (org.lwjgl BufferUtils LWJGLException Sys)))

;; ## Todo:
;;
;; - Picking algorithm for choosing 3D objects with mouse

;; ## Camera

(defn move-from-look
  "Move a camera according to some dX, dY, dZ."
  [cam dx dy dz]
  (.moveFromLook cam dx dy dz))

(defn rotate-from-look
  "Rotate a camera according to some dR (roll), dP (pitch), dW (yaw)."
  [cam dr dp dw]
  (.rotateFromLook cam dr dp dw))

;; ## Window and Graphical Environment

#_(defn init
  "Initialize the brevis window and the graphical environment."
  [state]
  (app/title! "brevis")
  (app/vsync! true)   
  (enable :blend)
  (enable :depth-test)
  (init-box-graphic)
  (init-checkers)
  (init-sky)
  (enable :lighting)
  (enable :light0)
  (light 0 
         :specular [0.4 0.4 0.4 1.0];:specular [1 1 1 1.0]
         :position [0 -1 0 0];;directional can be enabled after the penumbra update         
         :diffuse [1 1 1 1])
  (enable :light1)
  (light 1
         :specular [0.2 0.2 0.2 1.0]
         :position [0 -1 0 0]
         :diffuse [1 1 1 1])
  (glfx/light-model :light-model-ambient [0.5 0.5 0.5 1.0])
  (blend-func :src-alpha :one-minus-src-alpha)  
  (enable :normalize)
  (java-init-world)
  (init-sky)
  state)

#_(defn make-init
  "Make an initialize function based upon a user-customized init function."
  [user-init]
  (fn [state]
    (init state)
    (user-init)
    state))

#_(defn reshape
  "Reshape after the window is resized."
  [[x y w h] state]
  (frustum-view  45 (/ w h) 0.1 2000)
  (load-identity)
  (reset! *gui-state*
          (assoc @*gui-state*
                 :window-x x
                 :window-y y
                 :window-width w
                 :window-height h))
  state)

(defn draw-sky
  "Draw a skybox"
  []
  (when *sky*
    #_(println "drawing sky")
    (.draw *sky* 
      (.x (:camera @*gui-state*))
      (.y (:camera @*gui-state*))
      (.z (:camera @*gui-state*)))
    #_(.draw *sky*)))

#_(defn draw-sky
  "Draw a skybox"
  []
  (let [w 2000
        h 2000
        d 2000
        pos [0 0 0]
        ]
    (when *sky-texture*
      (with-disabled :lighting      
        (with-enabled :depth-test
          (with-enabled :texture-2d
            (with-texture *sky-texture*
              (depth-test :lequal)
              (push-matrix
	            (material :front-and-back
	                      :shininess 0
	                      :ambient-and-diffuse [0 0 1 0.5])
	            (translate pos)
	            (apply scale [w h d])
	            (draw-textured-cube)))))))))

#_(defn get-min-vec
  "Return the minimum vec3 of a collection, component-wise."
  [vectors]
  (reduce #(vec3 (Math/min (.x %1) (.x %2)) (Math/min (.y %1) (.y %2)) (Math/min (.z %1) (.z %2))) 
          (vec3 java.lang.Double/POSITIVE_INFINITY java.lang.Double/POSITIVE_INFINITY java.lang.Double/POSITIVE_INFINITY)
          vectors))

#_(defn get-max-vec
  "Return the maximum vec3 of a collection, component-wise."
  [vectors]
  (reduce #(vec3 (Math/max (.x %) (.x %2)) (Math/max (.y %1) (.y %2)) (Math/max (.z %1) (.z %2))) 
          (vec3 java.lang.Double/NEGATIVE_INFINITY java.lang.Double/NEGATIVE_INFINITY java.lang.Double/NEGATIVE_INFINITY)
          vectors))

#_(defn camera-score
  "What is the score of a current camera position relative to the world."
  [state]
  0)

#_(defn auto-camera
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

;; ## Start a brevis instance

(defn display
  "Render all objects."
  []  
  (begin-with-graphics-thread)
  (when (Display/wasResized) (.setDimensions (:camera @*gui-state*) (float (Display/getWidth)) (float (Display/getHeight))))
  (let [objs (all-objects)]    
    (Basic3D/initFrame (:camera @*gui-state*))
    (draw-sky)
    #_(update-display-text)
    #_(gl-matrix-mode :modelview)
    #_(gl-load-identity-matrix)
    #_(use-camera (:camera @*gui-state*))
    (doseq [obj objs]
      (when (drawable? obj) ;; add a check to see if the object is in view
       (draw-shape obj)))    
    (Display/update)        
    (Display/sync 100)
    (end-with-graphics-thread)
    ))

(defn simulate
  "Simulation loop."
  [initialize update & input-handlers]
  (Display/setLocation (/ (- (.getWidth (Display/getDisplayMode)) (.width (:camera @*gui-state*))) 2)
                       (/ (- (.getHeight (Display/getDisplayMode)) (.height (:camera @*gui-state*))) 2))
  (try 
    (Display/setDisplayMode (DisplayMode. (.width (:camera @*gui-state*)) (.height (:camera @*gui-state*))))
    (Display/setTitle "Brevis")
    (Display/setVSyncEnabled true)
    (Display/setResizable true)
    (Display/create)
    (catch LWJGLException e
        (.printStackTrace e)))
  ;; For multithreaded graphics
  (swap! *graphics* assoc
         :drawable (SharedDrawable. (Display/getDrawable))
         :lock (ReentrantLock.))
  (Basic3D/initGL)     
  (init-sky)
  (when *sky*
    (println "Sky loaded."))
  (initialize)
  (try 
    (reset! *gui-state* (assoc @*gui-state* :input (BrInput.)))
    (catch LWJGLException e
      (.printStackTrace e)))
  (if (empty? input-handlers);; hack for custom input handlers
    (default-input-handlers)
    ((first input-handlers)))
  (let [startTime (ref (java.lang.System/nanoTime))
        fps (ref 0)
        display? true]
    (loop [step 0]             
      (if (:close-requested @*gui-state*)
        (println "Closing application.")
        (do
          (.pollInput (:input @*gui-state*) @*java-engine*)
          ;(update [1 1] {})
          (update [(* step (get-dt)) (get-dt)] {})
          (dosync (ref-set fps (inc @fps)))
          (when (> (java.lang.System/nanoTime) @startTime)
            #_(println "Update" step "FPS:" (double (/ @fps (/ (- (+ 1000000000 (java.lang.System/nanoTime)) @startTime) 1000000000))))
            (dosync 
              (ref-set startTime (+ (java.lang.System/nanoTime) 1000000000))
              (ref-set fps 0)))
          (when display?            
            #_(when (Display/wasResized) (.setDimensions (:camera @*gui-state*) (float (Display/getWidth)) (float (Display/getHeight))))
            #_(println "fullscreen" (:fullscreen @*gui-state*) (not (Display/isFullscreen)))
            #_(when (and (:fullscreen @*gui-state*) (not (Display/isFullscreen))) (println "going fullscreen") (Display/setFullscreen true))
            #_(when (and (not (:fullscreen @*gui-state*)) (Display/isFullscreen)) (println "disable fullscreen") (Display/setFullscreen false))
            (display)
              )
          (recur (inc step))))))
  (Keyboard/destroy)
  (Mouse/destroy)
  (Display/destroy)
  ;; Should call system/exit if not using UI
  #_(System/exit 0)
  )

(defn start-gui 
  "Start the simulation with a GUI."
  ([initialize]
    (start-gui initialize java-update-world))    
  ([initialize update & input-handlers]
    (reset! *gui-message-board* (sorted-map))
    (when (.contains (System/getProperty "os.name") "indows")
      (reset! enable-display-text false))
	  (reset! *app-thread*
           (if-not (empty? input-handlers)
             (Thread. (fn [] (simulate initialize update (first input-handlers))))
             (Thread. (fn []
                        (simulate initialize update)
                        #_(app/start
                               {:reshape reshape, :init (make-init initialize), :mouse-drag mouse-drag, :key-press key-press :mouse-wheel mouse-wheel, :update update, :display display
                                :key-release key-release
                                ;:mouse-move    (fn [[[dx dy] [x y]] state] (println )
                                ;:mouse-up       (fn [[x y] button state] (println button) state)
                                ;:mouse-click   (fn [[x y] button state] (println button) state)
                                ;:mouse-down    (fn [[x y] button state] (println button) state)
                                ;:mouse-wheel   (fn [dw state] (println dw) state)
                                }        
                               @*gui-state*)))))
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
	  (simulation-loop
	   {:init initialize, :update update})))      

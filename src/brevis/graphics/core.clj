(ns brevis.graphics.core)

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

(ns brevis.core;graphics.core;; was just brevis.core
  (:use ;[penumbra opengl compute]
        ;[penumbra.opengl core]
        [brevis.init]; ew.....
        [brevis globals utils input osd display vector]
        [brevis.graphics basic-3D multithread]
        [brevis.physics core space utils]
        [brevis.shape core box sphere cone])       
  (:require ;[penumbra.app :as app]            
            [clojure.math.numeric-tower :as math]
            ;[penumbra.text :as text]
            ;[penumbra.data :as data]
            ;[penumbra.opengl.frame-buffer :as fb]
            #_[penumbra.opengl.effects :as glfx])
  (:import (brevis.graphics Basic3D) 
           (brevis BrInput SystemUtils Natives)
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
           (org.lwjgl BufferUtils LWJGLException Sys)
           ))

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

#_(defn init-view
   "Initialize the gui-state global to the default."
   []
   (reset! *gui-state* default-gui-state))

(defn drawable?
  "Is an object drawable?"
  [obj]
  (.isDrawable obj))

;; ## Start a brevis instance

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

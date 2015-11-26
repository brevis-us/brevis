(ns brevis.graphics.core)
(ns brevis.core;graphics.core;; was just brevis.core
  (:use [brevis.init]; ew.....
        [brevis globals utils input osd display vector]
        [brevis.graphics basic-3D multithread visual-overlays]
        [brevis.physics core space utils]
        [brevis.shape core box sphere cone])       
  (:require [clojure.math.numeric-tower :as math])
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

(defn drawable?
  "Is an object drawable?"
  [obj]
  (.isDrawable ^brevis.BrObject obj))

;; ## Start a brevis instance

(defn draw-sky
  "Draw a skybox"
  []
  (when *sky*
    #_(println "drawing sky")
    ;old
    #_(.draw *sky* 
       (.x (:camera @*gui-state*))
       (.y (:camera @*gui-state*))
       (.z (:camera @*gui-state*)))
    (.draw ^brevis.graphics.BrSky *sky* 
      (:camera @*gui-state*))
    #_(.draw *sky*)))

(defn init-display
  "Initialize the display before we do updates."
  []
  (begin-with-graphics-thread)
  (when (Display/wasResized) (.setDimensions ^brevis.graphics.BrCamera (:camera @*gui-state*) (float (Display/getWidth)) (float (Display/getHeight))))
  (Basic3D/generateTextureCoordinates)
  (Basic3D/initFrame (:camera @*gui-state*))
  #_(when-not (:disable-skybox @*gui-state*)
     (draw-sky))
  (end-with-graphics-thread))

(defn display
  "Render all objects."
  []  
  (begin-with-graphics-thread)
  (when (Display/wasResized) 
    (.setDimensions (:camera @*gui-state*) 
      (float (Display/getWidth)) (float (Display/getHeight))))
  (let [objs (all-objects)]    
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT
                          GL11/GL_DEPTH_BUFFER_BIT
                          GL11/GL_STENCIL_BUFFER_BIT))
    (GL11/glEnable GL11/GL_BLEND)
    (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)
        
    #_(Basic3D/initFrame (:camera @*gui-state*))
    (when-not (:disable-skybox @*gui-state*)
     (draw-sky))
    
    (GL11/glCullFace GL11/GL_BACK);
    (GL11/glEnable GL11/GL_CULL_FACE)
    (GL11/glDepthMask true)
    (GL11/glEnable GL11/GL_DEPTH_TEST )
    
     #_(update-display-text)
     #_(gl-matrix-mode :modelview)
     #_(gl-load-identity-matrix)
     #_(use-camera (:camera @*gui-state*))
    
     ; Calculates shadows 
     #_(Basic3D/initShadows (:camera @*gui-state*))
     #_(doseq [obj objs]
        (when (drawable? obj) ;; add a check to see if the object is in view
         (draw-shape obj)
         #_(draw-shape-shadow obj)
         ))
     #_(Basic3D/finishShadows)
     ; Second pass    
     (doseq [obj (reverse objs)]
     ;(doseq [obj (sort-by (partial z-val get-position) (reverse objs))]; should do something like this for z-ordering
       (when (drawable? obj) ;; add a check to see if the object is in view
        (draw-shape obj)
        #_(draw-shape-shadow obj)
        ))    
     ; Third pass
     #_(doseq [obj objs]
        (when (drawable? obj) ;; add a check to see if the object is in view
         (draw-shape obj)
         #_(draw-shape-shadow obj)
         ))
     (doseq [vo @visual-overlays]      
       (draw-visual-overlay vo))        
     (Display/update)        
     (Display/sync 60)
     (end-with-graphics-thread)
     ))

#_(defn display
    "Render all objects."
    []  
    (begin-with-graphics-thread)
    (Basic3D/displayEngine @*java-engine* (:camera @*gui-state*))
    (doseq [vo @visual-overlays]      
      (draw-visual-overlay vo))        
    (Display/update)        
    (Display/sync 100)
    (end-with-graphics-thread))

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
    ;(Display/create)
    (Display/create (org.lwjgl.opengl.PixelFormat. 8 24 0 8));/*Alpha Bits*/8, /*Depth bits*/ 8, /*Stencil bits*/ 0, /*samples*/8
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
      (when-not (:close-requested @*gui-state*)
        #_(println "Closing application.")
        (do
          (.pollInput ^brevis.BrInput (:input @*gui-state*) ^Engine @*java-engine*)
          #_(when (:reset-simulation @*gui-state*)
             (empty-simulation)
             (swap! *gui-state* dissoc :reset-simulation))
          ;(update [1 1] {})
          (when display?
            (init-display))
          (update [(* step (get-dt)) (get-dt)] {})
          (dosync (ref-set fps (inc @fps)))
          (when (and (:display-fps @*gui-state*)
                     (> (java.lang.System/nanoTime) @startTime))
            (println "Update" step "FPS:" (double (/ @fps (/ (- (+ 1000000000 (java.lang.System/nanoTime)) @startTime) 1000000000))))
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
  (doseq [dh @destroy-hooks] (dh))    
  (Keyboard/destroy)
  (Mouse/destroy)
  (Display/destroy)  
  ;; Should call system/exit if not using UI
  (when-not 
    (or (find-ns 'ccw.complete)
        (find-ns 'brevis.ui.core));; if we're in CCW or the Brevis IDE, don't exit.
    (System/exit 0));; exit only when not using a repl-mode
  )

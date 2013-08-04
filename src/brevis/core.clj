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
        ;[cantor]
        [brevis.graphics.basic-3D]
        [brevis.physics core space utils vector]
        [brevis.shape core box sphere cone])       
  (:require [penumbra.app :as app]            
            [clojure.math.numeric-tower :as math]
            [penumbra.text :as text]
            [penumbra.data :as data]
            [cantor.range]
            [penumbra.opengl.frame-buffer :as fb]
            [penumbra.opengl.effects :as glfx])
  (:import (java.awt AWTException Robot Rectangle Toolkit)
           (java.awt.geom AffineTransform)
           (java.awt.image AffineTransformOp BufferedImage)
           (java.nio ByteBuffer)
           (java.io File IOException)
           (javax.imageio ImageIO)
           (org.lwjgl.opengl Display GL11)
           (org.lwjgl BufferUtils)))

(def enable-display-text true)
(def #^:dynamic *gui-state* (atom {:rotate-mode :none :translate-mode :none                                    
                                   :rot-x 0 :rot-y 0 :rot-z 0
                                   :shift-x 0 :shift-y -20 :shift-z -50;-30                                   
                                   :last-report-time 0 :simulation-time 0}))

;; ## Todo:
;;
;; - Picking algorithm for choosing 3D objects with mouse

;; ## Window and Graphical Environment

(defn init
  "Initialize the brevis window and the graphical environment."
  [state]
  (app/title! "brevis")
  (app/vsync! true)
  ;(app/key-repeat! false)
  (enable :blend)
  (enable :depth-test)
  ;(clear-color [0 0 0 1])
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
  ;(glfx/gl-enable :point-smooth)
  ;(glfx/gl-enable :line-smooth)
  ;(enable :polygon-smooth)  
  (blend-func :src-alpha :one-minus-src-alpha)  
  (enable :normalize)
  #_(init-shader)  
  (java-init-world)
  state
  #_(glfx/enable-high-quality-rendering))

(defn make-init
  "Make an initialize function based upon a user-customized init function."
  [user-init]
  (println "make-init")
  (fn [state]
    (init state)
    (user-init)
    state))

(def shift-key-down (atom false))
(defn sin [n] (float (Math/sin n)))
(defn cos [n] (float (Math/cos n)))

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

(def #^:dynamic *gui-message-board* (atom (sorted-map))) 

(defn osd
  "Write an onscreen message. Generally should be of the form:
(osd :msg-type msg-type :fn <my-function or string> :start-t 0 :stop-t -1)
:x and :y could also be specified
stop-t = -1 means do not automatically erase"
  ;[msg-type func x y start-t stop-t]
  [& args]
  (let [msg (apply hash-map args)
        uid (gensym)]  
    (swap! *gui-message-board* assoc
           uid msg)
    uid))

(defn remove-osd-by-uid
  "Remove an OSD message based on UID."
  [uid]
  (swap! *gui-message-board*
         dissoc uid))

(defn default-display-text
  "Setup the default display-text."
  []
  (osd :msg-type :penumbra :fn (fn [[dt t] state] (str (int (/ 1 dt)) " fps")) :start-t 0 :stop-t -1)
  (osd :msg-type :penumbra :fn (fn [[dt t] state] (str (float (:simulation-time state)) " time")) :start-t 0 :stop-t -1)
  (osd :msg-type :brevis :fn #(str (int (count @*objects*)) " objs") :start-t 0 :stop-t 5))

(defn update-display-text
  "Update the onscreen displayed (OSD) text."
  [[dt t] state]
  (loop [msgs @*gui-message-board*
         console-x 5
         console-y 5]
    (when-not (empty? msgs)      
      (let [[uid msg] (first msgs)
            x (or (:x msg) console-x)
            y (or (:y msg) console-y)
            sim-t (get-time)
            started? (> sim-t (:start-t msg))
            stopped? (and (> (:stop-t msg) 0) (> sim-t (:stop-t msg)))]
        (when started?
          (let [text (cond
                       (= (:msg-type msg) :penumbra) ((:fn msg) [dt t] state)
                       (= (:msg-type msg) :penumbra-rotate) ((:fn msg) [dt t] state)
                       (= (:msg-type msg) :penumbra-translate) ((:fn msg) [dt t] state)
                       (= (:msg-type msg) :brevis) ((:fn msg))
                       :else (str "OSD msg type:" (:msg-type msg) "not recognized"))]
            (text/write-to-screen text x y)))
        (when stopped?
          (remove-osd-by-uid uid))
        (recur (cond
                 (= (:msg-type msg) :penumbra-rotate) 
                 (filter #(not= (:msg-type (second %)) :penumbra-rotate) (rest msgs))
                 (= (:msg-type msg) :penumbra-translate) 
                 (filter #(not= (:msg-type (second %)) :penumbra-translate) (rest msgs))
                 :else (rest msgs))
               x (+ y 30))))))
  
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

(defn screenshot
  "Take a screenshot."
  [filename state]
  (let [pixels (int-array (* (:window-width state) (:window-height state))); Creating an rbg array of total pixels
        fb (ByteBuffer/allocateDirect (* 3 (:window-width state) (:window-height state))); allocate space for RBG pixels
        img-type (second (re-find (re-matcher #"\.(\w+)$" filename)))]        
    (fb/gl-read-pixels (int 0) (int 0) (int (:window-width state)) (int (:window-height state)) :rgb :unsigned-byte fb)
    (let [imageIn (BufferedImage. (:window-width state) (:window-height state) BufferedImage/TYPE_INT_RGB)]
      (dotimes [i (alength pixels)]
        (let [bidx (* 3 i)]
          (aset pixels i 
                (+ (bit-shift-left (.get fb bidx) 16) 
                   (bit-shift-left (.get fb (inc bidx)) 8) 
                   (bit-shift-left (.get fb (inc (inc bidx))) 0))))) 
     (.setRGB imageIn 0 0 (:window-width state) (:window-height state) pixels 0 (:window-width state)); Allocate colored pixel to buffered Image
     (let [at (AffineTransform/getScaleInstance 1 -1)]; Creating the transformation direction (horizontal)
       (.translate at 0 (- (.getHeight imageIn)))
       (let [opRotated (AffineTransformOp. at AffineTransformOp/TYPE_BILINEAR);//Applying transformation
             imageOut (. opRotated filter imageIn nil)
             file (File. filename)]
         ;; probably should use try-catch
         (ImageIO/write imageOut img-type file))))))

(defn get-objects
  "Return all objects in the simulation."
  []
  (seq (.getObjects  @*java-engine*)))

(defn display
  "Display the world."
  [[dt t] state]
  (let [state (if (:auto-camera state) (auto-camera state) state)]      
    (when enable-display-text
      (update-display-text [dt t] state))
	  (rotate (:rot-x @*gui-state*) 1 0 0)
	  (rotate (:rot-y @*gui-state*) 0 1 0)
	  (rotate (:rot-z @*gui-state*) 0 0 1)
	  (translate (:shift-x @*gui-state*) (:shift-y @*gui-state*) (:shift-z @*gui-state*))
	  (draw-sky)
    #_(println "display drawing n objects:" (count (get-objects)))
	  (doseq [obj (get-objects)]
	    (draw-shape obj))
	  (app/repaint!)
   (when (:record-video @*gui-state*)
     (screenshot (str (:video-name @*gui-state*) "_" @video-counter ".png") state)
     (swap! video-counter inc))   
     ;(screenshot (str (:video-name @*gui-state*) "_" (get-time) ".png") state))
   ))

(defn key-press
  "Update the state in response to a keypress."
  [key state]
  ;(println "key-press" key)
  (cond
    ;(= :lshift key) (do (reset! shift-key-down true) state)
    (= "z" key) (do (reset! shift-key-down true) state)
    (= "p" key) (do (app/pause!)
                  state)
    (= "o" key) (do (screenshot (str "brevis_screenshot_" (System/currentTimeMillis) ".png") state)
                  state)
    (= :escape key)
    (do (app/stop!)
      (assoc state 
             :terminated? true)
      )))

(defn key-release
  "Update the state in response to the release of a key"
  [key state]
  ;(println "key-release" key) 
  (cond
    ;(= :lshift key) (reset! shift-key-down false))
    (= "z" key) (reset! shift-key-down false))
  state)

;; ## Input handling
(defn mouse-drag
  "Rotate the world."
  [[dx dy] _ button state]
  (let [rads (/ (Math/PI) 180)
        thetaY (*(:rot-y state) rads)
        sY (sin thetaY)
        cY (cos thetaY)
        thetaX (* (:rot-x state) rads)
        sX (sin thetaX)
        cX (cos thetaX)
        t (get-time)]  
	  (when @shift-key-down
	    (cond 
	      ; Rotate
	      (= :left button)
	      (swap! *gui-state* assoc
	             :rot-x (+ (:rot-x @*gui-state*) dy)
	             :rot-y (+ (:rot-y @*gui-state*) dx))
	      (= :right button)
	      (swap! *gui-state* assoc
	             :shift-x (+ (:shift-x @*gui-state*) (* dx cY))
	             :shift-y (- (:shift-y @*gui-state*) (* dy cX))
	             :shift-z (+ (:shift-z @*gui-state*) (* dx sY))
	             )
	      ; Zoom
	      (= :center button)
	      (swap! *gui-state* assoc
	             :shift-x (+ (:shift-x @*gui-state*) (* (/ dy 6) (* sY -1)))
	             :shift-y (+ (:shift-y @*gui-state*) (* (/ dy 6) sX))
	             :shift-z (+ (:shift-z @*gui-state*) (* (/ dy 6) cY))           
	             )))
   (osd :msg-type :penumbra-rotate :fn (fn [[dt t] state] (str "Rotation: (" 
                                                        (:rot-x state) "," (:rot-y state) "," (:rot-z state) ")")) 
        :start-t t :stop-t (+ t 3))
   (osd :msg-type :penumbra-translate :fn (fn [[dt t] state] (str "Translation: (" 
                                                        (int (:shift-x @*gui-state*)) "," (int (:shift-y @*gui-state*)) "," (int (:shift-z @*gui-state*)) ")")) 
        :start-t t :stop-t (+ t 3)))
  state)

(defn mouse-wheel
  "Respond to a mousewheel movement. dw is +/- depending on scroll-up or down."
  [dw state]
  (let [rads (/ (Math/PI) 180)
        thetaY (*(:rot-y state) rads)
        sY (sin thetaY)
        cY (cos thetaY)
        thetaX (* (:rot-x state) rads)
        sX (sin thetaX)
        cX (cos thetaX)]
  (swap! *gui-state* assoc
						         :shift-z (+ (:shift-z @*gui-state*) (* (/ dw 6) cY))
						         :shift-x (+ (:shift-x @*gui-state*) (* (/ dw 6) (* sY -1)))
						         :shift-y (+ (:shift-y @*gui-state*) (* (/ dw 6) sX)))
  state))

(defn mouse-move
  "Respond to a change in x,y position of the mouse."
  [[[dx dy] [x y]] state] 
  (println "mouse-move" x y)
  state)
    
(defn mouse-up
  "Respond to a mouse button being released"
  [[x y] button state] 
  (println "mouse-up" button) 
  state)
    
(defn mouse-click
  "Respond to a click?"
  [[x y] button state]
  (println "mouse-click" button)
  state)

(defn mouse-down
  "Respond to a mouse button being pressed"
  [[x y] button state]
  (println "mouse-down" button)
  state)


;; ## Start a brevis instance

(defn- reset-core
  "Reset the core variables."
  []
  #_(reset! *collision-handlers* {})
  (reset! *gui-message-board* (sorted-map))
  (reset! *collisions* {})
  #_(reset! *update-handlers* {})
  (reset! *physics* nil)
  (reset! *objects* {}))

(defn disable-collisions "Disable collision detection." [] (reset! collisions-enabled false))
(defn enable-collisions "Enable collision detection." [] (reset! collisions-enabled true))

(defn disable-neighborhoods "Disable neighborhood detection." [] (reset! neighborhoods-enabled false))
(defn enable-neighborhoods "Enable neighborhood detection." [] (reset! neighborhoods-enabled true))

(defn start-gui 
  "Start the simulation with a GUI."
  ([initialize]
    (start-gui initialize java-update-world))    
;    (start-gui initialize update-world))
  ([initialize update]
    (reset-core)
	  (app/start
	   {:reshape reshape, :init (make-init initialize), :mouse-drag mouse-drag, :key-press key-press :mouse-wheel mouse-wheel, :update update, :display display
	    :key-release key-release
	    ;:mouse-move    (fn [[[dx dy] [x y]] state] (println )
	    ;:mouse-up       (fn [[x y] button state] (println button) state)
	    ;:mouse-click   (fn [[x y] button state] (println button) state)
	    ;:mouse-down    (fn [[x y] button state] (println button) state)
	    ;:mouse-wheel   (fn [dw state] (println dw) state)
	    }        
    @*gui-state*)))

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
    (reset-core)
	  (simulation-loop
	   {:init initialize, :update update})))      

#_(defn start-nogui [iteration-step-size]
  (let [dt iteration-step-size
        max-t 25]
    (loop [t 0
           state (reset-simulation {:iteration-step-size iteration-step-size
                                    :simulation-time 0})]
      (when (zero? t) (report state))
      (if (and (not (zero? max-t)) (> (+ t dt) max-t))
        (println "Simulation complete")
        (recur (+ t dt) (update [dt t] state))))))
    


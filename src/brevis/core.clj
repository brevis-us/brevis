(ns brevis.core
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [cantor]
        [brevis.graphics.basic-3D]
        [brevis.physics.space]
        [brevis.shape core box])       
  (:require [penumbra.app :as app]
            [clojure.math.numeric-tower :as math]
            [penumbra.text :as text]
            [penumbra.data :as data]
            [cantor.range]
            [penumbra.opengl.frame-buffer :as fb]
            [penumbra.opengl.effects :as glfx])
  (:import (java.awt AWTException Robot Rectangle Toolkit)
           (java.awt.image BufferedImage)
           (java.io File IOException)
           (javax.imageio ImageIO)
           (org.lwjgl BufferUtils)))

;; ## Window and Graphical Environment

(defn init
  "Initialize the brevis window and the graphical environment."
  [state]
  (app/title! "brevis")
  (app/vsync! true)
  ;(app/key-repeat! true)
  (enable :blend)
  (enable :depth-test)
  (init-box-graphic)
  (init-checkers)
  (init-sky)
  (enable :lighting)
  (enable :light0)
  (enable :light1)
  ;(enable :light2)
  ;(enable :light3)
  ;(enable :light4)
  ;(enable :light5)
  ;(glfx/gl-enable :point-smooth)
  ;(glfx/gl-enable :line-smooth)
  (enable :polygon-smooth)  
  (blend-func :src-alpha :one-minus-src-alpha)
  #_(init-shader)  
  state
  #_(glfx/enable-high-quality-rendering))

#_(def #^:dynamic *lights* (atom []))
#_(defn make-light
  "Make a light."
  [light-properties]
  (let [id (count @*lights*)]
    #_(eval (str "(enable" (keyword (str "light" id)) ")"))
    #_(enable (keyword (str "light" id)))
    (light id 
           :position (:position light-properties)
           :diffuse (:diffuse light-properties))))

(def shift-key-down (atom false))
(defn sin [n] (float (Math/sin n)))
(defn cos [n] (float (Math/cos n)))

(defn reshape
  "Reshape after the window is resized."
  [[x y w h] state]
  (frustum-view 45 (/ w h) 0.1 1000)
  (load-identity)
  #_(translate 0 0 -40)
  (light 0 
         :ambient [1 1 1 1]
         :specular [1 1 1 1]
         :position [1 -100 10 0]
         :diffuse [1 1 1 1])
  (light 1
         :ambient [1 1 1 1]
         :specular [1 1 1 1]
         :position [100 -100 10 0]
         :diffuse [1 1 1 1])
  (assoc state
    :window-width w
    :window-height h))

(defn draw-sky
  "Draw a skybox"
  []
  (let [w 1000
        h 1000
        d 1000
        pos (vec3 0 0 0) #_(vec3 (- (/ w 2)) 0 (- (/ d 2)))]
    (when *sky*
      (with-enabled :texture-2d
        (with-texture *sky*         
          (push-matrix
            (material :front-and-back
                      :ambient-and-diffuse [1 1 1 1]
                      :specular [0 0 0 0];[1 1 1 1]
                      :shininess           80)
            (translate pos)
            (apply scale [w h d])
            (draw-textured-cube)))))))  

(defn display
  "Display the world."
  [[dt time] state]
  (text/write-to-screen (str (int (/ 1 dt)) " fps") 0 0)
  (text/write-to-screen (str (float (:simulation-time state)) " time") 0 30)
  (text/write-to-screen (str "Rotation: (" 
                             (:rot-x state) "," (:rot-y state) "," (:rot-z state) ")") 0 60)
  (text/write-to-screen (str "Translation: (" 
                             (int (:shift-x state)) "," (int (:shift-y state)) "," (int (:shift-z state)) ")") 0 90)
  (rotate (:rot-x state) 1 0 0)
  (rotate (:rot-y state) 0 1 0)
  (rotate (:rot-z state) 0 0 1)
  (translate (:shift-x state) (:shift-y state) (:shift-z state))
  ;(with-disabled :texture-2d
  (draw-sky)
  (doseq [obj (vals @*objects*)]    
;    (doseq [obj (:objects state)]
    (draw-shape obj))
  (app/repaint!))

(defn screenshot
  "Take a screenshot. Currently captures the entire screen."
  [filename]
  (let [img-type (second (re-find (re-matcher #"\.(\w+)$" filename)))
	capture (.createScreenCapture (Robot.)
				      (Rectangle. (.getScreenSize (Toolkit/getDefaultToolkit))))
	file (File. filename)]
    (ImageIO/write capture img-type file)))

(defn key-press
  "Update the state in response to a keypress."
  [key state]
  (cond
    (= :lshift key) (do (reset! shift-key-down true) state)
    (= "p" key) (do (app/pause!)
                  state)
    (= "o" key) (do (screenshot (str "brevis_screenshot_" (System/currentTimeMillis) ".png"))
                  state)
    (= :escape key)
    (do (app/stop!)
      (assoc state 
             :terminated? true)
      )))

(defn key-release
  "Update the state in response to the release of a key"
  [key state]
  (cond
    (= :lshift key) (reset! shift-key-down false))
  state)

;; ## Input handling
(defn mouse-drag
  "Rotate the world."
  [[dx dy] _ button state]
  (def rads (/ (Math/PI) 180))
  (def thetaY (*(:rot-y state) rads))
  (def sY (sin thetaY))
  (def cY (cos thetaY))
  (def thetaX (* (:rot-x state) rads))
  (def sX (sin thetaX))
  (def cX (cos thetaX))
  
  (if @shift-key-down
    (cond 
      ; Rotate
      (= :left button)
      (assoc state
             :rot-x (+ (:rot-x state) dy)
             :rot-y (+ (:rot-y state) dx))
      (= :center button)
      (assoc state
             :shift-x (+ (:shift-x state) (* dx cY))
             :shift-y (- (:shift-y state) (* dy cX))
             :shift-z (+ (:shift-z state) (* dx sY))
             )
      ; Zoom
      (= :right button)
      (assoc state
             :shift-x (+ (:shift-x state) (* (/ dy 6) (* sY -1)))
             :shift-y (+ (:shift-y state) (* (/ dy 6) sX))
             :shift-z (+ (:shift-z state) (* (/ dy 6) cY))           
             ))))

(defn mouse-wheel
  "Respond to a mousewheel movement. dw is +/- depending on scroll-up or down."
  [dw state]
  (def rads (/ (Math/PI) 180))
  (def thetaY (*(:rot-y state) rads))
  (def sY (sin thetaY))
  (def cY (cos thetaY))
  (def thetaX (* (:rot-x state) rads))
  (def sX (sin thetaX))
  (def cX (cos thetaX))
  
  (assoc state
         :shift-z (+ (:shift-z state) (* (/ dw 6) cY))
         :shift-x (+ (:shift-x state) (* (/ dw 6) (* sY -1)))
         :shift-y (+ (:shift-y state) (* (/ dw 6) sX))
         ))

(defn mouse-move
  "Respond to a change in x,y position of the mouse."
  [[[dx dy] [x y]] state] 
  #_(println x y)
  state)
    
(defn mouse-up
  "Respond to a mouse button being released"
  [[x y] button state] 
  #_(println button) 
  state)
    
(defn mouse-click
  "Respond to a click?"
  [[x y] button state]
  #_(println button)
  state)

(defn mouse-down
  "Respond to a mouse button being pressed"
  [[x y] button state]
  #_(println button)
  state)


;; ## Start a brevis instance

(defn start-gui [initialize update]
  "Start the simulation with a GUI."
  (app/start
   {:reshape reshape, :init init, :mouse-drag mouse-drag, :key-press key-press :mouse-wheel mouse-wheel, :update update, :display display
    :key-release key-release
    ;:mouse-move    (fn [[[dx dy] [x y]] state] (println )
    ;:mouse-up       (fn [[x y] button state] (println button) state)
    ;:mouse-click   (fn [[x y] button state] (println button) state)
    ;:mouse-down    (fn [[x y] button state] (println button) state)
    ;:mouse-wheel   (fn [dw state] (println dw) state)
    }
   (merge {:rotate-mode :x :translate-mode :x     
           :rot-x 0 :rot-y 0 :rot-z 90
           :shift-x 0 :shift-y 20 :shift-z 0;-30
           :init-simulation initialize
           :dt 1 :last-report-time 0 :simulation-time 0}                      
          (initialize))))


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
    


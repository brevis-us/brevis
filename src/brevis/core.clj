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
            [penumbra.opengl.frame-buffer :as fb])
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
  (app/key-repeat! false)
  (enable :blend)
  (enable :depth-test)
  (init-box-graphic)
  (enable :lighting)
  (enable :light0)
  (blend-func :src-alpha :one-minus-src-alpha))

(defn reshape
  "Reshape after the window is resized."
  [[x y w h] state]
  (frustum-view 45 (/ w h) 0.1 1000)
  (load-identity)
  #_(translate 0 0 -40)
  (light 0 
         :position [1 -100 1 0]
         :diffuse [1 0.9 0.8 1])
  (assoc state
    :window-width w
    :window-height h))

(defn display
  "Display the world."
  [[dt time] state]
  (text/write-to-screen (str (int (/ 1 dt)) " fps") 0 0)
  (text/write-to-screen (str (:simulation-time state) " time") 0 30)
  (text/write-to-screen (str "Rotation [active " (:rotate-mode state) "]: (" 
                             (:rot-x state) "," (:rot-y state) "," (:rot-z state) ")") 0 60)
  (text/write-to-screen (str "Translation [active " (:translate-mode state) "]: (" 
                             (:shift-x state) "," (:shift-y state) "," (:shift-z state) ")") 0 90)
  (rotate (:rot-x state) 1 0 0)
  (rotate (:rot-y state) 0 1 0)
  (rotate (:rot-z state) 0 0 1)
  (translate (:shift-x state) (:shift-y state) (:shift-z state))
  (with-disabled :texture-2d
    (doseq [obj (:objects state)]
      (draw-shape obj)))
  (app/repaint!))

(defn screenshot
  "Take a screenshot. Currently captures the entire screen."
  [filename]
  (let [img-type (second (re-find (re-matcher #"\.(\w+)$" filename)))
	capture (.createScreenCapture (Robot.)
				      (Rectangle. (.getScreenSize (Toolkit/getDefaultToolkit))))
	file (File. filename)]
    (ImageIO/write capture img-type file)))

;; ## Input handling

(defn mouse-drag
  "Perform the respective action given a mouse click and displacement."
  [[dx dy] _ button state]
  (let [displacement dx];(math/sqrt (+ (* dx dx) (* dy dy)))]
	  (cond 
	    ; Rotate
	    (= :left button)
      (merge state
             (cond 
               (= (:rotate-mode state) :x) {:rot-x (+ (:rot-x state) displacement)}               
               (= (:rotate-mode state) :y) {:rot-y (+ (:rot-y state) displacement)}               
               (= (:rotate-mode state) :z) {:rot-z (+ (:rot-z state) displacement)}))
	    ; Zoom
	    (= :right button)
	    (merge state
             (cond 
               (= (:translate-mode state) :x) {:shift-x (+ (:shift-x state) displacement)}               
               (= (:translate-mode state) :y) {:shift-y (+ (:shift-y state) displacement)}               
               (= (:translate-mode state) :z) {:shift-z (+ (:shift-z state) displacement)})))))
   
(defn key-press
  "Update the state in response to a keypress."
  [key state]
  (cond
   (= "q" key) (assoc state
                      :rotate-mode :x)
   (= "w" key) (assoc state
                      :rotate-mode :y)
   (= "e" key) (assoc state
                      :rotate-mode :z)
   (= "r" key) (assoc state
                      :rotate-mode :none)
   (= "a" key) (assoc state
                      :translate-mode :x)
   (= "s" key) (assoc state
                      :translate-mode :y)
   (= "d" key) (assoc state
                      :translate-mode :z)
   (= "f" key) (assoc state
                      :translate-mode :none)
   (= "p" key) (do (app/pause!)
                 state)
   (= "o" key) (do (screenshot (str "brevis_screenshot_" (System/currentTimeMillis) ".png"))
                 state)
   (app/key-pressed? :up) 
   (merge state (cond 
               (= (:rotate-mode state) :x) {:rot-x (+ (:rot-x state) 5)}               
               (= (:rotate-mode state) :y) {:rot-y (+ (:rot-y state) 5)}               
               (= (:rotate-mode state) :z) {:rot-z (+ (:rot-z state) 5)}))
   (app/key-pressed? :down) 
   (merge state (cond 
               (= (:rotate-mode state) :x) {:rot-x (- (:rot-x state) 5)}               
               (= (:rotate-mode state) :y) {:rot-y (- (:rot-y state) 5)}               
               (= (:rotate-mode state) :z) {:rot-z (- (:rot-z state) 5)}))
   (app/key-pressed? :left) 
   (merge state
             (cond 
               (= (:translate-mode state) :x) {:shift-x (+ (:shift-x state) 5)}               
               (= (:translate-mode state) :y) {:shift-y (+ (:shift-y state) 5)}               
               (= (:translate-mode state) :z) {:shift-z (+ (:shift-z state) 5)}))
   (app/key-pressed? :right) 
   (merge state
             (cond 
               (= (:translate-mode state) :x) {:shift-x (- (:shift-x state) 5)}               
               (= (:translate-mode state) :y) {:shift-y (- (:shift-y state) 5)}               
               (= (:translate-mode state) :z) {:shift-z (- (:shift-z state) 5)}))   
   (= :escape key)
   (do (app/stop!)
     (assoc state 
          :terminated? true)
          )))

;; ## Start a brevis instance

(defn start-gui [initialize update]
  "Start the simulation with a GUI."
  (app/start
   {:reshape reshape, :init init, :mouse-drag mouse-drag, :key-press key-press, :update update, :display display}
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
    


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

(ns brevis.input
  (:require [penumbra.app :as app])
  (:use [brevis globals display utils osd vector]
        [brevis.physics utils]))

(def #^:dynamic *input-handlers* (atom {:key-press {}
                                        :key-release {}}));; no mouse yet

#_(def shift-key-down (atom false))
(defn sin [n] (float (Math/sin n)))
(defn cos [n] (float (Math/cos n)))

(defn add-input-handler
  "Add an input handler."
  [input-type input-predicate behavior]
  (reset! *input-handlers*
          (assoc-in @*input-handlers*
                    [:key-press input-predicate] behavior)))

(def keyspeed 10000)

(defn default-input-handlers
  "Define the default input handlers."
  []
  (add-input-handler :key-press
                     #(= "i" %)
                     #(do (swap! *gui-state* assoc :fullscreen (not (:fullscreen @*gui-state*)))
                          (app/fullscreen! (:fullscreen @*gui-state*))))
  (add-input-handler :key-press
                     #(= "w" %)
                     #(.processKeyboard (:camera @*gui-state*) keyspeed 1 true false false false false false))
  (add-input-handler :key-press
                     #(= "a" %)
                     #(.processKeyboard (:camera @*gui-state*) keyspeed 1 false false true false false false))
  (add-input-handler :key-press
                     #(= "s" %)
                     #(.processKeyboard (:camera @*gui-state*) keyspeed 1 false true false false false false))
  (add-input-handler :key-press
                     #(= "d" %)
                     #(.processKeyboard (:camera @*gui-state*) keyspeed 1 false false false true false false))
  (add-input-handler :key-press
                     #(= "c" %)
                     #(.processKeyboard (:camera @*gui-state*) keyspeed 1 false false false false true false))
  (add-input-handler :key-press
                     #(= :lshift %)
                     #(.processKeyboard (:camera @*gui-state*) keyspeed 1 false false false false false true))
  (add-input-handler :key-press
                     #(= "p" %)
                     #(app/pause!))
  (add-input-handler :key-press
                     #(= "o" %)
                     #(screenshot (str "brevis_screenshot_" (System/currentTimeMillis) ".png")))
  (add-input-handler :key-press
                     #(= :escape %)
                     #(app/stop!)))
;; Currently forcing default input handlers to be enabled
(default-input-handlers)

(defn key-press
  "Update the state in response to a keypress."
  [key state]
  (doseq [[predicate behavior] (:key-press @*input-handlers*)]
    (when (predicate key)
      (behavior)))
  state)

(defn key-release
  "Update the state in response to the release of a key"
  [key state]
  (doseq [[predicate behavior] (:key-release @*input-handlers*)]
    (when (predicate key)
      (behavior)))
  state)

(defn rotate-x
  "Rotate about the X-axis"
  [delta]
  (swap! *gui-state* assoc
         :rot-x (+ (:rot-x @*gui-state*) delta)))

(defn rotate-y
  "Rotate about the Y-axis"
  [delta]
  (swap! *gui-state* assoc
         :rot-y (+ (:rot-y @*gui-state*) delta)))

(defn rotate-z
  "Rotate about the Z-axis"
  [delta]
  (swap! *gui-state* assoc
         :rot-z (+ (:rot-z @*gui-state*) delta)))

(defn shift-x
  "Shift along the X-axis"
  [delta]
  (swap! *gui-state* assoc
         :shift-x (+ (:shift-x @*gui-state*) delta)))

(defn shift-y
  "Shift along the Y-axis"
  [delta]
  (swap! *gui-state* assoc
         :shift-y (+ (:shift-y @*gui-state*) delta)))

(defn shift-z
  "Shift along the Z-axis"
  [delta]
  (swap! *gui-state* assoc
         :shift-z (+ (:shift-z @*gui-state*) delta)))

(def mouse-translate-speed 100)

(defn osd-view-transformation
  "Display the current view transformation as an OSD message."
  []
  (let [cam (:camera @*gui-state*)
        t (get-time)]
    (osd :msg-type :penumbra-rotate 
         :fn (fn [[dt t] state] (str "Rotation: (" 
                                     (.roll cam) "," (.pitch cam) "," (.yaw cam) ")")) 
         :start-t t :stop-t (+ t 1))
    (osd :msg-type :penumbra-translate 
         :fn (fn [[dt t] state] (str "Translation: (" 
                                     (.x cam) "," (.y cam) "," (.z cam) ")"))                                      
         :start-t t :stop-t (+ t 1))))

;; ## Input handling
(defn mouse-drag
  "Rotate the world."
  [[dx dy] _ button state]
  (let [cam (:camera @*gui-state*)
        ;rot-axis (normalize (vec3 (.pitch cam) (.yaw cam) (.roll cam))#_(vec3 (:rot-x @*gui-state*) (:rot-y @*gui-state*) (:rot-z @*gui-state*)))
        ;cam-position (.position cam) #_(vec3 (:shift-x @*gui-state*) (:shift-y @*gui-state*) (:shift-z @*gui-state*))
        ;temp (do (println rot-axis cam-position (vec3 (.pitch cam) (.yaw cam) (.roll cam))))
        ;mouse-x-axis (cross rot-axis (vec3 0 1 0))
        ;mouse-y-axis (cross mouse-x-axis rot-axis)
        
;        rads (/ (Math/PI) 180)
;        thetaY (*(:rot-y @*gui-state*) rads)
;        sY (sin thetaY)
;        cY (cos thetaY)
;        thetaX (* (:rot-x @*gui-state*) rads)
;        sX (sin thetaX)
;        cX (cos thetaX)
        t (get-time)
;        
;        side (* 0.01 dx)
;        fwd (if (= :right button) (* 0.01 dy) 0)
]
    (cond 
      ; Rotate
      (= :left button)       
      (.processMouse cam dx dy (/ (+ (if (pos? dx) (- dx) dx) 
                                     (if (pos? dy) (- dy) dy))
                                  5) 180 -180)      
      ; Translate
      (= :right button)
      (do (cond
            (pos? dx) (.processKeyboard (:camera @*gui-state*) dx mouse-translate-speed false false false true false false)            
            (neg? dx) (.processKeyboard (:camera @*gui-state*) (- dx) mouse-translate-speed false false true false false false))
        (cond 
          (pos? dy) (.processKeyboard (:camera @*gui-state*) dy mouse-translate-speed false false false false true false)
          (neg? dy) (.processKeyboard (:camera @*gui-state*) (- dy) mouse-translate-speed false false false false false true)))      
      ; Zoom
      (= :center button)
      (cond 
          (pos? dy) (.processKeyboard (:camera @*gui-state*) dx mouse-translate-speed true false false false false false)
          (neg? dy) (.processKeyboard (:camera @*gui-state*) (- dx) mouse-translate-speed false true false false false false)))
   (osd-view-transformation)
  state))

(defn mouse-wheel
  "Respond to a mousewheel movement. dw is +/- depending on scroll-up or down."
  [dw state]
  (let [t (get-time)]
    (cond 
      (pos? dw) (.processKeyboard (:camera @*gui-state*) dw mouse-translate-speed true false false false false false)
      (neg? dw) (.processKeyboard (:camera @*gui-state*) (- dw) mouse-translate-speed false true false false false false))
    (osd-view-transformation)
    #_(osd :msg-type :penumbra-rotate 
         :fn (fn [[dt t] state] (str "Rotation: (" 
                                     (:rot-x @*gui-state*) "," (:rot-y @*gui-state*) "," (:rot-z @*gui-state*) ")")) 
         :start-t t :stop-t (+ t 1))
    #_(osd :msg-type :penumbra-translate 
         :fn (fn [[dt t] state] (str "Translation: (" 
                                     (int (:shift-x @*gui-state*)) "," (int (:shift-y @*gui-state*)) "," (int (:shift-z @*gui-state*)) ")")) 
         :start-t t :stop-t (+ t 1))
    state))

#_(defn mouse-wheel
  "Respond to a mousewheel movement. dw is +/- depending on scroll-up or down."
  [dw state]
  (let [rads (/ (Math/PI) 180)
        thetaY (*(:rot-y state) rads)
        sY (sin thetaY)
        cY (cos thetaY)
        thetaX (* (:rot-x state) rads)
        sX (sin thetaX)
        cX (cos thetaX)
        t (get-time)]
  (swap! *gui-state* assoc
						         :shift-z (+ (:shift-z @*gui-state*) (* (/ dw 6) cY))
						         :shift-x (+ (:shift-x @*gui-state*) (* (/ dw 6) (* sY -1)))
						         :shift-y (+ (:shift-y @*gui-state*) (* (/ dw 6) sX)))
  (osd :msg-type :penumbra-rotate 
        :fn (fn [[dt t] state] (str "Rotation: (" 
                                    (:rot-x @*gui-state*) "," (:rot-y @*gui-state*) "," (:rot-z @*gui-state*) ")")) 
        :start-t t :stop-t (+ t 1))
   (osd :msg-type :penumbra-translate 
        :fn (fn [[dt t] state] (str "Translation: (" 
                                    (int (:shift-x @*gui-state*)) "," (int (:shift-y @*gui-state*)) "," (int (:shift-z @*gui-state*)) ")")) 
        :start-t t :stop-t (+ t 1))
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

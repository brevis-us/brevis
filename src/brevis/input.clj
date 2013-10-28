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
    (.processMouse cam dx dy (/ (+ (if (pos? dx) (- dx) dx) 
                                   (if (pos? dy) (- dy) dy))
                                5))
    #_(cond 
	      ; Rotate
	      (= :left button)       
       (do (.yaw (:camera @*gui-state*) dy)
         (.pitch (:camera @*gui-state*) dx))       
                       
       ; Translate
	      (= :right button)       
       (do (if (pos? dx)              
             (.strafeRight (:camera @*gui-state*) dx)
             (.strafeLeft (:camera @*gui-state*) (- dx)))
         (if (pos? dy)
           (.walkForward (:camera @*gui-state*) dy)
           (.walkBackward (:camera @*gui-state*) (- dy))))
	      ; Zoom
	      (= :center button)
	      (swap! *gui-state* assoc
	             :shift-x (+ (:shift-x @*gui-state*) (* dy (.x rot-axis)))
	             :shift-y (+ (:shift-y @*gui-state*) (* dy (.y rot-axis))
	             :shift-z (+ (:shift-z @*gui-state*) (* dy (.z rot-axis)))
	             )))
    
    #_(cond 
	      ; Rotate
	      (= :left button)
       (let [new-rot-axis (mul (normalize (add rot-axis (mul mouse-x-axis dx) (mul mouse-y-axis dy)))
                               360.0)]
	      (swap! *gui-state* assoc
              :rot-x (.x new-rot-axis)
              :rot-y (.y new-rot-axis)
              :rot-z (.z new-rot-axis)))
                       
       ; Translate
	      (= :right button)
       (let [new-pos (add cam-position (mul mouse-x-axis dx) (mul mouse-y-axis dy))]
         (swap! *gui-state* assoc
                :shift-x (.x new-pos)
                :shift-y (.y new-pos)
                :shift-z (.z new-pos)))
	      ; Zoom
	      (= :center button)
	      (swap! *gui-state* assoc
	             :shift-x (+ (:shift-x @*gui-state*) (* dy (.x rot-axis)))
	             :shift-y (+ (:shift-y @*gui-state*) (* dy (.y rot-axis))
	             :shift-z (+ (:shift-z @*gui-state*) (* dy (.z rot-axis)))
	             )))
    
    #_(swap! *gui-state* assoc
           ;:rot-x (+ (:rot-x @*gui-state*) dy)
           :rot-z (+ (:rot-z @*gui-state*) dy)
           :rot-y (+ (:rot-y @*gui-state*) dx)
           :shift-x (+ (:shift-x @*gui-state*) (* (- sX) side) (* cX fwd))
           :shift-y (+ (:shift-y @*gui-state*) (* cX side) (* sX fwd))
           :shift-z (if (= :middle button)
                           (+ (:shift-z @*gui-state*) (* 0.01 dy))
                           (:shift-z @*gui-state*)))

	    #_(cond 
	      ; Rotate
	      (= :left button)
	      (swap! *gui-state* assoc
	             :rot-x (loop [ang (+ (:rot-x @*gui-state*) dy)]
                       (cond
                         (> ang 180) (recur (- ang 360))
                         (< ang -180) (recur (+ ang 360))
                         :else ang))                       
	             :rot-y (loop [ang (+ (:rot-y @*gui-state*) dy)]
                       (cond
                         (> ang 180) (recur (- ang 360))
                         (< ang -180) (recur (+ ang 360))
                         :else ang)))                       
       ; Translate
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
	             ))
   (osd :msg-type :penumbra-rotate 
        :fn (fn [[dt t] state] (str "Rotation: (" 
                                    (:rot-x @*gui-state*) "," (:rot-y @*gui-state*) "," (:rot-z @*gui-state*) ")")) 
        :start-t t :stop-t (+ t 1))
   (osd :msg-type :penumbra-translate 
        :fn (fn [[dt t] state] (str "Translation: (" 
                                    (int (:shift-x @*gui-state*)) "," (int (:shift-y @*gui-state*)) "," (int (:shift-z @*gui-state*)) ")")) 
        :start-t t :stop-t (+ t 1)))
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

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
  (:use [brevis globals display utils osd]
        [brevis.physics utils]))

#_(def shift-key-down (atom false))
(defn sin [n] (float (Math/sin n)))
(defn cos [n] (float (Math/cos n)))

(defn key-press
  "Update the state in response to a keypress."
  [key state]
  ;(println "key-press" key)
  (cond
    ;(= :lshift key) (do (reset! shift-key-down true) state)
    ;(= "z" key) (do (reset! shift-key-down true) state)    
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
  ;(cond
    ;(= :lshift key) (reset! shift-key-down false))
    ;(= "z" key) (reset! shift-key-down false))
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
        t (get-time)
        
        side (* 0.01 dx)
        fwd (if (= :right button) (* 0.01 dy) 0)]
    (swap! *gui-state* assoc
           :rot-x (+ (:rot-x @*gui-state*) dy)
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

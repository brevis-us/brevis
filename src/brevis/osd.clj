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

(ns brevis.osd
  (:require [penumbra.text :as text])
  (:use [brevis globals]
        [brevis.physics utils]))

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
  (osd :msg-type :penumbra :fn (fn [[dt t] state] (str (float (get-time)) " time")) :start-t 0 :stop-t -1)
  #_(osd :msg-type :brevis :fn #(str (int (count @*objects*)) " objs") :start-t 0 :stop-t 5))

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

(ns brevis.osd
  (:import (java.awt Font)
           (org.newdawn.slick TrueTypeFont Color))
  (:use [brevis globals]
        [brevis.graphics multithread]
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
  (osd :msg-type :brevis :fn (fn [] (str (int (/ 1 (get-dt))) " fps")) :start-t 0 :stop-t -1)
  (osd :msg-type :brevis :fn (fn [] (str (float (get-time)) " sim time")) :start-t 0 :stop-t -1)
  (osd :msg-type :brevis :fn (fn [] (str (float (get-wall-time)) " wall time")) :start-t 0 :stop-t -1)
  #_(osd :msg-type :penumbra :fn (fn [[dt t] state] "Keys: Esc - quit / w - forward / s - backward / a - left / d - right / c - up / leftshift - down ") :start-t 0 :stop-t 10)
  #_(println "Keys: Esc - quit / w - forward / s - backward / a - left / d - right / c - up / leftshift - down ")
  #_(osd :msg-type :brevis :fn #(str (int (count @*objects*)) " objs") :start-t 0 :stop-t 5))

(def font (atom nil)) 

(defn init-font
  "Initailize fonts."
  []
  (begin-with-graphics-thread)
  (reset! font (let [awtFont (Font. "Times New Roman" Font/BOLD 24)
                     font (TrueTypeFont. awtFont false)]      
                 font))
  (end-with-graphics-thread))

(defn write-to-screen
  "Write text to the screen. (Needs to be called with graphics thread bindings)"
  [text x y]
  (begin-with-graphics-thread)
  (.bind Color/white)
  (.drawString @font x y text Color/black)
  #_(end-with-graphics-thread))

(defn update-display-text
   "Update the onscreen displayed (OSD) text."
   []
   (when-not @font (init-font))   
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
                        (= (:msg-type msg) :brevis) ((:fn msg))
                        :else (str "OSD msg type:" (:msg-type msg) "not recognized"))]
             (write-to-screen text x y)))
         (when stopped?
           (remove-osd-by-uid uid))
         (recur (cond
                  (= (:msg-type msg) :penumbra-rotate) 
                  (filter #(not= (:msg-type (second %)) :penumbra-rotate) (rest msgs))
                  (= (:msg-type msg) :penumbra-translate) 
                  (filter #(not= (:msg-type (second %)) :penumbra-translate) (rest msgs))
                  :else (rest msgs))
                x (+ y 30))))))


(ns brevis.globals
  (:import [brevis.graphics BrCamera]))

(def enable-display-text (atom true))

(def default-gui-state {:fullscreen false
                        ;:camera (BrCamera. 300 300 -50 90 -70 45 60 (/ 4 3) 0.1 4000)
                        ;:camera (BrCamera. 300 300 -50 162 -56 0 60 (/ 4 3) 0.1 4000)
                        :camera (BrCamera. 100 50 -50 0 -90 0 60 640 480 0.1 4000)
                        :gui true
                        ;:input (BrInput.)
                        ;:rot-x 90 :rot-y -90 :rot-z -45
                        ;:shift-x 300 :shift-y 300 :shift-z -50;-30                                   
                        :last-report-time 0 :simulation-time 0})
  
(def #^:dynamic *gui-state* (atom default-gui-state))
(def #^:dynamic *gui-message-board* (atom (sorted-map))) 
(def #^:dynamic *app-thread* (atom nil))
(def #^:dynamic *screenshot-filename* (atom nil))
(def #^:dynamic *simulation-state* (atom {}))
(def #^:dynamic *graphics* (atom {}))
(def destroy-hooks (atom []))

;(def #^:dynamic *brevis-params* (atom {}))
;(def #^:dynamic *brevis-state* (atom {}))

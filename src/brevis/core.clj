(ns brevis.core
  (:use [brevis.init]; ew.....
        [brevis globals utils input osd display vector parameters]
        [brevis.graphics basic-3D multithread]
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
           (javax.imageio ImageIO)))

;; ## Window and Graphical Environment

(defn init-view
 "Initialize the gui-state global to the default."
 []
 (reset! *gui-state* default-gui-state))

;; ## Start a brevis instance

;; Yeesh... There must be a better way than this
(declare simulate)
(defn start-gui 
  "Start the simulation with a GUI."
  ([initialize]
    (start-gui initialize java-update-world))    
  ([initialize update]
    (start-gui initialize update default-input-handlers))
  ([initialize update input-handlers]
    (reset! *gui-message-board* (sorted-map))
    ;; Load graphics dependencies now
    (use 'brevis.graphics.core)
    ;;
	  (reset! *app-thread*
           (Thread. (fn [] (simulate initialize update input-handlers))))
   (.start @*app-thread*)))

;; ## Non-graphical simulation loop (may need updating) (comment may need updating)

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
      (if (or (and (:terminated? state)
                   (:close-on-terminate @params))
              (:close-requested @*gui-state*));; shouldnt be using gui state for this
        (do (println "Halting.")
          state
          (doseq [dh @destroy-hooks] (dh))
          (System/exit 0))
        (recur ((:update state) [t (get-dt)] state)
               (+ t (get-dt))
               (if (> t (+ twrite write-interval)) t twrite)
               (if (> t (+ twrite write-interval)) (java.lang.System/nanoTime) wallwrite))))))

(defn start-nogui 
  "Start the simulation with a GUI."
  ([initialize]
    (start-nogui initialize java-update-world #_update-world))
  ([initialize update]    
	  (simulation-loop
	   {:init initialize, :update update})))      

(defn autostart-in-repl
  "Autostart a function if we're in a REPL environment."
  [fn]
  ;; For autostart with Counterclockwise in Eclipse
  (when (or (find-ns 'ccw.complete)
            #_(find-ns 'brevis.ui.core))
    (fn)))


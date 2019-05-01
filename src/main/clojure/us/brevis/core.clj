(ns us.brevis.core
  (:use [us.brevis globals utils vector]
        [us.brevis.physics core utils]
        [us.brevis.shape core box sphere cone])
  (:require [clojure.math.numeric-tower :as math]
            [brevis-utils.parameters :as params]
            [us.brevis.graphics.core :as graphics])
  (:import (graphics.scenery SceneryBase)))

;; ## Window and Graphical Environment

(defn init-view
 "Initialize the gui-state global to the default."
 []
 (reset! *gui-state* default-gui-state))

(defn start-gui
  "Start the simulation with a GUI."
  ([initialize]
   (SceneryBase/xinitThreads)
   (start-gui initialize java-update-world))
  ([initialize update]
   (SceneryBase/xinitThreads)
   (reset! *gui-message-board* (sorted-map))
    ;;
   (reset! *app-thread*
            (Thread. (fn [] (graphics/simulate initialize update)))); TODO move graphics simulate here, the display functions can be inputs therefore dont need to be in a graphical env
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
                   (:close-on-terminate @params/params))
              (:close-requested @*gui-state*));; shouldnt be using gui state for this
        (do (println "Halting.")
          state
          (doseq [dh @destroy-hooks] (dh))
          (when-not (params/get-param :gui)
            (System/exit 0)))
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


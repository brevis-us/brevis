(ns us.brevis.graphics.core
  (:use [us.brevis globals utils vector]
        [us.brevis.physics core utils]
        [us.brevis.shape core box sphere cone])
  (:require [clojure.math.numeric-tower :as math]
            [us.brevis.graphics.sciview :as display]))

(defn drawable?
  "Is an object drawable?"
  [obj]
  (.isDrawable ^us.brevis.BrObject obj))

(defn simulate
  "Simulation loop."
  [initialize update]
  ;; For multithreaded graphics
  ; TODO lock to sync graphics?
  (initialize)
  (let [startTime (ref (java.lang.System/nanoTime))
        fps (ref 0)
        dstate (atom (display/init))]
    (loop [step 0]             
      (when-not (:close-requested @*gui-state*)
        #_(println "Closing application.")
        (update [(* step (get-dt)) (get-dt)] {})
        (dosync (ref-set fps (inc @fps)))
        (when (and (:display-fps @*gui-state*)
                   (> (java.lang.System/nanoTime) @startTime))
          (println "Update" step "FPS:" (double (/ @fps (/ (- (+ 1000000000 (java.lang.System/nanoTime)) @startTime) 1000000000))))
          (dosync
            (ref-set startTime (+ (java.lang.System/nanoTime) 1000000000))
            (ref-set fps 0)))
        (when @dstate
         #_(when (Display/wasResized) (.setDimensions (:camera @*gui-state*) (float (Display/getWidth)) (float (Display/getHeight))))
         #_(println "fullscreen" (:fullscreen @*gui-state*) (not (Display/isFullscreen)))
         #_(when (and (:fullscreen @*gui-state*) (not (Display/isFullscreen))) (println "going fullscreen") (Display/setFullscreen true))
         #_(when (and (not (:fullscreen @*gui-state*)) (Display/isFullscreen)) (println "disable fullscreen") (Display/setFullscreen false))
         (swap! dstate display/display)
         (recur (inc step))))))
  (doseq [dh @destroy-hooks] (dh))
  ;; Should call system/exit if not using UI
  (when-not 
    (or (find-ns 'ccw.complete)
        (find-ns 'brevis.ui.core));; if we're in CCW or the Brevis IDE, don't exit.
    (System/exit 0)));; exit only when not using a repl-mode


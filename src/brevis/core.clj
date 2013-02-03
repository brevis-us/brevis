(ns brevis.core
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [cantor]
        [brevis.graphics.basic-3D]
        [brevis.physics.space]
        [brevis.shape core box])       
  (:require [penumbra.app :as app]
            [penumbra.text :as text]
            [penumbra.data :as data]
            [cantor.range]
            [penumbra.opengl.frame-buffer :as fb]))

(def update-handlers
  (atom {}))

(defn add-update-handler
  "Associate an update function with a type. An update function should take 3                                                                                            
arguments: [object dt neighbors] and return an updated version of object                                                                                                 
given that dt amount of time has passed."
  [type handler-fn]
  (swap! update-handlers assoc type handler-fn))

;; This should probably technically go somewhere else, but I demand that core functionality be provided with (:use [brevis.core])
(defn update-objects
  "Update all objects in the simulation. Objects whose update returns nil                                                                                                
are removed from the simulation."
  [objects dt]
  (let [updated-objects (doall (for [obj objects]
;                                 ((get @update-handlers (:type obj)) obj dt (remove #{obj} objects))))                                                                  
                                   (let [f (get @update-handlers (:type obj))]
                                     ;(println (get @update-handlers (:type obj)) obj dt (remove #{obj} objects))                                                        
                                     (if f
                                       (f obj dt (remove #{obj} objects))
                                       obj))))
	singles (filter #(not (seq? %)) updated-objects);; These objects didn't produce children                                                                         
        multiples (apply concat (filter seq? updated-objects))];; These are parents and children                                                                         
    (keep identity (concat singles multiples))))


#_(defn update-objects
  "Update all objects in the simulation. Objects whose update returns nil
are removed from the simulation."
  [objects dt]
  (let [updated-objects (doall (for [obj objects]
                                 ((@update-handlers (:type obj)) obj dt (remove #{obj} objects))))
        singles (filter #(not (seq? %)) updated-objects);; These objects didn't produce children
        multiples (apply concat (filter seq? updated-objects))];; These are parents and children
    (keep identity (concat singles multiples))))

(defn update-world
  "Update the world."
  [[dt time] state]
  (when (and  state
              (not (:terminated? state)))
          (assoc state
            :simulation-time (+ (:simulation-time state) (:dt state))
            :objects (handle-collisions (update-objects (:objects state) (:dt state))
                                                        @collision-handlers))))

(defn init [state]
  (app/title! "brevis")
  (app/vsync! true)
  (app/key-repeat! false)
  (enable :blend)
  (enable :depth-test)
  (init-box-graphic)
  (enable :lighting)
  (enable :light0)
;  (gl-enable :auto-normal)
  (blend-func :src-alpha :one-minus-src-alpha)
  #_(reset-simulation state))

(defn reshape
  "Reshape after the window is resized."
  [[x y w h] state]
  (frustum-view 45 (/ w h) 0.1 1000)
  (load-identity)
  #_(translate 0 0 -40)
  (light 0 :position [1 100 1 0])
  (assoc state
    :window-width w
    :window-height h))

(defn display
  "Dispaly the world."
  [[dt time] state]
  (text/write-to-screen (str (int (/ 1 dt)) " fps") 0 0)
  (text/write-to-screen (str (:simulation-time state) " time") 0 30)
  (text/write-to-screen (str (count (filter #(= (:type %) :bird) (:objects state))) " birds") 0 60)
  (rotate (:rot-x state) 1 0 0)
  (rotate (:rot-y state) 0 1 0)
  (translate 0 0 (:shift-z state))
  (with-disabled :texture-2d
    (doseq [obj (:objects state)]
      (draw-shape obj)))
  (app/repaint!))

(defn mouse-drag
  "Rotate the world."
  [[dx dy] _ button state]
  (cond 
    ; Rotate
    (= :left button)
    (assoc state
           :rot-x (+ (:rot-x state) dy)
           :rot-y (+ (:rot-y state) dx))
    ; Zoom
    (= :right button)
    (assoc state
           :shift-y (+ (:shift-z state)
                       dy)
           :shift-z (+ (:shift-z state)
                      dx))))

;; Screenshot code

;; (import java.io.File)
;; (import java.awt.Color)
;; (import java.awt.image.BufferedImage)
;; (import javax.imageio.ImageIO)

;; (defn screenshot
;;   "Take a screenshot."
;;   [state]
;;   (let [bi (BufferedImage. (:window-width state) (:window-height state) BufferedImage/TYPE_INT_ARGB)]
;;     (def pixels (fb/gl-read-pixels ))))

;; end screenshot code
   
(defn key-press [key state]
  (cond
;   (= "s" key) (screenshot state)
;   (= "r" key) (reset-simulation state)
   (= "p" key) (do (app/pause!)
                 state)
   (= :escape key)
   (do (app/stop!)
     (assoc state 
          :terminated? true)
          )))

(defn start-gui [initialize update dt]
  "Start the simulation with a GUI."
  (app/start
   {:reshape reshape, :init init, :mouse-drag mouse-drag, :key-press key-press, :update update, :display display}
   (assoc (initialize)
;       :mouse-mode [:rot-x :rot-y]                                                                                                                                      
       :rot-x 0 :rot-y 0 :rot-z 0
       :shift-x 0 :shift-y 20 :shift-z -30
       :init-simulation initialize
	                 :dt dt
                         :last-report-time 0
                         :simulation-time 0)))

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
    


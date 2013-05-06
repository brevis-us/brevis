(ns brevis.physics.collision
  (:gen-class)
  (:import (org.ode4j.ode OdeHelper DSapSpace OdeConstants DContactBuffer DGeom DFixedJoint DContactJoint))
  (:import (org.ode4j.math DVector3))
  (:import java.lang.Math)  
  (:use [cantor]
        [penumbra.opengl]
        [brevis.shape core box]
        [brevis.physics core])
  (:require [cantor.range]))

(defn add-collision-handler
  "Store the collision handler for typea colliding with typeb."
  [typea typeb handler-fn]
  (swap! *collision-handlers* assoc
         [typea typeb] handler-fn))

(defn collided?
  "Have two objects collided?"
  [obj1 obj2]
  (contains? @*collisions* [(:uid obj1) (:uid obj2)]))

;; This callback is triggered by the physics engine. Currently the function does not technically
;; check for a collision, it only uses ODE's collision predictor.
(def nearCallback
  (proxy [org.ode4j.ode.DGeom$DNearCallback] []
    (call [#^java.lang.Object data #^DGeom o1 #^DGeom o2]
      (let [b1 (.getBody o1)
            b2 (.getBody o2)]
        (reset! *collisions* (concat @*collisions* 
                                     (list [(.getData b1) (.getData b2)]
                                           [(.getData b2) (.getData b1)])))))))

; collider override
(defn handle-collisions
  "Handle the collisions of a collection of real objects.
Things is updated and returned as a vector."
  [things collision-handlers]
  (loop [things (vec things)
         pairs (for [uid-pair @*collisions*]                                                                                                                         
                    [(some #(when (= (:uid (nth things %)) (:uid (first uid-pair))) %) (range (count things)))
                     (some #(when (= (:uid (nth things %)) (:uid (second uid-pair))) %) (range (count things)))
                     uid-pair])]
    (when (some nil? (first pairs)) 
      (println (first pairs)) 
      (println @*collisions*) 
      (println (doall (map :uid things)))
      (println (doall (map :type things))))
    
    (if (empty? pairs)
      things
      (recur (let [pair (first pairs)
                   thing1 (nth things (first pair))
                   thing2 (nth things (second pair))
                   collision-handler (get collision-handlers [(:type thing1) (:type thing2)])]
               #_(println pair (:type thing1) (:type thing2)) 
               #_(println "Collision-handler" collision-handler [(:type thing1) (:type thing2)] (nil? collision-handler)) 
               (cond (apply = pair); self-collision, somehow 
                     things
                     (not (nil? collision-handler))
                     (let [[thing1 thing2] (collision-handler thing1 thing2)]
                       #_
                         (println "Colliding" pair (:type thing1) (:type thing2))                                                                                                              
                       (assoc things
                         (first pair) thing1
                         (second pair) thing2))
                     :else things))    
             (rest pairs)))))

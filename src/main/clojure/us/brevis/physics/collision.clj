(ns us.brevis.physics.collision
  (:gen-class)
  (:use [us.brevis.shape core box]
        [us.brevis.physics core])
  (:import (us.brevis BrObject)))

(defn add-collision-handler
  "Store the collision handler for typea colliding with typeb."
  [typea typeb handler-fn]
  (let [ch (proxy [us.brevis.CollisionHandler] []
             (collide [#^us.brevis.Engine engine #^BrObject subj #^BrObject othr #^Double dt]
               (handler-fn subj othr)))]
    (.addCollisionHandler @*java-engine* (str (name typea)) (str (name typeb)) ch)))    

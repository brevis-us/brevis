(ns brevis.physics.collision
  (:gen-class)
  (:import (org.ode4j.ode OdeHelper DSapSpace OdeConstants DContactBuffer DGeom DFixedJoint DContactJoint))
  (:import (org.ode4j.math DVector3))
  (:import java.lang.Math)
  (:import (brevis Engine BrPhysics BrObject))  
  (:use [brevis.shape core box]
        [brevis.physics core]))

#_(def nearCallback  (proxy [org.ode4j.ode.DGeom$DNearCallback] []    (call [#^java.lang.Object data #^DGeom o1 #^DGeom o2]      (let [b1 (.getBody o1)            b2 (.getBody o2)]        (reset! *collisions* (concat @*collisions*                                     (list [(.getData b1) (.getData b2)]                                           [(.getData b2) (.getData b1)])))))))

(defn add-collision-handler
  "Store the collision handler for typea colliding with typeb."
  [typea typeb handler-fn]
  (let [ch (proxy [brevis.Engine$CollisionHandler] []             
             (collide [#^brevis.Engine engine #^BrObject subj #^BrObject othr #^Double dt]
               (handler-fn subj othr)))]
    (.addCollisionHandler @*java-engine* (str (name typea)) (str (name typeb)) ch)))    

#_(defn collided?
   "Have two objects collided?"
   [obj1 obj2]
   (contains? @*collisions* [(:uid obj1) (:uid obj2)]))

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

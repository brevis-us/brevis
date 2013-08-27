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

(ns brevis.physics.utils
  (:import (org.ode4j.ode OdeHelper DSapSpace OdeConstants DContactBuffer DGeom DFixedJoint DContactJoint))
  (:import (org.ode4j.math DVector3))
  (:import java.lang.Math)  
  (:import (brevis Engine BrPhysics BrObject))
  (:use ;[cantor]
        [penumbra.opengl]
        [brevis.shape core box]
        [brevis.physics core vector])
  (:require [cantor.range]))  

;; from lspector's taggp (avail on github)
(defn pmapall
  "Like pmap but: 1) coll should be finite, 2) the returned sequence
   will not be lazy, 3) calls to f may occur in any order, to maximize
   multicore processor utilization, and 4) takes only one coll so far."
  [f coll]
  (if @*brevis-parallel*
    (map f coll)
    (let [agents (map #(agent % :error-handler (fn [agnt except] (println except))) coll)]
      (dorun (map #(send % f) agents))
      (apply await agents)
      (doall (map deref agents)))))

#_(defn get-world
  "Return the current world"
  []
  (:world @*physics*))

(defn get-world
  "Return the current world"
  []
  (.getWorld @*java-engine*))

#_(defn get-time
  "Return the current time."
  []
  (:time @*physics*))

(defn get-time
  "Return the current time."
  []
  (.getTime @*java-engine*))

#_(defn add-object
  "Add an object to the current world."
  [obj]
  (swap! *added-objects* assoc (:uid obj) obj))

#_(defn map-to-brobject
  "Convert a hash map to a BrObject."
  [obj]
  (let [brobj (BrObject.)]    
    (.setUID brobj (long (:uid obj)))
    (.setType brobj (str (name (:type obj))));; NOT COMPLETE
    brobj))

(defn get-uid
  "Return the UID of an object."
  [obj]
  (.getUID obj))

(defn add-object
  "Add an object to the current world."
  [obj]
  (.addObject @*java-engine* (get-uid obj) obj))

#_(defn del-object
  "Delete an object to the current world."
  [obj]
  (swap! *deleted-objects* conj (:uid obj)))

(defn del-object
  "Add an object to the current world."
  [obj]
  (.deleteObject @*java-engine* (get-uid obj)))

#_(defn add-object*
  "(Internal version, use add-object) Add an object to the current world."
  [obj]
  (swap! *objects* assoc (:uid obj) obj))

(defn add-update-handler
  "Associate an update function with a type."
  [type handler-fn]
  (let [uh (proxy [brevis.Engine$UpdateHandler] []
				    (update [#^brevis.Engine engine #^Long uid #^Double dt]
          #_(println "inside an update handler" uid dt)
              (let [obj (.getObject engine uid)]
                (handler-fn obj dt (.getNeighbors obj)))))]
    (.addUpdateHandler @*java-engine* (str (name type)) uh)))
            
(defn odevec-to-vec3
  "Convert an ODE vector into a Cantor vector."
  [ov]
  (vec3 (.get0 ov) (.get1 ov) (.get2 ov)))

(defn vec3-to-odevec
  "Convert a Cantor vector into an ODE vector."
  [v3]
  (DVector3. (.x v3) (.y v3) (.z v3)))

#_(defn set-velocity
  "Set the velocity of an object"
  [obj v]
  (.setLinearVel (:body obj) (vec3-to-odevec v))
  obj)

(defn set-velocity
  "Set the velocity of an object"
  [obj v]
  (.setVelocity obj v)
  obj)

#_(defn get-position
  "Return the position of an object"
  [obj]
  (odevec-to-vec3 (.getPosition (:body obj))))

(defn get-position
  "Return the position of an object"
  [obj]
  (.getPosition obj))

#_(defn get-velocity
  "Return the velocity of an object"
  [obj]
  (odevec-to-vec3 (.getLinearVel (:body obj))))

(defn get-velocity
  "Return the velocity of an object"
  [obj]
  (.getVelocity obj))

(defn get-acceleration
  "Return the acceleration of an object."
  [obj]
  (.getAcceleration obj))

(defn set-acceleration
  "Set the acceleration of an object."
  [obj v]
  (.setAcceleration obj v)
  obj)

(defn get-body
  "Return the physics body of an object."
  [obj]
  (.getBody obj))

(defn obj-to-mass
  "Create an ODE mass for an object"
  [obj]
  (let [dim (:dim (:shape obj))]    
      (cond
        (= (:type (:shape obj)) :box) 
        (doto (OdeHelper/createMass)           
          (.setBox (:density obj) (.x dim) (.y dim) (.z dim)))
        :else;(= (:type (:shape obj)) :sphere)
        (doto (OdeHelper/createMass)          
          (.setSphere (:density obj) (.x dim))))))

(defn obj-to-geom
  "Create an ODE geometry for a obj"
  [obj]
  (let [dim (:dim (:shape obj))]
    (cond
      (= (:type (:shape obj)) :cone) (OdeHelper/createSphere (:space @*physics*) (.x dim));; the cake is a lie
      (= (:type (:shape obj)) :box) (OdeHelper/createBox (:space @*physics*) (.x dim) (.y dim) (.z dim))
      (= (:type (:shape obj)) :sphere) (OdeHelper/createSphere (:space @*physics*) (.x dim)))))
    
(defn obj-to-nbr-geom
  [obj]
  (let [dim (:dim (:shape obj))]
    (obj-to-geom (assoc-in obj [:shape :dim] (+ dim (* 2 (vec3 @*neighborhood-radius* @*neighborhood-radius* @*neighborhood-radius*)))))))

(defn get-dt
  []
  @*dt*)

(defn set-dt
  [new-dt]
  (reset! *dt* new-dt)) 

(defn obj-distance
  "Return the distance between two objects, this is preferable because faster lookups can be standardized."
  [me other]
  (.distance 
    (.getPosition (:body me)) 
    (.getPosition (:body other))))
;  (length (sub (get-position me) (get-position other))))

#_(defn get-object
  "Return the object by UID"
  [uid]
  (get @*objects* uid))

(defn get-object
  "Return the object by UID"
  [uid]
  (.getObject @*java-engine*))

(defn set-object
  "Set the object at UID to a new version."
  [uid new-obj]
  ; should check if new-obj has the right uid
  (swap! *objects* assoc uid new-obj))

#_(defn get-neighbor-objects
  "Return the objects of a neighborhood."
  [obj]
  (map #(get @*objects* %) (:neighbors obj)))

(defn get-neighbor-objects
  "Return the objects of a neighborhood."
  [obj]
  #_(println (.getNeighbors obj))
  (map #(.getObject @*java-engine* %) (.getNeighbors obj)))

(defn set-neighborhood-radius
  "Set the neighborhood radius."
  [new-radius]
  (.setNeighborhoodRadius @*java-engine* (double new-radius))
  #_(reset! *neighborhood-radius* new-radius))

;; the following 2 functions from ztellman's cantor (see github)
(defn radians
  "Transforms degrees to radians."
  [x]
  (* (/ Math/PI 180.0) (double x)))

(defn degrees
  "Transforms radians to degrees."
  [x]
  (* (/ 180.0 Math/PI) (double x)))


(defn get-color
  "Return the color of an object."
  [obj]
  (.getColor obj))

(defn set-color
  "Return the color of an object."
  [obj col]
  (.setColor obj col)
  obj)

(defn get-rotation
  "Return the rotation of an object."
  [obj]
  (.getRotation obj))

(defn get-dimension
  "Return the dimension of an object."
  [obj]
  (.getDimension obj))

(defn get-mass
  "Return the mass of an object."
  [obj]
  (.getMass obj))

(defn get-double-mass
  "Return the mass of an object."
  [obj]
  (.getDoubleMass obj))

(defn get-texture
  "Return the texture of an object."
  [obj]
  (.getTexture obj))

(defn set-texture
  "set the texture of an object."
  [obj new-tex]
  (.setTexture obj new-tex)
  obj)


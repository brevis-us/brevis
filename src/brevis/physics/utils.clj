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
  (:use [cantor]
        [penumbra.opengl]
        [brevis.shape core box]
        [brevis.physics core])
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

(defn get-world
  "Return the current world"
  []
  (:world @*physics*))

(defn get-time
  "Return the current time."
  []
  (:time @*physics*))

(defn add-object
  "Add an object to the current world."
  [obj]
  (swap! *added-objects* assoc (:uid obj) obj))

(defn del-object
  "Add an object to the current world."
  [obj]
  (swap! *deleted-objects* conj (:uid obj)))

#_(defn add-object*
  "(Internal version, use add-object) Add an object to the current world."
  [obj]
  (swap! *objects* assoc (:uid obj) obj))

(defn add-update-handler
  "Associate an update function with a type."
  [type handler-fn]
  (swap! *update-handlers* assoc type handler-fn))
            
(defn odevec-to-vec3
  "Convert an ODE vector into a Cantor vector."
  [ov]
  (vec3 (.get0 ov) (.get1 ov) (.get2 ov)))

(defn vec3-to-odevec
  "Convert a Cantor vector into an ODE vector."
  [v3]
  (DVector3. (.x v3) (.y v3) (.z v3)))

(defn set-velocity
  "Set the velocity of an object"
  [obj v]
  (.setLinearVel (:body obj) (vec3-to-odevec v))
  obj)

(defn get-position
  "Return the position of an object"
  [obj]
  (odevec-to-vec3 (.getPosition (:body obj))))

(defn get-velocity
  "Return the velocity of an object"
  [obj]
  (odevec-to-vec3 (.getLinearVel (:body obj))))

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

(defn get-object
  "Return the object by UID"
  [uid]
  (get @*objects* uid))

(defn set-object
  "Set the object at UID to a new version."
  [uid new-obj]
  ; should check if new-obj has the right uid
  (swap! *objects* assoc uid new-obj))

(defn get-neighbor-objects
  "Return the objects of a neighborhood."
  [obj]
  (map #(get @*objects* %) (:neighbors obj)))

(defn set-neighborhood-radius
  "Set the neighborhood radius."
  [new-radius]
  (reset! *neighborhood-radius* new-radius))

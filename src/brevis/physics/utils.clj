(ns brevis.physics.utils
  (:import (org.ode4j.ode OdeHelper DSapSpace OdeConstants DContactBuffer DGeom DFixedJoint DContactJoint))
  (:import (org.ode4j.math DVector3))
  (:import java.lang.Math)  
  (:use [cantor]
        [penumbra.opengl]
        [brevis.shape core box]
        [brevis.physics core])
  (:require [cantor.range]))  

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

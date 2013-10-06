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
  (:use [penumbra.opengl]        
        [brevis vector]
        [brevis.shape core box]        
        [brevis.physics core]))

(defn get-world
  "Return the current world"
  []
  (.getWorld @*java-engine*))

(defn get-time
  "Return the current time."
  []
  (.getTime @*java-engine*))

(defn get-uid
  "Return the UID of an object."
  [obj]
  (.getUID obj))

(defn add-object
  "Add an object to the current world."
  [obj]
  (.addObject @*java-engine* (get-uid obj) obj) 
  obj)

(defn del-object
  "Add an object to the current world."
  [obj]
  (.deleteObject @*java-engine* (get-uid obj)))

(defn add-update-handler
  "Associate an update function with a type."
  [type handler-fn]
  (let [uh (proxy [brevis.Engine$UpdateHandler] []
				    (update [#^brevis.Engine engine #^Long uid #^Double dt]
          #_(println "inside an update handler" uid dt)
              (let [obj (.getObject engine uid)]
                (handler-fn obj dt (.getNeighbors obj)))))]
    (.addUpdateHandler @*java-engine* (str (name type)) uh)))

(defn add-global-update-handler
  "Add a global update handler with specified priority."
  [priority handler-fn]
  (let [gh (proxy [brevis.Engine$GlobalUpdateHandler] []
             (update [#^brevis.Engine engine]
               (handler-fn)))]
    (.addGlobalUpdateHandler @*java-engine* priority gh)))

#_(defn param-label
  "Convert a string into an ODE param label."
  [s]
  (cond
    (= s "dParamVel2") dParamVel2
    (= s "dParamVel") dParamVel
    (= s "dParamFMax2") dParamFMax2
    (= s "dParamFMax") dParamFMax
    (= s "dParamLoStop") dParamLoStop
    (= s "dParamHiStop") dParamHiStop
    (= s "dParamFudgeFactor") dParamFudgeFactor))

#_(defn set-joint-param
  "Set a joint parameter."
  [joint param-name value]
  (let [param-fn (cond (= :hinge2 (:joint-type joint)) .dJointSetHinge2Param)]
    (param-fn (param-label param-name) value)))

(defn set-joint-vel
  "Set the velocity of a joint."
  [joint value] 
  #_(println "set-joint-vel" (.getJoint joint) value) 
  (cond (= "hinge2" (.getType joint))    
    (.setParamVel (.getJoint joint) value)))

(defn enable-kinematics-update
  "Enable automatic kinematics for this type."
  [type]
  (.enableUpdateKinematics @*java-engine* (str (name type))))
            
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
  (.setVelocity obj v)
  obj)

(defn get-position
  "Return the position of an object"
  [obj]
  (.getPosition obj))

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
    (.getPosition (get-body me)) 
    (.getPosition (get-body other))))

(defn get-object
  "Return the object by UID"
  [uid]
  (.getObject @*java-engine* uid))

(defn set-object
  "Set the object at UID to a new version."
  [uid new-obj]
  (.setObject @*java-engine* uid new-obj))

(defn get-neighbor-objects
  "Return the objects of a neighborhood."
  [obj]
  (let [nbrs (.getNeighbors obj)]
    (when nbrs
      (map #(get-object %)
           (seq (.toArray nbrs))))))

(defn set-neighborhood-radius
  "Set the neighborhood radius."
  [new-radius]
  (.setNeighborhoodRadius @*java-engine* (double new-radius)))

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

(defn get-type
  "Return the type of an object."
  [obj]
  (.getType obj))

(defn set-texture
  "set the texture of an object."
  [obj new-tex]
  (.setTexture obj new-tex)
  obj)

(defn set-texture-image
  "set the texture of an object to a bufferedimage."
  [obj new-tex-img]
  (.setTextureImage obj new-tex-img)
  obj)


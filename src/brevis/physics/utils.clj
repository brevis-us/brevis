(ns brevis.physics.utils
  (:import (org.ode4j.ode OdeHelper DSapSpace OdeConstants DContactBuffer DGeom DFixedJoint DContactJoint))
  (:import (org.ode4j.math DVector3))
  (:import (org.lwjgl.util.vector Vector3f Vector4f))
  (:import java.lang.Math)  
  (:import (brevis Engine BrPhysics BrObject))
  (:use [brevis vector math utils]
        [brevis.shape core box]        
        [brevis.graphics multithread]
        [brevis.physics core]))

(defn get-world
  "Return the current world"
  []
  (.getWorld ^Engine @*java-engine*))

(defn get-space
  "Return the current physical space being simulated"
  []
  (.getSpace ^Engine @*java-engine*))

(defn get-contact-group
  "Return the current joints being used by entities."
  []
  (.getJoints ^Engine @*java-engine*))

(defn get-mass 
  "Return the mass object for an object."
  [^BrObject obj]
  (.getMass obj))



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

;; # Joints

(defn make-joint-hinge
  "Make a joint that connects 2 real objects. 
o1 is the first object, o2 the second
pos-o1 is the location on the first object to connect to
axis is the axis about which the joint rotates"
  [o1 o2 pos-o1 axis]
  #_(println (class o1) (class o2) (class pos-o1))
  (.jointHinge (.getPhysics @*java-engine*)
    o1 o2 pos-o1 axis))

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
  [^BrObject obj ^Vector3f v]
  (.setVelocity obj v)
  obj)

(defn get-position
  "Return the position of an object"
  [^BrObject obj]
  (.getPosition obj))

(defn get-velocity
  "Return the velocity of an object"
  [^BrObject obj]
  (.getVelocity obj))

(defn get-acceleration
  "Return the acceleration of an object."
  [^BrObject obj]
  (.getAcceleration obj))

(defn set-acceleration
  "Set the acceleration of an object."
  [^BrObject obj ^Vector3f v]
  (.setAcceleration obj v)  
  obj)

(defn get-body
  "Return the physics body of an object."
  [^BrObject obj]
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

(defn get-geom
  "Return an object's geometry structure."
  [obj]
  (.getGeom obj))

(defn obj-distance
  "Return the distance between two objects, this is preferable because faster lookups can be standardized."
  [^BrObject me ^BrObject other]
  (.distance 
    (.getPosition (get-body me)) 
    (.getPosition (get-body other))))

(defn get-neighbor-objects
   "Return the objects of a neighborhood."
   [^BrObject obj]
   (let [nbrs (.getNeighbors obj)
         obj-uid (get-uid obj)]
     (when nbrs
       (map #(get-object %)
            (filter (partial not= obj-uid) nbrs)))))

(defn get-closest-neighbor
   "Return the objects of a neighborhood."
   [^BrObject obj]
   (let [nbr-uid (.getClosestNeighbor obj)]
     (when-not (or (nil? nbr-uid) (zero? nbr-uid))
       (get-object nbr-uid))))

(defn set-neighborhood-radius
  "Set the neighborhood radius."
  [new-radius]
  (.setNeighborhoodRadius ^Engine @*java-engine* (double new-radius)))

(defn get-neighborhood-radius
  "Get the neighborhood radius."
  []
  (.getNeighborhoodRadius ^Engine @*java-engine*))


#_(defn radians
   "Transforms degrees to radians."
   [x]
   (* (/ Math/PI 180.0) (double x)))

#_(defn degrees
   "Transforms radians to degrees."
   [x]
   (* (/ 180.0 Math/PI) (double x)))

(defn get-color
  "Return the color of an object."
  [^BrObject obj]
  (.getColor obj))

(defn set-color
  "Return the color of an object."
  [^BrObject obj ^Vector4f col]
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
  [^BrObject obj]
  (.getMass obj))

(defn get-double-mass
  "Return the mass of an object."
  [^BrObject obj]
  (.getDoubleMass obj))

#_(defn get-closest-object
   "Return the closest object to a vector from a list of objects."
   [pos objs]
   (let [dists (map #(length-vec3 (sub-vec3 (get-position %) pos)) objs)]
     (when-not (empty? objs)
       (nth objs
            (reduce #(if (< (nth dists %1) (nth dists %2)) %1 %2) (range (count dists)))))))

(defn get-closest-object
   "Return the closest object to a vector from a list of objects."
   [pos objs]
   (let [dists (map #(length-vec3 (sub-vec3 (get-position %) pos)) objs)]         
     (when-not (empty? objs)
       (nth objs
            (first (apply min-key second (map-indexed vector dists)))))))
    

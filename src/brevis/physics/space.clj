;; This is for simulations that require space (such as physical or pseudo-physical worlds)
(ns brevis.physics.space
  (:import (org.ode4j.ode OdeHelper DSapSpace OdeConstants DContactBuffer DGeom DFixedJoint DContactJoint))  (:import (org.ode4j.math DVector3))  (:import java.lang.Math)  
  (:use [cantor]
        [brevis.shape core box])
  (:require [cantor.range]))

(def collision-handlers
  (atom {}))

(def update-handlers
  (atom {}))

(defn add-update-handler
  "Associate an update function with a type. An update function should take 3                                                                                            
arguments: [object dt neighbors] and return an updated version of object                                                                                                 
given that dt amount of time has passed."
  [type handler-fn]
  (swap! update-handlers assoc type handler-fn))

(def simulation-boundary
  (box3 (vec3 100 100 100)
        (vec3 -100 -100 -100)))

(def #^:dynamic *physics* (atom nil))
(def #^:dynamic *collisions* (atom #{}))

(defn get-world
  "Return the current world"
  []
  (:world @*physics*))

(defn add-collision-handler
  "Store the collision handler for typea colliding with typeb.                                                                                                           
Collision functions take [collider collidee] and return [collider collidee]                                                                                              
Both can be modified; however, two independent collisions are actually computed [a b] and [b a]."
  [typea typeb handler-fn]
  (swap! collision-handlers assoc
         [typea typeb] handler-fn))
            
(defn obj-to-mass
  "Create an ODE mass for an object"
  [obj]
  (let [dim (:dim (:shape obj))]
    (doto (OdeHelper/createMass)
      (.setBox (:density obj) (.x dim) (.y dim) (.z dim)))))

(defn obj-to-geom
  "Create an ODE geometry for a obj"
  [obj]
  (let [dim (:dim (:shape obj))
        pos (:position obj)]
    (OdeHelper/createBox (:space *physics*) (.x dim) (.y dim) (.z dim))))

(defn make-real
  "Add Real attributes to an object map."
  [obj]
  (let [obj (assoc obj        
			         :uid (gensym)
			         :real true
			         :position (or (:position obj) (vec3 0 0 0))
			         :velocity (or (:velocity obj) (vec3 0 0 0))
			         :acceleration (or (:acceleration obj) (vec3 0 0 0))
               :density (or (:density obj) 1)
			         :shape (or (:shape obj) (create-box)))
        pos (:position obj)
        mass (obj-to-mass obj)
        body (doto (OdeHelper/createBody (get-world))               (.setPosition (.x pos) (.y pos) (.z pos))               (.setMass mass))        geom (doto (obj-to-geom obj)
               (.setBody body)
               (.setOffsetWorldPosition (.x pos) (.y pos) (.z pos)))]    
    obj))

(defn collided?
  "Have two objects collided?"
  [obj1 obj2]
  (contains? @*collisions* [(:uid obj1) (:uid obj2)]))

(defn inside-boundary?
  "Returns true if an object is out of the boundary of the simulation."
  [obj]
  (cantor.range/inside? simulation-boundary (:position obj)))

(defn sort-by-proximity
  "Return a list of objects sorted by proximity."
  [position objects]
  (sort-by #(length (sub position (:position %)))
           objects))
  
(defn move
  "Move an object to the specified position."
  [obj new-position]
  (assoc obj
    :position new-position))

(defn update-object-kinematics
  "Update the kinematics of an object by applying acceleration and velocity for an infinitesimal amount of time."
  [obj dt]
  (let [obj (assoc obj
                   :velocity (add (:velocity obj)
                                  (mul (:acceleration obj) dt)))]
    (move obj (add (:position obj)
                   (mul (:velocity obj) dt)))))

(defn make-floor
  "Make a floor object."
  [w h]
  (move (make-real {:color [0 0 1]
                    :type :floor
                    :density 8050
                    :shape (create-box w 0.1 h)})
        (vec3 0 -3 0)))

(def contact-joint-array (into-array java.lang.Class [org.ode4j.ode.DContactJoint]))(def nearCallback  (proxy [org.ode4j.ode.DGeom$DNearCallback] []    (call [#^java.lang.Object data #^DGeom o1 #^DGeom o2]      (let [b1 (.getBody o1)            b2 (.getBody o2)]        (if (and b1 b2 (OdeHelper/areConnectedExcluding b1 b2 contact-joint-array))          (do #_(println "Position b1:" (.getPosition b1) "Position b2:" (.getPosition b2))               nil)                        (let [N 8                contacts (new DContactBuffer N)                n (OdeHelper/collide o1 o2 N (.getGeomBuffer contacts))]            (when (> n 0)              (dotimes [i n]                (let [contact (.get contacts i)]                  (set! (.. contact surface mode)                        (bit-or (. OdeConstants dContactSlip1) (. OdeConstants dContactSlip2) #_(. OdeConstants dContactSoftERP)                                #_(. OdeConstants dContactSoftCFM) #_(. OdeConstants dContactApprox1)))                  (set! (.. contact surface mu) 100)                  (set! (.. contact surface slip1) 0.0)                  (set! (.. contact surface slip2) 0.0)                  #_(set! (.. contact surface soft_erp) 0.8)                  #_(set! (.. contact surface soft_cfm) 0.01)                  (doto (OdeHelper/createContactJoint (.getWorld b1) (:contact-group *physics*) contact)                    (.attach (.getBody o1) (.getBody o2))))))))))))

(defn handle-collisions
  "Handle the collisions of a collection of real objects.
Things is updated and returned as a vector."  [things collision-handlers]  (loop [things (vec things)         pairs (for [uid-pair @*collisions*]                                                                                                                         
                    [(some #(when (= (:uid (nth things %)) (first uid-pair)) %) things)
                     (some #(when (= (:uid (nth things %)) (second uid-pair)) %) things)])]    (if (empty? pairs)      things      (recur (let [pair (first pairs)                   thing1 (nth things (first pair))                   thing2 (nth things (second pair))                   collision-handler (get collision-handlers [(:type thing1) (:type thing2)])]               (if (collided? thing1 thing2)                 (cond (apply = pair) 
                       things                       (not (nil? collision-handler))                       (let [[thing1 thing2] (collision-handler thing1 thing2)]                         (assoc things                           (first pair) thing1                           (second pair) thing2))                       :else things)
                 things))
              (rest pairs)))))

(defn init-world  "Return a map of ODE physics for 1 world."  []  (let [world (doto (OdeHelper/createWorld)        
                      (.setGravity 0 -9.81 0))        space (OdeHelper/createHashSpace)        contact-group (OdeHelper/createJointGroup)]
    (reset! *physics* {:world world                             :space space                       :contact-group contact-group
                       :time 0});      (let [[floor floor-joint] (make-floor 1000 1000)
      (let [floor (make-floor 1000 1000)
            environment {:objects [floor]
                         :joints nil}]
        (reset! *physics* (assoc @*physics*
                                 :environment environment)))))
(defn reset-world  "Reset the *physics* global."  []  (loop []    (when (pos? (.getNumGeoms (:space *physics*)))      (.remove (:space *physics*) (.getGeom (:space *physics*) 0))      (recur)))  (let [[floor floor-joint] (make-floor)
        environment {:objects [floor]
                     :joints [floor-joint]}]    (reset! *physics* (assoc *physics*
                             :environment environment
                             :time 0)))) 

(defn increment-physics-time
  "Increment the physics time by dt"
  [dt]
  (reset! *physics* (assoc @*physics* 
                           :time (+ (:time @*physics*) dt))))

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

(defn update-world
  "Update the world."
  [[dt time] state]
  (when (and  state
              (not (:terminated? state)))
    (when (:contact-group @*physics*))
    (.empty (:contact-group @*physics*))
    (OdeHelper/spaceCollide (:space @*physics*) nil nearCallback)
    (.quickStep (:world @*physics*) (:dt state))
    #_(.empty (:contact-group *physics*))    
    (increment-physics-time (:dt state))
    (assoc state
           :simulation-time (+ (:simulation-time state) (:dt state))
           :objects (handle-collisions (update-objects (:objects state) (:dt state))
                                                        @collision-handlers))))


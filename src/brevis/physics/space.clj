;; This is for simulations that require space (such as physical or pseudo-physical worlds)
(ns brevis.physics.space
  (:gen-class)
  (:import (org.ode4j.ode OdeHelper DSapSpace OdeConstants DContactBuffer DGeom DFixedJoint DContactJoint))  (:import (org.ode4j.math DVector3))  (:import java.lang.Math)  
  (:use [cantor]
        [brevis.shape core box])
  (:require [cantor.range]))

;; ## Globals

;; Hash map keyed on pairs of types with values of the respective collision function.
;; 
;; Keys are of the form [:ball :floor]
;;
;; Collision functions take [collider collidee] and return [collider collidee].
;;
;; Both can be modified; however, two independent collisions are actually computed [a b] and [b a]."
(def #^:dynamic *collision-handlers*
  (atom {}))

;; Hash map keyed on type with values of the respective update function.
;;
;; An update function should take 3 arguments:
;;
;; [object dt neighbors] and return an updated version of object                                                                                                 
;; given that dt amount of time has passed.
(def #^:dynamic *update-handlers*
  (atom {}))

(def simulation-boundary
  (box3 (vec3 100 100 100)
        (vec3 -100 -100 -100)))

(def #^:dynamic *physics* (atom nil))
(def #^:dynamic *collisions* (atom #{}))

;; ## Utilities

(defn get-world
  "Return the current world"
  []
  (:world @*physics*))

(defn add-update-handler
  "Associate an update function with a type."
  [type handler-fn]
  (swap! *update-handlers* assoc type handler-fn))

(defn add-collision-handler
  "Store the collision handler for typea colliding with typeb."
  [typea typeb handler-fn]
  (swap! *collision-handlers* assoc
         [typea typeb] handler-fn))
            
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
  (.setLinearVel (:body obj) (vec3-to-odevec v)))

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
    (doto (OdeHelper/createMass)
      (.setBox (:density obj) (.x dim) (.y dim) (.z dim)))))

(defn obj-to-geom
  "Create an ODE geometry for a obj"
  [obj]
  (let [dim (:dim (:shape obj))]
    (OdeHelper/createBox (:space @*physics*) (.x dim) (.y dim) (.z dim))))

;; ## Real/Physical/Spatial

(defn make-real
  "Add Real attributes to an object map."
  [obj]
  (let [uid (gensym)
        obj (assoc obj        
			         :uid uid
			         :real true
			         :acceleration (or (:acceleration obj) (vec3 0 0 0))
               :density (or (:density obj) 1)
			         :shape (or (:shape obj) (create-box)))
        pos (or (:position obj) (vec3 0 0 0))
        mass (obj-to-mass obj)
        body (doto (OdeHelper/createBody (get-world))
               (.setMass mass)
               (.setData {:uid uid :type (:type obj)})                (.setPosition (.x pos) (.y pos) (.z pos)))                       geom (doto (obj-to-geom obj)
               (.setBody body)
               (.setOffsetWorldPosition (.x pos) (.y pos) (.z pos))
               #_(.enable))]    
    (assoc obj
           :mass mass
           :body body
           :geom geom)))

(defn collided?
  "Have two objects collided?"
  [obj1 obj2]
  (contains? @*collisions* [(:uid obj1) (:uid obj2)]))

(defn inside-boundary?
  "Returns true if an object is out of the boundary of the simulation."
  [obj]
  (cantor.range/inside? simulation-boundary (get-position obj)))

(defn sort-by-proximity
  "Return a list of objects sorted by proximity."
  [position objects]
  (sort-by #(length (sub position (get-position %)))
           objects))
  
(defn move
  "Move an object to the specified position."
  [obj v]
  (when-not (:body obj)
    (println obj))
  (.setPosition (:body obj)
    (.x v) (.y v) (.z v))
  obj)

(defn update-object-kinematics
  "Update the kinematics of an object by applying acceleration and velocity for an infinitesimal amount of time."
  [obj dt]
  (let [newvel (add (get-velocity obj)
                    (mul (:acceleration obj) dt))
        m 0.05
        f (mul (:acceleration obj)
               m)]; f = ma    
    (.addForce (:body obj) (.x f) (.y f) (.z f))
    #_(.setLinearVel (:body obj) (.x newvel) (.y newvel) (.z newvel)); avoids conversion that set-velocity would do
    obj))

(defn make-floor
  "Make a floor object."
  [w h]
  (move (make-real {:color [0 0 1]
                    :type :floor
                    :density 8050
                    :shape (create-box w 0.1 h)})
        (vec3 0 -3 0)))

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

(defn handle-collisions
  "Handle the collisions of a collection of real objects.
Things is updated and returned as a vector."  [things collision-handlers]  (loop [things (vec things)         pairs (for [uid-pair @*collisions*]                                                                                                                         
                    [(some #(when (= (:uid (nth things %)) (:uid (first uid-pair))) %) (range (count things)))
                     (some #(when (= (:uid (nth things %)) (:uid (second uid-pair))) %) (range (count things)))
                     uid-pair])]
    (when (some nil? (first pairs)) 
      (println (first pairs)) 
      (println @*collisions*) 
      (println (doall (map :uid things)))
      (println (doall (map :type things))))
        (if (empty? pairs)      things      (recur (let [pair (first pairs)                   thing1 (nth things (first pair))                   thing2 (nth things (second pair))                   collision-handler (get collision-handlers [(:type thing1) (:type thing2)])]
               #_(println pair (:type thing1) (:type thing2)) 
               #_(println "Collision-handler" collision-handler [(:type thing1) (:type thing2)] (nil? collision-handler))                (cond (apply = pair); self-collision, somehow 
                     things                     (not (nil? collision-handler))                     (let [[thing1 thing2] (collision-handler thing1 thing2)]
                       #_
                         (println "Colliding" pair (:type thing1) (:type thing2))                                                                                                                                     (assoc things                         (first pair) thing1                         (second pair) thing2))                     :else things))    
             (rest pairs)))))

(defn init-world  "Return a map of ODE physics for 1 world."  []  (let [world (doto (OdeHelper/createWorld)     
                      (.setGravity 0 0 0)                                                                                   
                      #_(.setGravity 0 -9.81 0))        space (OdeHelper/createHashSpace)        contact-group (OdeHelper/createJointGroup)]
    (reset! *physics* {:world world                             :space space                       :contact-group contact-group
                       :time 0});      (let [[floor floor-joint] (make-floor 1000 1000)
    (println "Collision handlers:" (keys @*collision-handlers*))
    (println "Update handlers:" (keys @*update-handlers*))
    (let [floor (make-floor 1000 1000)
            environment {:objects [floor]
                         :joints nil}]
        (reset! *physics* (assoc @*physics*
                                 :environment environment))        
        (:objects environment))))
(defn reset-world  "Reset the *physics* global."  []  (loop []    (when (pos? (.getNumGeoms (:space @*physics*)))      (.remove (:space *physics*) (.getGeom (:space @*physics*) 0))      (recur)))  (let [[floor floor-joint] (make-floor)
        environment {:objects [floor]
                     :joints [floor-joint]}]    (reset! *physics* (assoc @*physics*
                             :environment environment
                             :time 0)))) 

(defn increment-physics-time
  "Increment the physics time by dt"
  [dt]
  (reset! *physics* (assoc @*physics* 
                           :time (+ (:time @*physics*) dt))))

(defn update-objects
  "Update all objects in the simulation. Objects whose update returns nil                                                                                                
are removed from the simulation."
  [objects dt]
  (let [updated-objects (doall (for [obj objects]
;                                 ((get @update-handlers (:type obj)) obj dt (remove #{obj} objects))))                                                                  
                                   (let [f (get @*update-handlers* (:type obj))]
                                     ;(println (get @update-handlers (:type obj)) obj dt (remove #{obj} objects))                                                        
                                     (if f
                                       (f obj dt (remove #{obj} objects))
                                       obj))))
	      singles (filter #(not (seq? %)) updated-objects);; These objects didn't produce children                                                                         
        multiples (apply concat (filter seq? updated-objects))];; These are parents and children                                                                         
    (into [] (keep identity (concat singles multiples)))))

(defn update-world
  "Update the world."
  [[dt time] state]
  (when (and  state
              (not (:terminated? state)))
    (when (:contact-group @*physics*)
      (.empty (:contact-group @*physics*)))
    #_(println "Number of obj in space:" (.getNumGeoms (:space @*physics*)))
    (reset! *collisions* #{})
    (OdeHelper/spaceCollide (:space @*physics*) nil nearCallback)
    (.quickStep (:world @*physics*) (:dt state))
    #_(println "Collisions" (doall (for [uid-pair @*collisions*]                                                                                                                         
                                   [(some #(when (= (:uid (nth (:objects state) %)) (:uid (first uid-pair))) %) (range (count (:objects state))))
                                    (some #(when (= (:uid (nth (:objects state) %)) (:uid (second uid-pair))) %) (range (count (:objects state))))])))        
    #_(.empty (:contact-group *physics*))    
    (increment-physics-time (:dt state))    
    (assoc state
           :simulation-time (+ (:simulation-time state) (:dt state))
           :objects (handle-collisions (update-objects (:objects state) (:dt state))
                                                        @*collision-handlers*))))


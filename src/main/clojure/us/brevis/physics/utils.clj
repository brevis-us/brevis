(ns us.brevis.physics.utils
  (:import (org.ode4j.ode OdeHelper))
  (:import (org.ode4j.math DVector3))
  (:import (us.brevis Engine BrPhysics BrObject)
           (org.joml Vector3f Vector4f))
  (:require [us.brevis.vector :as v]
            [brevis-utils.math.core :as math]
            [us.brevis.physics.core :as physics]
            [us.brevis.utils :as utils]
            [us.brevis.shape.box :as box]
            [brevis-utils.parameters :as parameters]))

(defn get-world
  "Return the current world"
  []
  (.getWorld ^Engine @physics/*java-engine*))

(defn get-space
  "Return the current physical space being simulated"
  []
  (.getSpace ^Engine @physics/*java-engine*))

(defn get-contact-group
  "Return the current joints being used by entities."
  []
  (.getJoints ^Engine @physics/*java-engine*))

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
  (.jointHinge (.getPhysics @physics/*java-engine*)
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
  (.enableUpdateKinematics @physics/*java-engine* (str (name type))))
            
(defn odevec-to-vec3
  "Convert an ODE vector into a Cantor vector."
  [ov]
  (v/vec3 (.get0 ov) (.get1 ov) (.get2 ov)))

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
      (= (:type (:shape obj)) :cone) (OdeHelper/createSphere (:space @physics/*physics*) (.x dim));; the cake is a lie
      (= (:type (:shape obj)) :box) (OdeHelper/createBox (:space @physics/*physics*) (.x dim) (.y dim) (.z dim))
      (= (:type (:shape obj)) :sphere) (OdeHelper/createSphere (:space @physics/*physics*) (.x dim)))))
    
(defn obj-to-nbr-geom
  [obj]
  (let [dim (:dim (:shape obj))]
    (obj-to-geom (assoc-in obj [:shape :dim] (+ dim (* 2 (v/vec3 @physics/*neighborhood-radius* @physics/*neighborhood-radius* @physics/*neighborhood-radius*)))))))

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
         obj-uid (utils/get-uid obj)]
     (when nbrs
       (map #(utils/get-object %)
            (filter (partial not= obj-uid) nbrs)))))

(defn get-closest-neighbor
   "Return the objects of a neighborhood."
   [^BrObject obj]
   (let [nbr-uid (.getClosestNeighbor obj)]
     (when-not (or (nil? nbr-uid) (zero? nbr-uid))
       (utils/get-object nbr-uid))))

(defn set-neighborhood-radius
  "Set the neighborhood radius."
  [new-radius]
  (.setNeighborhoodRadius ^Engine @physics/*java-engine* (double new-radius)))

(defn get-neighborhood-radius
  "Get the neighborhood radius."
  []
  (.getNeighborhoodRadius ^Engine @physics/*java-engine*))


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
   (let [dists (map #(v/length-vec3 (v/sub-vec3 (get-position %) pos)) objs)]         
     (when-not (empty? objs)
       (nth objs
            (first (apply min-key second (map-indexed vector dists)))))))
    

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Real/Physical/Spatial

(defn make-real
  "Add Real attributes to an object map."
  [obj]
  (let [uid (long (hash (gensym)))                          ;; might not be safe
        uid (if (zero? uid) (inc uid) uid)                  ; 0 is used as a NIL UID
        obj (assoc obj
              :uid uid
              :real true
              :acceleration (or (:acceleration obj) (v/vec3 0 0 0))
              :density (or (:density obj) 1)
              :shape (or (:shape obj) (box/create-box)))
        pos (or (:position obj) (v/vec3 0 0 0))
        brobj (BrObject.)]
    (when (:color obj)
      (.setColor brobj (:color obj)))
    (.setUID brobj uid)
    (.setType brobj (str (name (:type obj))))
    (.setShape brobj (:shape obj))
    (.makeReal brobj @physics/*java-engine*)
    brobj))

(defn recreate-physics-geom
  "Recreate the physics geometry of this object."
  [obj]
  (.recreatePhysicsGeom obj @physics/*java-engine*)
  obj)

(defn orient-object
  "Orient an object by changing its rotation such that its vertex points towards a target vector.
  Takes the object's up vector and a target direction vector"
  [^BrObject obj ^Vector3f up-vec ^Vector3f target-vec]
  (if (or (zero? (v/length up-vec))
          (zero? (v/length target-vec)))
    obj
    (do (.orient obj up-vec target-vec)
        obj)))

(defn set-rotation
  "Set the rotation of an object directly using a quaternion."
  [^BrObject obj ^Vector4f v]
  (.setRotation obj v)
  obj)

(defn sort-by-proximity
  "Return a list of objects sorted by proximity."
  [position objects]
  (sort-by #(v/length (v/sub position (get-position %)))
           objects))


(defn compute-neighborhood
  "Return a list of neighbor UIDs within the neighborhood radius (deprecated)"
  ;; This should probably use extrema of object shape instead of center point
  [obj objects]
  (let [nbr-radius @physics/*neighborhood-radius*; TODO should be a parameter
        my-position (get-position obj)
        ;upper-comp #(< %1 (+ %2 nbr-radius))
        ;lower-comp #(> %1 (- %2 nbr-radius))
        ul-comp #(and (< %1 (+ %3 nbr-radius))
                      (> %2 (- %4 nbr-radius)))
        nbrs (atom (into #{} (keys @physics/*objects*)))
        to-remove (atom #{})]
    #_(println "initial" (count @nbrs))
    ;; Find all UIDs that cannot be neighbors along this dimension
    (doseq [their-uid @nbrs]
      (let [them (get @physics/*objects* their-uid)]
        (when-not (< (obj-distance obj them) nbr-radius)
          (swap! to-remove conj their-uid))))
    ;; Remove from possible neighbors
    #_(println "Removing" (count @to-remove) "from" (count @nbrs))
    (doseq [exo @to-remove]
      (swap! nbrs disj exo))
    #_(println "final" (count (filter identity (seq @nbrs))))
    #_(when-not (zero? (count (filter identity (seq @nbrs))))
        (println "final set" (seq @nbrs)))
    (doall (map #(get @physics/*objects* %) (filter identity (seq @nbrs))))))

(defn insert-into [s x]
  (let [[low high] (split-with #(< % x) s)]
    (concat low [x] high)))

(defn chunked-update-neighbors
  "Update all neighborhoods (deprecated)"
  [objs]
  (let [num-chunks 16
        all-uids (doall (keys @physics/*objects*))
        all-pairs (loop [pairs []
                         rem (range (count all-uids))]
                    (if (empty? rem)
                      pairs
                      (recur (concat pairs
                                     (map #(vector (first rem) %) (rest rem)))
                             (rest rem))))
        nbr-pairs (doall (filter identity (pmap (fn [[me other]]
                                                  (let [d (obj-distance (utils/get-object (nth all-uids me))
                                                                        (utils/get-object (nth all-uids other)))]
                                                    (when (< d @physics/*neighborhood-radius*)
                                                      [me other])))
                                                all-pairs)))
        nbrhoods (loop [nbrhoods (zipmap (range (count all-uids)) (repeat #{}))
                        rem nbr-pairs]
                   (if (empty? rem)
                     nbrhoods
                     (let [[me other] (first rem)]
                       (recur (assoc nbrhoods
                                me (conj (nbrhoods me) (nth all-uids other))
                                other (conj (nbrhoods other) (nth all-uids me)))
                              (rest rem)))))]
    #_(println nbr-pairs nbrhoods)
    (doall (for [k (range (count objs))]
             (assoc (nth objs k)
               :neighbors (doall (filter identity (nbrhoods k))))))))

(defn update-neighbors
  "Update all neighborhoods (deprecated)"
  [objs]
  (if @physics/*brevis-parallel*
    (chunked-update-neighbors objs)
    (let [all-uids (doall (keys @physics/*objects*))
          ;; Initialize everyone in everyone else's neighborhood
          nbrhoods (zipmap (range (count all-uids))
                           (repeatedly (count all-uids)
                                       #(atom (into [] all-uids))))
          positions (doall (map #(.getPosition (:body (get @physics/*objects* %))) all-uids))]
      (doseq [me (range (count all-uids))]
        (doseq [other (range me (count all-uids))]
          (let [d (obj-distance (utils/get-object (nth all-uids me))
                                (utils/get-object (nth all-uids other)))]
            (when (> d @physics/*neighborhood-radius*)
              (do (reset! (nbrhoods me)
                          (assoc @(nbrhoods me)
                            other nil))
                  (reset! (nbrhoods other)
                          (assoc @(nbrhoods other)
                            me nil)))))))
      (doall (for [k (range (count objs))]
               (assoc (nth objs k)
                 :neighbors (doall (filter identity @(nbrhoods k)))))))))

(defn set-kdtree-rebalance-steps
  "Set the number of steps after which to rebalance the KDtree."
  [num-steps]
  (.setRebalanceKDTreeSteps @physics/*java-engine* num-steps))

(defn get-kdtree-rebalance-steps
  "Get the number of steps after which to rebalance the KDtree."
  []
  (.getRebalanceKDTreeSteps @physics/*java-engine*))

(defn move
  "Move an object to the specified position."
  [obj v]
  ; Update KDnode for obj
  (.setPosition obj v)
  obj)

(defn update-object-kinematics
  "Update the kinematics of an object by applying acceleration and velocity for an infinitesimal amount of time."
  [^BrObject obj dt]
  (let [newvel (v/add (get-velocity obj)
                    (v/mul (get-acceleration obj) dt))
        m (get-double-mass obj)                             ;0.05
        f (v/mul (get-acceleration obj)
               m)]                                          ; f = ma
    #_(println "update-object-kinematics" (get-uid obj) (get-position obj))
    (.addForce ^org.ode4j.ode.DBody (get-body obj) (.x f) (.y f) (.z f))
    obj))

#_(defn make-floor
    "Make a floor object."
    [w h]
    (set-texture (move (make-real {:color     (vec4 0.8 0.8 0.8 1)
                                   :shininess 80
                                   :type      :floor
                                   :density   8050
                                   :hasShadow false
                                   ;                    :texture *checkers*
                                   :shape     (create-box w 0.1 h)})
                       (vec3 0 -3 0))
                 "img/checker_large.png"
                 #_(clojure.java.io/resource "img/checker_large.png")))

(defn init-world
  "Return a map of ODE physics for 1 world. (Now does some brevis in it too)"
  []
  (when @physics/*java-engine*
    (.clearSimulation ^Engine @physics/*java-engine*))
  (let [world (doto (OdeHelper/createWorld)
                (.setGravity 0 0 0)
                #_(.setGravity 0 -9.81 0))
        space (OdeHelper/createHashSpace)
        contact-group (OdeHelper/createJointGroup)]
    (reset! physics/*physics* {:world         world
                               :space         space
                               :contact-group contact-group
                               :time          0})
    ;      (let [[floor floor-joint] (make-floor 1000 1000)
    #_(println "Collision handlers:" (keys @*collision-handlers*))
    #_(println "Update handlers:" (keys @*update-handlers*))
    #_(let [floor (make-floor 500 500)
            environment {:objects [floor]
                         :joints  nil}]
        (reset! *physics* (assoc @*physics*
                            :environment environment))
        (add-object floor)
        (:objects environment))))

#_(defn reset-world
    "Reset the *physics* global."
    []
    (loop []
      (when (pos? (.getNumGeoms (:space @*physics*)))
        (.remove (:space *physics*) (.getGeom (:space @*physics*) 0))
        (recur)))
    (let [[floor floor-joint] (make-floor)
          environment {:objects [floor]
                       :joints  [floor-joint]}]
      (reset! *physics* (assoc @*physics*
                          :environment environment
                          :time 0))))

(defn reset-world
  "Reset the *physics* global."
  []
  (loop []
    (when (pos? (.getNumGeoms (:space @physics/*physics*)))
      (.remove (:space physics/*physics*) (.getGeom (:space @physics/*physics*) 0))
      (recur)))
  (let [environment {:objects []
                     :joints  []}]
    (reset! physics/*physics* (assoc @physics/*physics*
                                :environment environment
                                :time 0))))

(defn increment-physics-time
  "Increment the physics time by dt"
  [dt]
  (reset! physics/*physics* (assoc @physics/*physics*
                              :time (+ (:time @physics/*physics*) dt))))

#_(defn update-objects
    "Update all objects in the simulation. Objects whose update returns nil
 are removed from the simulation. (deprecated)"
    [objects dt]
    (let [updated-objects
          (pmapall (fn [obj]
                     (let [f (get @*update-handlers* (:type obj))]
                       (if f
                         (f obj dt (remove #{obj} objects))
                         obj))) objects)
          singles (filter #(not (seq? %)) updated-objects)  ;; These objects didn't produce children
          multiples (apply concat (filter seq? updated-objects))] ;; These are parents and children
      (into [] (keep identity (concat singles multiples)))))

(defn java-update-world
  "Update the world (Java engine)."
  [[dt t] state]
  (when (and state
             (not (:terminated? state)))
    (when-not @physics/*java-engine*
      (reset! physics/*java-engine*
              (Engine.)))
    (when (and @physics/*java-engine*
               (not (parameters/get-param :paused)))
      (.updateWorld ^Engine @physics/*java-engine*)
      #_(.updateWorld @*java-engine* (double dt)))
    state))

(defn java-init-world
  "Initialize the world (Java engine)."
  []
  (when-not @physics/*java-engine*
    (reset! physics/*java-engine*
            (Engine.)))                                     ;; resetting loses handler class  instances
  #_(reset! *java-engine*
            (Engine.))
  (.initWorld ^Engine @physics/*java-engine*))

#_(defn update-world
    "Update the world. (deprecated)"
    [[dt t] state]
    (when (and state
               (not (:terminated? state)))
      (when (:contact-group @*physics*)
        (.empty (:contact-group @*physics*)))
      (reset! *collisions* #{})

      (OdeHelper/spaceCollide (:space @*physics*) nil nearCallback)
      (.quickStep (:world @*physics*) (get-dt))
      (increment-physics-time (get-dt))

      ;; Add/delete objects before updating them
      (reset! *objects* (let [in-objs (vals (merge @*objects* @*added-objects*))]
                          (reset! *added-objects* {})
                          (let [objs (zipmap (map :uid in-objs) in-objs)]
                            (apply (partial dissoc objs) @*deleted-objects*))))

      ;; Update objects based upon their update method
      (reset! *objects* (let [in-objs (vals @*objects*)]
                          (reset! *added-objects* {})
                          (let [new-objs (update-objects in-objs (get-dt))
                                objs (zipmap (map :uid new-objs) new-objs)]
                            (apply (partial dissoc objs) @*deleted-objects*))))
      (reset! *deleted-objects* #{})

      ;; Update objects for collisions
      (when @collisions-enabled
        (reset! *objects* (let [in-objs (vals (merge @*objects* @*added-objects*))]
                            (reset! *added-objects* {})
                            (let [new-objs (handle-collisions in-objs @*collision-handlers*)
                                  objs (zipmap (map :uid new-objs) new-objs)]
                              (apply (partial dissoc objs) @*deleted-objects*)))))
      (reset! *deleted-objects* #{})

      ;; Finally update neighborhoods
      (when @neighborhoods-enabled
        (reset! *objects* (let [in-objs (vals (merge @*objects* @*added-objects*))]
                            (reset! *added-objects* {})
                            (let [new-objs (update-neighbors in-objs)]
                              (zipmap (map :uid new-objs) new-objs)))))

      (assoc state
        :simulation-time (+ (:simulation-time state) (get-dt)))))

;; ## Neighbors

(defn distance-obj-to-line
  "Distance of an object to a line."
  [obj start-point direction]
  (let [p (get-position obj)
        a (v/sub p start-point)
        aXv (v/cross a direction)
        la (v/length a)
        sin-theta (/ (v/length aXv) (* la (v/length direction)))]
    (* la sin-theta)))

(defn get-neighbors-along-line
  "Return all neighbors along a line."
  [point dir radius]
  #_(println point dir radius @*java-engine*)
  (let [objs (seq (.objectsAlongLine ^Engine @physics/*java-engine* (double-array point) (double-array dir) radius))
        adjacent objs
        #_(filter #(< (distance-obj-to-line % (apply vec3 point) (apply vec3 dir))
                      radius) objs)]
    #_(println point dir radius (count adjacent))
    adjacent))


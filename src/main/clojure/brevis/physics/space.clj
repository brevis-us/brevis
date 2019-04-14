(ns brevis.physics.space
  (:gen-class)
  (:import (org.ode4j.ode OdeHelper))
  (:import (brevis Engine BrObject)
           (org.joml Vector3f Vector4f))
  (:use [brevis vector utils globals]
        [brevis.shape core box]
        [brevis.physics core collision utils])
  (:require [clojure.java.io]))

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
              :hasShadow (or (:hasShadow obj) true)
              :acceleration (or (:acceleration obj) (vec3 0 0 0))
              :density (or (:density obj) 1)
              :shape (or (:shape obj) (create-box)))
        pos (or (:position obj) (vec3 0 0 0))
        brobj (BrObject.)]
    (when (:color obj)
      (.setColor brobj (:color obj)))
    (.setShadow brobj (:hasShadow obj))
    (.setUID brobj uid)
    (.setType brobj (str (name (:type obj))))
    (.setShape brobj (:shape obj))
    (.makeReal brobj @*java-engine*)
    brobj))

(defn recreate-physics-geom
  "Recreate the physics geometry of this object."
  [obj]
  (.recreatePhysicsGeom obj @*java-engine*)
  obj)

(defn orient-object
  "Orient an object by changing its rotation such that its vertex points towards a target vector."
  [^BrObject obj ^Vector3f obj-vec ^Vector3f target-vec]
  (if (or (zero? (length obj-vec))
          (zero? (length target-vec)))
    obj
    (do (.orient obj obj-vec target-vec)
        obj)))

(defn set-rotation
  "Set the rotation of an object directly using a quaternion."
  [^BrObject obj ^Vector4f v]
  (.setRotation obj v)
  obj)

(defn sort-by-proximity
  "Return a list of objects sorted by proximity."
  [position objects]
  (sort-by #(length (sub position (get-position %)))
           objects))


(defn compute-neighborhood
  "Return a list of neighbor UIDs within the neighborhood radius (deprecated)"
  ;; This should probably use extrema of object shape instead of center point
  [obj objects]
  (let [nbr-radius @*neighborhood-radius*
        my-position (get-position obj)
        ;upper-comp #(< %1 (+ %2 nbr-radius))
        ;lower-comp #(> %1 (- %2 nbr-radius))
        ul-comp #(and (< %1 (+ %3 nbr-radius))
                      (> %2 (- %4 nbr-radius)))
        nbrs (atom (into #{} (keys @*objects*)))
        to-remove (atom #{})]
    #_(println "initial" (count @nbrs))
    ;; Find all UIDs that cannot be neighbors along this dimension
    (doseq [their-uid @nbrs]
      (let [them (get @*objects* their-uid)]
        (when-not (< (obj-distance obj them) nbr-radius)
          (swap! to-remove conj their-uid))))
    ;; Remove from possible neighbors
    #_(println "Removing" (count @to-remove) "from" (count @nbrs))
    (doseq [exo @to-remove]
      (swap! nbrs disj exo))
    #_(println "final" (count (filter identity (seq @nbrs))))
    #_(when-not (zero? (count (filter identity (seq @nbrs))))
        (println "final set" (seq @nbrs)))
    (doall (map #(get @*objects* %) (filter identity (seq @nbrs))))))

(defn insert-into [s x]
  (let [[low high] (split-with #(< % x) s)]
    (concat low [x] high)))

(defn chunked-update-neighbors
  "Update all neighborhoods (deprecated)"
  [objs]
  (let [num-chunks 16
        all-uids (doall (keys @*objects*))
        all-pairs (loop [pairs []
                         rem (range (count all-uids))]
                    (if (empty? rem)
                      pairs
                      (recur (concat pairs
                                     (map #(vector (first rem) %) (rest rem)))
                             (rest rem))))
        nbr-pairs (doall (filter identity (pmap (fn [[me other]]
                                                  (let [d (obj-distance (get-object (nth all-uids me))
                                                                        (get-object (nth all-uids other)))]
                                                    (when (< d @*neighborhood-radius*)
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
  (if @*brevis-parallel*
    (chunked-update-neighbors objs)
    (let [all-uids (doall (keys @*objects*))
          ;; Initialize everyone in everyone else's neighborhood
          nbrhoods (zipmap (range (count all-uids))
                           (repeatedly (count all-uids)
                                       #(atom (into [] all-uids))))
          positions (doall (map #(.getPosition (:body (get @*objects* %))) all-uids))]
      (doseq [me (range (count all-uids))]
        (doseq [other (range me (count all-uids))]
          (let [d (obj-distance (get-object (nth all-uids me))
                                (get-object (nth all-uids other)))]
            (when (> d @*neighborhood-radius*)
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
  (.setRebalanceKDTreeSteps @*java-engine* num-steps))

(defn get-kdtree-rebalance-steps
  "Get the number of steps after which to rebalance the KDtree."
  []
  (.getRebalanceKDTreeSteps @*java-engine*))

(defn move
  "Move an object to the specified position."
  [obj v]
  ; Update KDnode for obj
  (.setPosition obj v)
  obj)

(defn update-object-kinematics
  "Update the kinematics of an object by applying acceleration and velocity for an infinitesimal amount of time."
  [^BrObject obj dt]
  (let [newvel (add (get-velocity obj)
                    (mul (get-acceleration obj) dt))
        m (get-double-mass obj)                             ;0.05
        f (mul (get-acceleration obj)
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
  (when @*java-engine*
    (.clearSimulation ^Engine @*java-engine*))
  (let [world (doto (OdeHelper/createWorld)
                (.setGravity 0 0 0)
                #_(.setGravity 0 -9.81 0))
        space (OdeHelper/createHashSpace)
        contact-group (OdeHelper/createJointGroup)]
    (reset! *physics* {:world         world
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
    (when (pos? (.getNumGeoms (:space @*physics*)))
      (.remove (:space *physics*) (.getGeom (:space @*physics*) 0))
      (recur)))
  (let [environment {:objects []
                     :joints  []}]
    (reset! *physics* (assoc @*physics*
                        :environment environment
                        :time 0))))

(defn increment-physics-time
  "Increment the physics time by dt"
  [dt]
  (reset! *physics* (assoc @*physics*
                      :time (+ (:time @*physics*) dt))))

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
    (when-not @*java-engine*
      (reset! *java-engine*
              (Engine.)))
    (when (and @*java-engine*
               (not (:paused @*gui-state*)))
      (.updateWorld ^Engine @*java-engine*)
      #_(.updateWorld @*java-engine* (double dt)))
    state))

(defn java-init-world
  "Initialize the world (Java engine)."
  []
  (when-not @*java-engine*
    (reset! *java-engine*
            (Engine.)))                                     ;; resetting loses handler class  instances
  #_(reset! *java-engine*
            (Engine.))
  (.initWorld ^Engine @*java-engine*))

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
        a (sub p start-point)
        aXv (cross a direction)
        la (length a)
        sin-theta (/ (length aXv) (* la (length direction)))]
    (* la sin-theta)))

(defn get-neighbors-along-line
  "Return all neighbors along a line."
  [point dir radius]
  #_(println point dir radius @*java-engine*)
  (let [objs (seq (.objectsAlongLine ^Engine @*java-engine* (double-array point) (double-array dir) radius))
        adjacent objs
        #_(filter #(< (distance-obj-to-line % (apply vec3 point) (apply vec3 dir))
                      radius) objs)]
    #_(println point dir radius (count adjacent))
    adjacent))

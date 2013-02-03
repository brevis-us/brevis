;; This is for simulations that require space (such as physical or pseudo-physical worlds)
(ns brevis.physics.space
  (:use [cantor]
        [brevis.shape core box])
  (:require [cantor.range]))

(def collision-handlers
  (atom {}))

(def simulation-boundary
  (box3 (vec3 100 100 100)
        (vec3 -100 -100 -100)))

(def max-collision-neighbors (atom 5))

(defn add-collision-handler
  "Store the collision handler for typea colliding with typeb.                                                                                                           
Collision functions take [collider collidee] and return [collider collidee]                                                                                              
Both can be modified; however, two independent collisions are actually computed [a b] and [b a]."
  [typea typeb handler-fn]
  (swap! collision-handlers assoc
         [typea typeb] handler-fn))
            
(defn make-real
  "Add Real attributes to an object map."
  [obj]
  (assoc obj
    :real true
    :position (vec3 0 0 0)
    :velocity (vec3 0 0 0)
    :acceleration (vec3 0 0 0)))

(defn make-collision-shape
  "Create the collision shape of an object."
  [obj]
  (assoc obj
    :collision-shape (cond (= (:type (:shape obj)) :box)
                           (create-collision-box (:position obj) (:shape obj)))))

(defn collided?
  "Have two objects collided?"
  [obj1 obj2]
  (cantor.range/overlap? (:collision-shape obj1) (:collision-shape obj2)))

;; Fully-connected pairwise collision
#_(defn handle-collisions
  "Handle the collisions of a collection of real objects.
Things is updated and returned as a vector."
  [things collision-handlers]
  (loop [things (vec things)
         pairs (for [a (range (count things)) b (range (count things))] [a b])]
    (if (empty? pairs)
      things
      (recur (let [pair (first pairs)
                   thing1 (nth things (first pair))
                   thing2 (nth things (second pair))
                   collision-handler (get collision-handlers [(:type thing1) (:type thing2)])]
               (if (collided? thing1 thing2)
                 (cond (apply = pair) things;; Don't collide with self
                       ;; Collision handling
                       (not (nil? collision-handler))
                       (let [[thing1 thing2] (collision-handler thing1 thing2)]
                         (assoc things
                           (first pair) thing1
                           (second pair) thing2))
                       :else things);; If there is no collision handler
                 things))
             (rest pairs)))))

; Only loop over individuals and let them compute their neighbors
; However, this needs to account for the bounding volume of each object
(defn potential-collision-pairs
  "Given a set of objects with positions return a list of potential collisions."
  [things]
  (apply concat
         (for [obj-idx (range (count things))]
           (let [obj (nth things obj-idx)
                 pos (:position obj)]
             (map #(vector obj-idx %)
                  (take @max-collision-neighbors
                        (sort-by #(length (sub pos (:position (nth things %))))
                                 (range (count things)))))))))

(defn handle-collisions
  "Handle the collisions of a collection of real objects.
Things is updated and returned as a vector."
  [things collision-handlers]
  (loop [things (vec things)
         pairs (potential-collision-pairs things)]
    (if (empty? pairs)
      things
      (recur (let [pair (first pairs)
                   thing1 (nth things (first pair))
                   thing2 (nth things (second pair))
                   collision-handler (get collision-handlers [(:type thing1) (:type thing2)])]
               (if (collided? thing1 thing2)
                 (cond (apply = pair) things;; Don't collide with self
                       ;; Collision handling
                       (not (nil? collision-handler))
                       (let [[thing1 thing2] (collision-handler thing1 thing2)]
                         (assoc things
                           (first pair) thing1
                           (second pair) thing2))
                       :else things);; If there is no collision handler
                 things))
             (rest pairs)))))  
  
#_(defn handle-collisions
  "Handle the collisions of a collection of real objects.
Things is updated and returned as a vector."
  [things collision-handlers]
  (loop [things (vec things)
         idx (shuffle (range (count things)))]
    (if (empty? idx)
      things
      (recur (let [ind-pos (:position (nth things (first idx)))
                   nbrs (take @max-collision-neighbors
                              (sort-by-proximity ind-pos things))
                   pair (first pairs)
                   thing1 (nth things (first pair))
                   thing2 (nth things (second pair))
                   collision-handler (get collision-handlers [(:type thing1) (:type thing2)])]
               (if (collided? thing1 thing2)
                 (cond (apply = pair) things;; Don't collide with self
                       ;; Collision handling
                       (not (nil? collision-handler))
                       (let [[thing1 thing2] (collision-handler thing1 thing2)]
                         (assoc things
                           (first pair) thing1
                           (second pair) thing2))
                       :else things);; If there is no collision handler
                 things))
             (rest pairs)))))

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
  (let [obj (assoc obj :velocity (add (:velocity obj) (mul (:acceleration obj) dt)))]
    (-> (assoc obj :position (add (:position obj) (mul (:velocity obj) dt)))
        (make-collision-shape))))

(defn make-floor
  "Make a floor object."
  []
  (-> {}
      (make-real)
      (make-box)
      (resize-shape (vec3 100 0.1 100))
      (move (vec3 0 -3 0))
      (assoc :color [0 0 1]
             :type :floor)
      (make-collision-shape)))

(ns brevis.example.coverage
  (:require [clojure.zip :as zip])
  (:use [brevis.graphics.basic-3D]
        [brevis.physics.space]
        [brevis.shape core box sphere]
        [brevis.core]
        [cantor]))  

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Globals

(def num-robots 7)
(def avoidance (atom 0.2))
(def clustering (atom 0.1))
(def centering (atom 0.0))
(def dirt-counter (atom 0))

(def max-velocity 10)
(def max-acceleration 2)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Dirt

(defn dirt?
  "Is a thing dirt?"
  [thing]
  (= (:type thing) :dirt))

(defn make-dirt
  "Make dirt at a specific location"
  [x y]
  (-> {}
    (make-real)
    (make-box)
    (assoc :type :dirt
           :color [0.7 0.3 0.2]
           :energy 0
           :position (vec3 x 0 y))
    (make-collision-shape)))

(defn update-dirt
  "Update a dirt object"
  [dirt dt nbrs]
  (let [nrg-dim (+ 0.25 (* 0.001 (:energy dirt)))]
    (assoc (resize-shape dirt (vec3 nrg-dim 1 nrg-dim))
           :energy (+ (:energy dirt) dt))))

(add-update-handler :dirt update-dirt)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Robots

(defn robot?
  "Is a thing a robot?"
  [thing]
  (= (:type thing) :robot))

(defn random-robot-position
  "Returns a random valid robot position."
  []
  (vec3 (- (rand 100) 50) 
        0
        (- (rand 100) 50)))

(defn random-robot-velocity
  "Returns a random reasonable velocity."
  []
  (vec3 (- (rand 2) 1) 0 (- (rand 2) 1)))

(defn make-robot
  "Make a new robot with the specified program. At the specified location."
  [position]
  (-> {}
      (make-real)
      (make-box)
;      (make-sphere)
      (resize-shape (vec3 5 1 5))
      (assoc :type :robot
             :color [1 0 0]
             :position position)
      (make-collision-shape)))
  
(defn random-robot
  "Make a new random robot."
  []
  (make-robot (random-robot-position)))

(defn bound-acceleration
  "Keeps the acceleration within a reasonable range."
  [v]
  (if (> (length v) max-acceleration)
    (mul (div v (length v)) max-acceleration)
    v))

(defn bound-velocity
  "Keeps the acceleration within a reasonable range."
  [v]
  (if (> (length v) max-velocity)
    (mul (div v (length v)) max-velocity)
    v))

(defn fly
  "Change the acceleration of a robot."
  [robot dt nbrs]
  (let [closest-robot (first nbrs)
        centroid (div (reduce add (map :position nbrs)) 
                      (count nbrs))
        d-closest-robot (sub (:position closest-robot) (:position robot))
        d-centroid (sub centroid (:position robot))
        d-center (sub (vec3 0 0 0) (:position robot))]
    (assoc robot
           :velocity (bound-velocity (:velocity robot))
           :acceleration (bound-acceleration
                           (add (:acceleration robot)
                                (mul d-center @centering)
                                (mul d-closest-robot @avoidance)
                                (mul d-centroid @clustering))))))  

(defn update-robot
  "Update a robot based upon its flocking behavior and the physical kinematics."
  [robot dt objects]  
  (let [objects (filter robot? objects)
        nbrs (sort-by-proximity (:position robot) objects)
        floor (some #(when (= (:type %) :floor) %) objects)]
    (doseq [el (:vertices (:shape robot))]
      (println el))
    (update-object-kinematics 
      (fly robot dt nbrs) dt)))

(add-update-handler :robot update-robot); This tells the simulator how to update these objects

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Collision handling
;;
;; Collision functions take [collider collidee] and return [collider collidee]
;;   Both can be modified; however, two independent collisions are actually computed [a b] and [b a].

(defn wall-bump
  "Collision between a robot and a wall. This is called on [robot wall]."
  [robot wall]
  [(assoc robot
          :color [(rand) (rand) (rand)]
          :acceleration (mul (:acceleration robot) -1)
          :velocity (mul (:velocity robot) -1))
   wall])

(defn bump
  "Collision between two robots. This is called on [robot1 robot2] and [robot2 robot1] independently
so we only modify robot1."
  [robot1 robot2]
  [(assoc robot1 
          :color [(rand) (rand) (rand)]
          :acceleration (mul (:acceleration robot1) -1)
          :velocity (mul (:velocity robot1) -1))
   robot2])

(defn land
  "Collision between a robot and the floor."
  [robot floor]
  [(assoc robot
     :position (vec3 (.x (:position robot)) 0 (.z (:position robot)))
     :acceleration (vec3 0 0 0)
     :velocity (vec3 0 0 0))
   floor])

(defn vacuum
  "SUCK THAT UP!"
  [robot dirt]
  (reset! dirt-counter (+ @dirt-counter (:energy dirt)))
  [(assoc robot
     :position (vec3 (.x (:position robot)) 0 (.z (:position robot))))
   (assoc dirt
     :energy 0)])

(add-collision-handler :robot :obstruction wall-bump)
(add-collision-handler :robot :robot bump)
(add-collision-handler :robot :floor land)
(add-collision-handler :robot :dirt vacuum)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; World updates

(defn make-obstruction
  "Make an obstruction at the (x,y) coordinate."
  ([x y]
    (make-obstruction x y 1 1))
  ([x y w h]
  (-> {}
      (make-real)
      (make-box)
      (resize-shape (vec3 w 1 h))
      (move (vec3 x 0 y))
      (assoc :color [0.5 0.5 0.5]
             :type :obstruction)
      (make-collision-shape))))

(defn make-square-walls
  "Make square walls around the floor."
  [width height]
  (let [low-x 0;(- (/ width 2))
        low-y 0 ;(- (/ height 2))
        high-x (/ width -2)
        high-y (/ height -2)
        ]
  (concat (list (make-obstruction low-x (- high-y) width 1)
                (make-obstruction (- high-x) low-y 1 height)
                (make-obstruction low-x high-y width 1)
                (make-obstruction high-x low-y 1 height))
          (map #(make-dirt (first %) (second %))
               (for [x (range (inc high-x) (dec (- high-x)) 10)
                     y (range (inc high-y) (dec (- high-y)) 10)]
                 [x y]
                 )))))

;  (map #(make-obstruction %1 %2) 
;       (concat (range width) (repeat height 0) (range width) (repeat height (dec width)))
;       (concat (repeat width 0) (range height) (repeat width (dec height)) (range height))))

(defn make-map
  "Construct map according to the map-type argument"
  [map-type]
  (let [w 100
        h 100
        floor (make-floor w h)]
    (conj (cond (= :square map-type) (make-square-walls w h))
          floor)))

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []
  (let [robots (repeatedly num-robots random-robot)
        map (make-map :square)]
    {:objects (concat robots map)
     :rotate-mode :none :translate-mode :none
     :dt 0.1
     :rot-x 0 :rot-y 0 :rot-z 0
     :shift-x 0 :shift-y -20 :shift-z -150
     }))

(start-gui initialize-simulation update-world)

(ns brevis.example.swarm
  (:require [clojure.zip :as zip])
  (:use [brevis.graphics.basic-3D]
        [brevis.physics.space]
        [brevis.shape.box]
        [brevis.core]
        [cantor]))  


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Globals

(def max-vec3 (vec3 1000 1000 1000))
(def min-vec3 (mul max-vec3 -1))

;; Populations
(def num-initial-birds 25)
(def minimum-num-birds num-initial-birds)
(def num-initial-foods 20)

;; Energies
(def child-bird-energy-cost 0.15)
(def child-birth-energy-loss 0.05)

(def bump-energy-cost 0.0001)
(def energy-from-eating-food 0.01)

(def bird-cost-of-living 0.0001)
(def food-growth-rate 0.0001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Food

(def lrand rand)

(defn food?
  "Is a thing a food?"
  [thing]
  (= (:type thing) :food))

(defn random-food-position
  []
  (vec3 (- (lrand 30) 15) 0 (- (lrand 30) 15)))

(defn random-food
  []
  (-> {}
      (make-real)
      (make-box)      
      (assoc :type :food
             :color [0 1 0]
             :energy 1
             :position (random-food-position))
      (make-collision-shape)))

(defn update-food 
  "Update a food object."
  [food dt objects]
  (let [food (update-object-kinematics food dt)
        food (assoc food
; Mobile food                    
;               :acceleration (mul (vec3 (- (rand 2) 1) 0 (- (rand 2) 1)) 0.01)
               :energy (+ (:energy food) food-growth-rate))]
    food))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Birds

(defn bird?
  "Is a thing a bird?"
  [thing]
  (= (:type thing) :bird))
  
(defn keep-vec3-reasonable
  "Bounds each coordinate by a maximum value."
  [v]
  (map* max (map* min v max-vec3) min-vec3))        

(defn random-bird-position
  "Returns a random valid bird position."
  []
  (vec3 (- (lrand 20) 10) -3 (- (lrand 20) 10)))

(defn random-bird-velocity
  "Returns a random reasonable velocity."
  []
  (vec3 (- (lrand 2) 1) (- (lrand 2) 1) (- (lrand 2) 1)))

(defn make-bird
  "Make a new bird with the specified program. At the specified location."
  [friendliness position]
  (-> {}
      (make-real)
      (make-box)      
      (assoc :type :bird
             :color [1 0 0]
             :energy child-bird-energy-cost
             :position position
             :friendliness friendliness)
      (make-collision-shape)))

(defn random-bird
  "Make a new random bird."
  []
  (let [friendliness (lrand)]
    (make-bird friendliness (random-bird-position))))

(defn bound-acceleration
  "Keeps the acceleration within a reasonable range."
  [v]
  (if (> (length v) 10)
    (div v 10)
    v))

(defn eval-bird
  "Evaluate a bird to decide what to do."
  [bird nbrs]
  (let [dir-to-nbr (sub (:position (first nbrs)) (:position bird))]
    (assoc bird
           :acceleration (add (:acceleration bird) 
                              (mul dir-to-nbr (:friendliness bird))))))

(defn fly
  "Change the acceleration of a bird."
  [bird dt proximity-list]
  (let [desires (eval-bird bird proximity-list)
        bird (assoc bird
               :acceleration (bound-acceleration (:acceleration desires))
               :friendliness (:friendliness desires))]
    bird))

(defn offset-position
  "Return a position offset from the specified position by a specified radius."
  [position radius]
  (add position
       (mul (normalize (vec3 (- (lrand 2) 1) (- (lrand 2) 1) (- (lrand 2) 1)))
            radius)))

(defn update-bird
  [bird dt objects]  
  (let [proximity-list (sort-by-proximity (:position bird) objects)         
        bird (fly (update-object-kinematics bird dt) dt proximity-list)
        bird (assoc bird
               :energy (- (:energy bird) bird-cost-of-living))]
    (when (pos? (:energy bird))
      (if (> (:energy bird) child-bird-energy-cost)
        (list (make-bird (:friendliness bird) (offset-position (:position bird) 2))
              (assoc bird
                :energy (- (:energy bird) child-bird-energy-cost child-birth-energy-loss)))
        bird))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Collision handling
;;
;; Collision functions take [collider collidee] and return [collider collidee]
;;   Both can be modified; however, two collisions are actually computed [a b] and [b a].

(defn bump
  "Collision between two birds. This is called on [bird1 bird2] and [bird2 bird1] independently
so we only modify bird1."
  [bird1 bird2]
  [(assoc bird1 :energy (- (:energy bird1) bump-energy-cost))     
   bird2])

(defn eat
  "Collision between a bird and food. Nom nom nom."
  [bird food]
  (let [energy-from-food (min energy-from-eating-food (:energy food))]
    [(assoc bird :energy (+ (:energy bird) energy-from-food))       
     (assoc food :energy (- (:energy food) energy-from-food))]))
       

(defn land
  "Collision between a bird and the floor."
  [bird floor]
  [(assoc bird
     :position (vec3 (.x (:position bird)) 0 (.z (:position bird)))
     :acceleration (vec3 0 0 0)
     :velocity (vec3 0 0 0))
   floor])

(reset! collision-handlers
  {[:bird :bird] bump
   [:bird :food] eat
   [:bird :floor] land
   })

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; World updates

(defn identity-update
  "Return the object to be updated unchanged."
  [obj dt objects]
  obj)

(reset! update-handlers
  {:bird update-bird
   :food update-food
   :floor identity-update})

(defn report
  "Report on the state of the simulation."
  [state]
  (if (< (+ (:last-report-time state) 1) (:simulation-time state))
    (do (println)		  
			  (println "time:" (:simulation-time state))
			  (println "num-birds:" (count (filter bird? (:objects state))))
			  (println "energy in system:" (reduce + (map :energy (:objects state))))
        (assoc state
               :last-report-time (:simulation-time state)))
    state))
     

(defn update
  "Update the world."
  [[dt time] state]  
  (let [state (report state)
        objects (handle-collisions (:objects state) collision-handlers)
        updated-objects (doall (filter inside-boundary? (update-objects objects (:iteration-step-size state))))
        num-birds-to-add (- minimum-num-birds
                            (count (filter bird? updated-objects)))]
    (assoc state
      :simulation-time (+ (:simulation-time state) (:iteration-step-size state))
      :objects (concat updated-objects
                       (when (pos? num-birds-to-add)
                         (repeatedly num-birds-to-add random-bird))))))

(start-gui 0.01 update)

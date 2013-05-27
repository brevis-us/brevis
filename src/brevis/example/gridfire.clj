;(ns NeuralGestureControl.simulation.gridfire
(ns brevis.example.gridfire
  (:require [clojure.zip :as zip])
  (:use [brevis.graphics.basic-3D]
        [brevis.physics collision core space utils]
        [brevis.shape core box sphere cone]
        [brevis.core]
        [cantor]))  

;; ## Gridfire
;;
;; Oh no, you are stranded in a discrete world that has caught on fire.
;;   Put out them fires before its too late!
;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Globals

(defn load-default-map
  []
  {:width 50 :height 50 :walls []}
  #_(read-string (slurp "resources/maps/map_empty.clj"))
  #_(read-string (slurp "resources/maps/map_001.clj")))

(def dunit 2);; The unit of measure for visualization
(def fire-growth-rate 0.01);; increase of energy of fire per unit time

(def parms
  (let [default-map (load-default-map)]  
        (atom (assoc default-map
                     :robots [[1 1] [25 25]]
                     :fires [[8 8] [20 20] [40 40]]))))         
;			   :walls (bounding-wall 10 10)}));; walls essentially encodes the map

(def fire-locations (atom #{}))
(def robot-locations (atom #{}))
(def wall-locations (atom #{}))

(def neighborhood-radius 5)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Utilities

(defn grid-to-3d
  "Convert 2D grid coordinates to a 3D vector"
  [coords]
  (mul (vec3 (first coords) 0 (second coords)) dunit))

(defn get-adjacent-coordinates
  "Return the neighboring coordinates, or nil if off edge of world."
  [coords dir]
  (cond
    (= dir :n) (when (< (second coords) (:height @parms)) 
                 [(first coords) (inc (second coords))])
    (= dir :s) (when (> (second coords) 0)
                 [(first coords) (dec (second coords))])
    (= dir :e) (when (< (first coords) (:width @parms)) 
                 [(inc (first coords)) (second coords)])
    (= dir :w) (when (> (first coords) 0)
                 [(dec (first coords)) (second coords)])))

(defn get-neighbor-coords
  "Return the neighbor coords within radius around target coord."
  [radius coord]  
  (cond (nil? coord) nil
    (zero? radius) #{coord}
    :else
    (let [nbrs (apply concat (map #(get-neighbor-coords (dec radius) (get-adjacent-coordinates coord %)) [:n :e :w :s]))]
      (into #{} (filter #(not (nil? %)) nbrs)))))            

;; A hashmap that says the neighbor coords of each square
(def neighborhood-lookup
  (let [all-coords (for [x (range (:width @parms)) y (range (:height @parms))] 
                     [x y])]
    (zipmap all-coords
            (doall (map #(get-neighbor-coords neighborhood-radius %) all-coords)))))
  
(defn lookup-neighbors
  "Return the subsequence of objects which are neighbors of the target."
  [target objects]
  (let [nbrhood (neighborhood-lookup (:grid-coords target))]
    (filter (fn [obj]
              (some #(= (:grid-coords obj) %) nbrhood)) objects)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Wall

(defn wall?
  "Is a thing a wall?"
  [thing]
  (= (:type thing) (:fire)))

(defn make-wall
  "Make wall at a specific location"
  [coords]
  (swap! wall-locations conj coords) 
  (make-real {:type :wall
              :color [0.6 0.7 0.7]
              :grid-coords coords
              :position (grid-to-3d coords)
              :shape (create-box dunit dunit dunit)}))    

(defn bounding-wall
  "Create a list of coordinates on the boundary of a world."
  [width height]
  (concat (map #(vector 0 %) (range height))
          (map #(vector (dec width) %) (range height))
          (map #(vector % 0) (range width))
          (map #(vector % (dec height)) (range width))))

(defn gen-map                                                                                                                                                                                                                                                                 
  [width height]                                                                                                                                                                                                                                                              
  (let [boundary (bounding-wall width height)                                                                                                                                                                                                                                 
                 num-rooms 5                                                                                                                                                                                                                                                  
                 room-width (/ width num-rooms)                                                                                                                                                                                                                               
                 room-length 10                                                                                                                                                                                                                                               
                 rooms (for [n (range num-rooms)                                                                                                                                                                                                                              
                               side [1 0]]                                                                                                                                                                                                                                    
                            (let [y (* side (- height room-length))]                                                                                                                                                                                                          
                              (concat (map #(vector (* room-width (dec n)) %) (range y (+ y room-length)))                                                                                                                                                                    
                                      (map #(vector (* room-width n) %) (range y (+ y room-length))))))]                                                                                                                                                                      
    {:width width :height height                                                                                                                                                                                                                                              
    :walls (concat boundary rooms)}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Fire

(defn fire?
  "Is a thing fire?"
  [thing]
  (= (:type thing) :fire))

(def init-nrg 0.01)
(defn make-fire
  "Make fire at a specific location"
  [coords]
  #_(println coords @fire-locations)
  (swap! fire-locations conj coords)   
  (let [nrg-dim (+ 0.25 (* 0.001 init-nrg))]
  (make-real {:type :fire
              :color [0.7 0.3 0.2]
              :energy 0
              :grid-coords coords
              :position (grid-to-3d coords)
              :shape (create-box dunit nrg-dim dunit)})))

(defn fire-spread-probability
  "What is the probability that this fire will spread."
  [fire] 
  (min (* 0.001 (:energy fire)) 1))

(defn update-fire
  "Update a fire object"
  [fire dt nbrs]
  ;; delete dead fire
  (when (< (rand) (fire-spread-probability fire))
    (let [dir (rand-nth [:n :e :w :s]);; Choose a random direction
          adj-coords (get-adjacent-coordinates (:grid-coords fire) dir)]
          ;nbrs (compute-neighborhood fire nbrs)]
      (when (and adj-coords (not (contains? @fire-locations adj-coords))) 
                 ;(not (some #(= adj-coords (:grid-coords %)) nbrs)))
        (add-object (make-fire adj-coords)))))
  (let [nrg-dim (+ 0.25 (* 0.001 (:energy fire)))]
    (assoc (resize-shape fire (vec3 dunit nrg-dim dunit))
           :energy (+ (:energy fire) dt))))

(add-update-handler :fire update-fire)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Robots

(defn robot?
  "Is a thing a robot?"
  [thing]
  (= (:type thing) :robot))

(defn make-robot
  "Make a new robot with the specified program. At the specified location."
  [coords]  
  (make-real {:type :robot
              :color [0 0.5 0.25]
              :score 0
              :grid-coords coords
              :position (grid-to-3d coords)
              :shape (create-sphere (/ dunit 2))}))
              ;:shape (create-cone)})

(defn direction-to
  "Return the action that moves towards a coordinate"
  [from to]
  (let [dx (- (first to) (first from))
        dy (- (second to) (second from))
        ydir (cond (pos? dy) :n
               (neg? dy) :s
               :else (rand-nth [:n :s])) 
        xdir (cond (pos? dx) :e
               (neg? dx) :w
               :else (rand-nth [:e :w]))] 
    (cond (> (Math/abs dx) (Math/abs dy)) xdir
      (< (Math/abs dx) (Math/abs dy)) ydir
      :else (rand-nth [xdir ydir]))))
              
(defn update-robot-nil
  "Do not do anything to a robot."
  [robot dt objects]
  robot)

(defn update-robot-brownianheat
  "Update a robot with brownian motion towards nearby heat sources."
  [robot dt objects]  
  (let [nbrs (lookup-neighbors robot (filter fire? objects))
        dir (if (empty? nbrs)
              (rand-nth [:n :e :w :s])              
              (direction-to (:grid-coords robot) (:grid-coords (rand-nth nbrs))))
        new-grid-coords (get-adjacent-coordinates (:grid-coords robot) dir)
        nbr (some #(when (= (:grid-coords %) new-grid-coords) %) nbrs)
        dscore (if nbr (:energy nbr) 0)]        
    (when nbr       
      (del-object nbr))
    #_(println "tmp" (:score robot) dscore nbr)
    #_(println dir new-grid-coords (:grid-coords robot))
    (if new-grid-coords
	    (assoc (move robot (grid-to-3d new-grid-coords))
	           :score (+ (:score robot) dscore)
	           :grid-coords new-grid-coords)
     robot)))

(defn update-robot-gradientheat
  "Update a robot with brownian motion towards nearby heat sources."
  [robot dt objects]  
  (let [nbrs (lookup-neighbors robot (filter fire? objects))
        heatmap {:n (reduce + (map :energy (filter #(> (first (:grid-coords %)) (first (:grid-coords robot))) nbrs))),
                 :s (reduce + (map :energy (filter #(< (first (:grid-coords %)) (first (:grid-coords robot))) nbrs))),
                 :e (reduce + (map :energy (filter #(> (second (:grid-coords %)) (second (:grid-coords robot))) nbrs))),
                 :w (reduce + (map :energy (filter #(< (second (:grid-coords %)) (second (:grid-coords robot))) nbrs)))}
        max-heat (apply max (vals heatmap))
        max-dirs (map first (filter #(= (second %) max-heat) heatmap))
        dir (rand-nth max-dirs)
        new-grid-coords (get-adjacent-coordinates (:grid-coords robot) dir)
        nbr (some #(when (= (:grid-coords %) new-grid-coords) %) nbrs)
        dscore (if nbr (:energy nbr) 0)]        
    (println heatmap dir)
    (java.lang.Thread/sleep 100)
    (when nbr       
      (del-object nbr))
    #_(println "tmp" (:score robot) dscore nbr)
    #_(println dir new-grid-coords (:grid-coords robot))
    (if new-grid-coords
	    (assoc (move robot (grid-to-3d new-grid-coords))
	           :score (+ (:score robot) dscore)
	           :grid-coords new-grid-coords)
     robot)))

;(add-update-handler :robot update-robot); This tells the simulator how to update these objects
(add-update-handler :robot update-robot-brownianheat)
;(add-update-handler :robot update-robot-gradientheat)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Collision handling
;;
;; Collision functions take [collider collidee] and return [collider collidee]
;;   Both can be modified; however, two independent collisions are actually computed [a b] and [b a].

(defn firefight-score
  "How many points for colliding with the fire."
  [robot fire]
  (:energy fire))

(defn firefight
  "Put up your dukes, fire"
  [robot fire]
  [(assoc robot
          :score (+ (:score robot) (firefight-score robot fire)))
   (assoc fire
          :energy 0)])

(defn bump
  "Collision between two robots. This is called on [robot1 robot2] and [robot2 robot1] independently
so we only modify robot1."
  [robot1 robot2]
  [(assoc robot1 :color [(rand) (rand) (rand)])
   robot2])

(defn land
  "Collision between a robot and the floor."
  [robot floor]
  (when (or (nil? robot) (nil? floor))
    (println "robot" robot) (println "Floor" floor))
  [(move (set-velocity (assoc robot
                        :acceleration (vec3 0 0 0))
                       (vec3 0 0 0))
         (vec3 0 0 0))
   floor])

(add-collision-handler :robot :robot bump)
;(add-collision-handler :robot :floor land)
(add-collision-handler :robot :fire firefight)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (init-world)
  (reset! fire-locations #{})
  (reset! robot-locations #{})
  (reset! wall-locations #{})
  (set-dt 0.1)
  (disable-collisions)
  (disable-neighborhoods)
  (reset! *gui-state* 
          (assoc @*gui-state*
                 :shift-x -60
                 :shift-y -20
                 :shift-z -165))                 
  (add-object (make-floor 500 500))
  (doseq [coords (:robots @parms)]
    (add-object (make-robot coords)))
  (doseq [coords (:fires @parms)]
    (add-object (make-fire coords)))
  (doseq [coords (:walls @parms)]
    (add-object (make-wall coords))))

;; Start zee macheen
(defn -main [& args]
  (start-gui initialize-simulation))

(-main)
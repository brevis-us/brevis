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

(ns brevis.example.swarm
  (:gen-class
    :name brevis.example.BrevisExampleSwarm
    :main main)
  (:use [brevis.graphics.basic-3D]
        [brevis.physics collision core space utils]
        [brevis.shape box sphere cone]
        [brevis core osd vector camera utils display]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Swarm
;;
;; ![](img/brevis_example_swarm.png?raw=true)
;;
;; Swarm simulations are models of flocking behavior in collections of organisms.   
;;
;; These algorithms were first explored computationally in:
;;
;;   Reynolds, Craig W. "Flocks, herds and schools: A distributed behavioral model." ACM SIGGRAPH Computer Graphics. Vol. 21. No. 4. ACM, 1987.
;;
;; Todo:
;; - auto-centering of camera (and skybox?)
;; 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Globals

(def num-birds (atom 500))

(def avoidance-distance (atom 10))
(def boundary 250)

(def speed 25)
(def max-acceleration 10)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Birds

(defn bird?
  "Is a thing a bird?"
  [thing]
  (= (get-type thing) :bird))

(defn random-bird-position
  "Returns a random valid bird position."
  []
  (let [w @num-birds
        h w]
    (vec3 (- (rand w) (/ w 2)) 
          (+ 59.5 (rand 10));; having this bigger than the neighbor radius will help with speed due to neighborhood computation
          (- (rand h) (/ h 2)))))

(defn make-bird
  "Make a new bird with the specified program. At the specified location."
  [position]  
  (move (make-real {:type :bird
                    :color (vec4 1 0 0 1)
                    :shape (create-cone 2.2 1.5)})
        position))
  
(defn random-bird
  "Make a new random bird."
  []
  (make-bird (random-bird-position)))    

(defn bound-acceleration
  "Keeps the acceleration within a reasonable range."
  [v]  
  (if (> (length v) max-acceleration)
    (mul (div v (length v)) max-acceleration)
    v))

(defn periodic-boundary
  "Change a position according to periodic boundary conditions."
  [pos]
  (let [x (x-val pos)
        y (y-val pos)
        z (z-val pos)]
    (vec3 (cond (> x boundary) (- (mod x boundary) boundary)
                (< x (- boundary)) (mod (- x) boundary)
                :else x)
          (cond (> y boundary) (- (mod y boundary) boundary)
                (< y (- boundary)) (mod (- y) boundary)
                :else y)
          (cond (> z boundary) (- (mod z boundary) boundary)
                (< z (- boundary)) (mod (- z) boundary)
                :else z))))

(defn fly
  "Change the acceleration of a bird."
  [bird]
  (let [nbrs (filter bird? (get-neighbor-objects bird))      
        ;tmp (println (count nbrs))
        ;tmp (do (doseq [nbr nbrs] (print (get-position nbr))) (println)) 
        bird-pos (get-position bird)
        
        ;; Actual closest bird, a little slow
        ;bird-dists (map #(length-vec3 (sub-vec3 (get-position %) bird-pos)) nbrs)
        #_closest-bird #_(when-not (empty? nbrs)
                          (nth nbrs 
                               (reduce #(if (< (nth bird-dists %1) (nth bird-dists %2)) %1 %2) (range (count bird-dists)))))
        
        closest-bird (first nbrs)
        
        new-acceleration (if-not closest-bird
                           ;; No neighbor, move randomly
                           (elmul (vec3 (- (rand) 0.5) (- (rand) 0.5) (- (rand) 0.5))
                                  (mul bird-pos -1.0))
                           (let [dvec (sub bird-pos (get-position closest-bird)) 
                                 len (length dvec)]
                             (add (sub (get-velocity closest-bird) (get-velocity bird)); velocity matching
                                  (if (<= len @avoidance-distance)
                                    ;; If far from neighbor, get closer
                                    dvec
                                    ;; If too close to neighbor, move away
                                    (add (mul dvec -1.0)
                                         (vec3 (rand 0.1) (rand 0.1) (rand 0.1)))))));; add a small random delta so we don't get into a loop                                    
        new-acceleration (if (zero? (length new-acceleration))
                           new-acceleration
                           (mul new-acceleration (/ 1 (length new-acceleration))))]
    (set-acceleration
      (if (or (> (java.lang.Math/abs (x-val bird-pos)) boundary) 
              (> (java.lang.Math/abs (y-val bird-pos)) boundary) 
              (> (java.lang.Math/abs (z-val bird-pos)) boundary)) 
        (move bird (periodic-boundary bird-pos) #_(vec3 0 25 0))
        bird)
      (bound-acceleration
        new-acceleration
        #_(add (mul (get-acceleration bird) 0.5)
             (mul new-acceleration speed))))))

(enable-kinematics-update :bird); This tells the simulator to move our objects
(add-update-handler :bird fly); This tells the simulator how to update these objects
;(add-parallel-update-handler :bird fly); This tells the simulator how to update these objects (in parallel)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Collision handling
;;
;; Collision functions take [collider collidee] and return [collider collidee]
;;   Both can be modified; however, two independent collisions are actually computed [a b] and [b a].

(defn bump
  "Collision between two birds. This is called on [bird1 bird2] and [bird2 bird1] independently
so we only modify bird1."
  [bird1 bird2]  
  [(set-color bird1 (vec4 (rand) (rand) (rand) 1))
   bird2])

(defn land
  "Collision between a bird and the floor."
  [bird floor]
  [(set-velocity (set-acceleration bird (vec3 0 10.5 0)) (vec3 0 10.0 0));; maybe move as well       
   floor])

(add-collision-handler :bird :bird bump)
(add-collision-handler :bird :floor land)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  ;(swap! brevis.globals/*gui-state* assoc :gui false)
  (init-world)
  (init-view)  
  
  #_(change-skybox
     ["img/night_skybox/front.jpg"
      "img/night_skybox/left.jpg"
      "img/night_skybox/back.jpg"
      "img/night_skybox/right.jpg"
      "img/night_skybox/up.jpg"
      "img/night_skybox/down.jpg"])
  ;(swap! brevis.globals/*gui-state* assoc :gui false)
  #_(.moveFromLook (:camera @brevis.globals/*gui-state*) 0 100 0)
  #_(set-dt 0.1)
  
  #_(set-camera-information (vec3 -10.0 -50.0 -200.0) (vec4 1.0 0.0 0.0 0.0))
  (set-camera-information (vec3 -10.0 57.939613 -890.0) (vec4 1.0 0.0 0.0 0.0))
  
  #_(disable-skybox)
  
  (set-dt 1)
  (set-neighborhood-radius 250)
  (default-display-text)
  (add-object (move (make-floor 500 500) (vec3 0 (- boundary) 0)))
  (dotimes [_ @num-birds]
    (add-object (random-bird))))

;; Start zee macheen
(defn -main [& args]
  #_(start-nogui initialize-simulation)
  (if-not (empty? args)
    (start-nogui initialize-simulation)
    (start-gui initialize-simulation)))

;; For autostart with Counterclockwise in Eclipse
(when (find-ns 'ccw.complete)
  (-main))
;(-main :nogui)

;; Java interop

(defn -init
  "Init function to be used as a constructor."
  ([]
    (-init 500 10))
  ([init-num-birds init-avoidance-distance]
    (reset! num-birds init-num-birds)
    (reset! avoidance-distance init-avoidance-distance)))


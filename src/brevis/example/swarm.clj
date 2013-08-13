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
  (:use [brevis.graphics.basic-3D]
        [brevis.physics collision core space utils vector]
        [brevis.shape box sphere cone]
        [brevis.core]))

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
;;
;; Todo:
;; - auto-centering of camera (and skybox?)
;; - Voronoi neighborhoods (or some other acceleration)
;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Globals

(def num-birds 100)

(def memory (atom 0.0))
(def avoidance (atom 1.8))
(def clustering (atom 1.05))
(def centering (atom 0.01))

(def max-acceleration 10)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Birds

(defn bird?
  "Is a thing a bird?"
  [thing]
  (= (:type thing) :bird))

(defn random-bird-position
  "Returns a random valid bird position."
  []
  (vec3 (- (rand num-birds) (/ num-birds 2)) 
        (+ 9.5 (rand 10));; having this bigger than the neighbor radius will help with speed due to neighborhood computation
        (- (rand num-birds) (/ num-birds 2))))

(defn make-bird
  "Make a new bird with the specified program. At the specified location."
  [position]  
  (move (make-real {:type :bird
              :color (vec4 1 0 0 1)
              :shape (create-sphere)})
              ;:shape (create-cone)})
        position))
  
(defn random-bird
  "Make a new random bird."
  []
  (make-bird (random-bird-position)))    

(defn bound-acceleration
  "Keeps the acceleration within a reasonable range."
  [v]
  v
  #_(if (> (length v) max-acceleration)
    (mul (div v (length v)) max-acceleration)
    v))

(defn fly
  "Change the acceleration of a bird."
  [bird dt nbrs]
  #_(println "fly: bird=" (:uid bird) " nbrs=" nbrs " " (count nbrs))
  #_(println "fly:" (get-position bird))
  (let [closest-bird (if (zero? (count nbrs))
                       bird
                       (first nbrs))
        centroid (if (zero? (count nbrs))
                   (get-position bird)
                   (div (reduce add (map get-position nbrs)) 
                        (count nbrs)))
        d-closest-bird (sub (get-position closest-bird) (get-position bird))
        d-centroid (sub centroid (get-position bird))
        d-center (sub (vec3 0 10 0) (get-position bird))
        new-acceleration (bound-acceleration
                           (add (mul (get-acceleration bird) @memory)
                                (mul d-center @centering)
                                (mul d-closest-bird @avoidance)
                                (mul d-centroid @clustering)))]
    #_(println d-center d-closest-bird d-centroid)    
    (set-acceleration
      (orient-object bird (vec3 0 0 1) (get-velocity bird))
      new-acceleration)))

(defn update-bird
  "Update a bird based upon its flocking behavior and the physical kinematics."
  [bird dt objects]  
  #_(println (get-time) bird)
  (let [objects (filter bird? objects)
        ;nbrs (compute-neighborhood bird objects)]
        nbrs (get-neighbor-objects bird)]
    (update-object-kinematics 
      (fly bird dt nbrs) dt)))

(add-update-handler :bird update-bird); This tells the simulator how to update these objects

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
   bird2]
  #_[(assoc bird1 :color [(rand) (rand) (rand)])
   bird2])

(defn land
  "Collision between a bird and the floor."
  [bird floor]
  (when (or (nil? bird) (nil? floor))
    (println "Bird" bird) (println "Floor" floor))
  [(set-acceleration
     bird
     #_(set-velocity bird
                   (vec3 0 0.5 0))
     (vec3 0 0.5 0))         
   floor])

(add-collision-handler :bird :bird bump)
(add-collision-handler :bird :floor land)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (init-world)
  (set-dt 0.1)
  (set-neighborhood-radius 25)
  (default-display-text)
  (reset! collisions-enabled false)
  #_(enable-video-recording "swarm_demo")
  (add-object (make-floor 500 500))
  (dotimes [_ num-birds]
    (add-object (random-bird))))

;; Start zee macheen
(defn -main [& args]
  (if-not (empty? args)
    (start-nogui initialize-simulation)
    (start-gui initialize-simulation)))

(when (find-ns 'ccw.complete)
  (-main))
;(-main :nogui)
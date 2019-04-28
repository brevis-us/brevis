(ns us.brevis.example.swarm.plot-swarm
  (:use [us.brevis.physics collision core utils]
        [us.brevis.shape box sphere cone]
        [us.brevis core vector plot random utils]))

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

(def num-birds 500)

(def avoidance-distance (atom 10))

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
  (let [w num-birds
        h w]
    (vec3 (- (rand w) (/ w 2)) 
          (+ 59.5 (rand 10));; having this bigger than the neighbor radius will help with speed due to neighborhood computation
          (- (rand h) (/ h 2)))))

(defn make-bird
  "Make a new bird with the specified program. At the specified location."
  [position]  
  (assoc (move (make-real {:type :bird
                           :color (vec4 1 0 0 1)
                           :shape (create-cone 2.2 1.5)})
               position)
         :trait (lrand-int 3)))
  
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

(defn fly
  "Change the acceleration of a bird."
  [bird]
  (let [nbrs (filter bird? (get-neighbor-objects bird))      
        ;tmp (println (count nbrs))
        ;tmp (do (doseq [nbr nbrs] (print (get-position nbr))) (println))
        bird-pos (get-position bird)
        bird-dists (map #(length-vec3 (sub-vec3 (get-position %) bird-pos)) nbrs)
        closest-bird (when-not (empty? nbrs)
                       (nth nbrs 
                            (reduce #(if (< (nth bird-dists %1) (nth bird-dists %2)) %1 %2) (range (count bird-dists)))))

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
      (if (> (length bird-pos) 700)
        (move bird (vec3 0 25 0))
        bird)
      (bound-acceleration
        new-acceleration
        #_(add (mul (get-acceleration bird) 0.5)
             (mul new-acceleration speed))))))

(enable-kinematics-update :bird); This tells the simulator to move our objects
(add-update-handler :bird fly); This tells the simulator how to update these objects

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Collision handling
;;
;; Collision functions take [collider collidee] and return [collider collidee]
;;   Both can be modified; however, two independent collisions are actually computed [a b] and [b a].

(defn bump
  "Collision between two birds. This is called on [bird1 bird2] and [bird2 bird1] independently
so we only modify bird1."
  [bird1 bird2]  
  [(assoc (set-color bird1 (vec4 (lrand) (lrand) (lrand) 1))
          :trait (lrand-int 3))
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
  ;(swap! brevis.globals/*gui-state* assoc :gui false)
  (.moveFromLook (:camera @us.brevis.globals/*gui-state*) 0 100 0)
  #_(set-dt 0.1)
  (set-dt 1)
  (set-neighborhood-radius 100)
  ;(add-object (make-floor 500 500))
  (dotimes [_ num-birds]
    (add-object (random-bird)))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## plotting code 

 (add-plot-handler
   (fn [] 
     (let [positions (map get-position (filter bird? (all-objects)))
           center (vec3 (/ (apply + (map #(.x %) positions)) (count positions))
                        (/ (apply + (map #(.y %) positions)) (count positions))
                        (/ (apply + (map #(.z %) positions)) (count positions)))
           distances (map #(length (sub center %)) positions)
           avg-distance (/ (apply + distances) (count distances))]
       [(* (get-time) (get-dt)) avg-distance]))
   :interval 200
   :title "Avg dist from centroid")
 
 (add-histogram-handler
   (fn [] 
     (let [birds (filter bird? (all-objects))]
       (map :trait birds)))
   :title "random traits"))


;; Start zee macheen
(defn -main [& args]
  #_(start-nogui initialize-simulation)
  (if-not (empty? args)
    (start-nogui initialize-simulation)
    (start-gui initialize-simulation)))

(autostart-in-repl -main)

(ns us.brevis.example.swarm.neighborhood-line-swarm
  (:use [us.brevis.physics collision core utils]
        [us.brevis.shape cone box]
        [us.brevis core vector utils random]
        [us.brevis.graphics core]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Globals

(def num-birds 250)

(def avoidance-distance (atom 10))
(def boundary 250)

(def speed 5)
(def max-acceleration 1)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Birds

(defn bird?
  "Is a thing a bird?"
  [thing]
  (= (get-type thing) :bird))

(defn random-bird-position
  "Returns a random valid bird position."
  []
  (lrand-vec3 (- (/ num-birds 2)) (/ num-birds 2)
              (- (/ num-birds 2)) (/ num-birds 2)
              (- (/ num-birds 2)) (/ num-birds 2)))

(defn make-bird
  "Make a new bird with the specified program. At the specified location."
  [position]  
  (move (make-real {:type :bird
                    :color (vec4 1 0 0 1)
                    :shape (create-cone 8.2 2.5)})
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

(defn bound-velocity
  "Keeps the acceleration within a reasonable range."
  [v]  
  (if (> (length v) speed)
    (mul (div v (length v)) speed)
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
        bird-pos (get-position bird)        
        closest-bird (get-closest-object bird-pos nbrs)                
        new-acceleration (if-not closest-bird
                           ;; No neighbor, move randomly
                           (elmul (lrand-vec3 -0.5 0.5 -0.5 0.5 -0.5 0.5)                             
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
    (set-velocity
      (set-acceleration
        (if (or (> (java.lang.Math/abs (x-val bird-pos)) boundary) 
                (> (java.lang.Math/abs (y-val bird-pos)) boundary) 
                (> (java.lang.Math/abs (z-val bird-pos)) boundary)) 
          (move bird (periodic-boundary bird-pos) #_(vec3 0 25 0))
          bird)
        (bound-acceleration
          new-acceleration
          #_(add (mul (get-acceleration bird) 0.5)
               (mul new-acceleration speed))))
      (bound-velocity (get-velocity bird)))))

(enable-kinematics-update :bird); This tells the simulator to move our objects
(add-update-handler :bird fly); This tells the simulator how to update these objects
;(add-parallel-update-handler :bird fly); This tells the simulator how to update these objects (in parallel)

;(add-global-update-handler 10
;                           (fn []
;                             (reset! visual-overlays [])
;                             ;(let [rand-bird (lrand-nth (filter bird? (all-objects)))]
;                             (doseq [rand-bird (filter bird? (all-objects))]
;                               (doseq [nbr (get-neighbor-objects rand-bird)]
;                                 (add-line (get-uid rand-bird) (get-uid nbr))))))

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

(defn make-floor
   "Make a floor object."
   [w h]
   (move (make-real {:color (vec4 0.8 0.8 0.8 1)
                     :shininess 80
                     :type :floor
                     :density 8050
                     :hasShadow false
                     ;                    :texture *checkers*
                     :shape (create-box w 0.1 h)})
         (vec3 0 -3 0)))

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (init-world)
  (init-view)
  
  (set-dt 1)
  (set-neighborhood-radius 100)
  #_(add-object (move (make-floor 500 500) (vec3 0 (- boundary) 0)))
  (dotimes [_ num-birds]
    (add-object (random-bird))))

;; Start zee macheen
(defn -main [& args]
  (if-not (empty? args)
    (start-nogui initialize-simulation)
    (start-gui initialize-simulation)))

(autostart-in-repl -main)

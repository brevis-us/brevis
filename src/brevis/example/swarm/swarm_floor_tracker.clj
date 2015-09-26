(ns brevis.example.swarm.swarm-floor-tracker
  (:gen-class)
  (:use [funimage imp]
        [brevis.graphics basic-3D texture]
        [brevis.physics collision core space utils]
        [brevis.shape box sphere cone]
        [brevis core osd vector camera utils display image random]))

;; ## Globals

(def num-birds (atom 500))

(def avoidance-distance (atom 25))
(def boundary 250)

(def speed 5)
(def max-acceleration 10)

(def floor (atom nil))
(def floor-imp (atom nil))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Birds

(defn bird?
  "Is a thing a bird?"
  [thing]
  (= (get-type thing) :bird))

(defn random-bird-position
  "Returns a random valid bird position."
  []
  (vec3 (- (lrand (* 2 boundary)) boundary)
        (- (lrand (* 2 boundary)) boundary)
        (- (lrand (* 2 boundary)) boundary))
  #_(let [w @num-birds
         h w]
     (vec3 (- (rand w) (/ w 2)) 
           (+ 59.5 (rand 10));; having this bigger than the neighbor radius will help with speed due to neighborhood computation
           (- (rand h) (/ h 2)))))

(defn make-bird
  "Make a new bird. At the specified location."
  [position]  
  (move (make-real {:type :bird
                    :color (vec4 1 0 0 0.25)
                    :shape (create-cone 25.2 16.5)
                    #_(create-cone 10.2 1.5)
                    })
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
  (let [x (x-val-vec3 pos)
        y (y-val-vec3 pos)
        z (z-val-vec3 pos)]
    (vec3 (loop [x x] (cond (> x boundary) (recur (- x (* 2 boundary)))
                            (< x (- boundary)) (recur (+ x (* 2 boundary)))
                            :else x))
          (loop [y y] (cond (> y boundary) (recur (- y (* 2 boundary)))
                            (< y (- boundary)) (recur (+ y (* 2 boundary)))
                            :else y))
          (loop [z z] (cond (> z boundary) (recur (- z (* 2 boundary)))
                            (< z (- boundary)) (recur (+ z (* 2 boundary)))
                            :else z)))
    #_(vec3 (cond (> x boundary) (- (mod x boundary) boundary)
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
                
        closest-bird (when-not (empty? nbrs)
                       (get-closest-neighbor bird))
        
        new-acceleration (if-not closest-bird
                           ;; No neighbor, move randomly
                           (vec3 (- (lrand) 0.5) (- (lrand) 0.5) (- (lrand) 0.5))
                           (let [dvec (sub-vec3 bird-pos (get-position closest-bird)) 
                                 len (length-vec3 dvec)]
                             (add (sub (get-velocity closest-bird) (get-velocity bird)); velocity matching
                                  (if (<= len @avoidance-distance)
                                    ;; If far from neighbor, get closer
                                    dvec
                                    ;; If too close to neighbor, move away
                                    (add-vec3 (mul-vec3 dvec -1.0)
                                         (vec3 (lrand 0.1) (lrand 0.1) (lrand 0.1)))))));; add a small random delta so we don't get into a loop                                    
        new-acceleration (if (zero? (length new-acceleration))
                           new-acceleration
                           (mul new-acceleration (/ 1 (length new-acceleration))))]    
    (if (or (> (java.lang.Math/abs (x-val bird-pos)) boundary) 
            (> (java.lang.Math/abs (y-val bird-pos)) boundary) 
            (> (java.lang.Math/abs (z-val bird-pos)) boundary)) 
      (set-velocity (set-acceleration (move bird (periodic-boundary bird-pos) #_(vec3 0 25 0))
                                      (vec3 (- (lrand) 0.5) (- (lrand) 0.5) (- (lrand) 0.5)))
                    (vec3 (- (lrand) 0.5) (- (lrand) 0.5) (- (lrand) 0.5)))
      (set-velocity (set-acceleration bird
                                      (bound-acceleration
                                        new-acceleration))
                    (bound-velocity (get-velocity bird))))
    ))

(enable-kinematics-update :bird); This tells the simulator to move our objects
(add-update-handler :bird fly); This tells the simulator how to update these objects
;(add-parallel-update-handler :bird fly); This tells the simulator how to update these objects (in parallel)

;; Actually handle drawing to the floor and updating the texture
(add-global-update-handler 10
(fn []
  (let [floor-obj (first (filter #(= (get-type %) :floor) (all-objects)))]
    (doseq [obj (all-objects)];filter for birds
      (let [pos (add-vec3 (get-position obj)
                          (vec3 boundary boundary boundary))
            x (max 0 (min (get-width @floor-imp) (x-val-vec3 pos))) 
            y (- (dec (get-height @floor-imp))
                 (max 0 (min (get-height @floor-imp) (y-val-vec3 pos))))
            delta 25]        
        (reset! floor-imp (put-pixel-double @floor-imp x y  
                                            (mod (+ (get-pixel-unsafe @floor-imp x y) delta)
                                                 255)))))
    (show-imp @floor-imp)
    (set-object (get-uid floor-obj)
                (set-texture-image floor-obj
                                   (.getBufferedImage @floor-imp)))
    )))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Collision handling
;;
;; Collision functions take [collider collidee] and return [collider collidee]
;; This is only called once per pair of colliding objects.

(defn bump
  "Collision between two birds."
  [bird1 bird2]  
  [(set-color bird1 (vec4 (rand) (rand) (rand) 0.25))
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
  
  (add-object
    (let [color-vec4 (vec4 1 1 1 1)
          amb-diff color-vec4
          position (vec3 0 200 0)
          light-id 0
          ]
      (add-light)
      (light-specular light-id #_color-vec4
                      (vec4 1 1 1 1)
                      #_(vec3-to-vec4 (mul-vec3 color-vec4 (/  num-lights))))
      (light-diffuse light-id amb-diff)
      (light-ambient light-id amb-diff)
      (assoc (move (make-real {:type :light-sphere
                               :color color-vec4
                               :shape (create-sphere 25)})
                   position)
             :light-id light-id)))
  
  (set-dt 1)
  (set-neighborhood-radius 50)
  (default-display-text)
  (reset! floor 
          (make-floor (* 2 boundary)
                      (* 2 boundary)))
  (reset! floor-imp 
          (create-imp :width (* 2 boundary) 
                      :height (* 2 boundary)
                      :type "8-bit")) 
  (add-object (move @floor (vec3 (- boundary) (- boundary) (- boundary))))
  (dotimes [_ @num-birds]
    (add-object (random-bird))))

;; Start zee macheen
(defn -main [& args]
  #_(start-nogui initialize-simulation)
  (if-not (empty? args)
    (start-nogui initialize-simulation)
    (start-gui initialize-simulation)))

(autostart-in-repl -main)

;; Java interop

(defn -init
  "Init function to be used as a constructor."
  ([]
    (-init 500 10))
  ([init-num-birds init-avoidance-distance]
    (reset! num-birds init-num-birds)
    (reset! avoidance-distance init-avoidance-distance)))


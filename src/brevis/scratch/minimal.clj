(ns brevis.scratch.minimal
  (:import (org.lwjgl LWJGLException Sys)
           (org.lwjgl.input Keyboard Mouse)
           (org.lwjgl.opengl Display DisplayMode GLContext)
           (brevis.graphics Basic3D))
  (:require [penumbra.app :as app]            
            [clojure.math.numeric-tower :as math]
            [penumbra.text :as text]
            [penumbra.data :as data]
            [penumbra.opengl.frame-buffer :as fb]
            [penumbra.opengl.effects :as glfx])
  (:use [brevis.graphics.basic-3D]
        [brevis.physics collision core space utils]
        [brevis.shape box sphere cone]
        [brevis osd vector globals input utils]
        [brevis.core :exclude [start-gui display]]))

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
(def max-acceleration 100)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## Birds

(defn bird?
  "Is a thing a bird?"
  [thing]
  (= (get-type thing) "bird"))

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

(defn fly
  "Change the acceleration of a bird."
  [bird dt nbrs]
  (let [nbrs (filter bird? (get-neighbor-objects bird))      
        ;tmp (println (count nbrs))
        ;tmp (do (doseq [nbr nbrs] (print (get-position nbr))) (println)) 
        closest-bird (when-not (empty? nbrs)
                       (first nbrs)
                       #_(rand-nth nbrs))
        new-acceleration (if-not closest-bird
                           ;; No neighbor, move randomly
                           (elmul (vec3 (- (rand) 0.5) (- (rand) 0.5) (- (rand) 0.5))
                                  (mul (get-position bird) -1.0))
                           (let [dvec (sub (get-position bird) (get-position closest-bird)) 
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
      (if (> (length (get-position bird)) 500)
        (move bird (vec3 0 25 0))
        bird)
      (add (mul (get-acceleration bird) 0.5)
           (mul new-acceleration speed)))))

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
  [(set-color bird1 (vec4 (rand) (rand) (rand) 1))
   bird2])

(defn land
  "Collision between a bird and the floor."
  [bird floor]
  [(set-velocity (set-acceleration bird (vec3 0 10.5 0)) (vec3 0 0 0));; maybe move as well       
   floor])

(add-collision-handler :bird :bird bump)
(add-collision-handler :bird :floor land)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (init-world)
  (init-view)
  (set-dt 0.1)
  (set-neighborhood-radius 1000)
  (default-display-text)
  (add-object (make-floor 500 500))
  (dotimes [_ num-birds]
    (add-object (random-bird))))

#_(defn display
  "Display the world."
  [[dt t] state]
  (let [state (if (:auto-camera state) (auto-camera state) state)]
    (enable :lighting)
    (enable :light0)
    #_(enable :light1)
    (enable :texture-2D)
    (disable :texture-gen-s)
		(disable :texture-gen-t)
    (shade-model :smooth)
    (enable :blend)
    (blend-func :src-alpha :one-minus-src-alpha)  
    (enable :normalize)
    (enable :depth-test)
    (depth-test :lequal)
    #_(enable :cull-face)
    #_(cull-face :black)
    ;GL11.glFrontFace (GL11.GL_CCW);
    (viewport 0 0 (:window-width @*gui-state*) (:window-height @*gui-state*))
    (gl-matrix-mode :projection)
    (gl-load-identity-matrix)
    ;should if on width>height
    ;(frustum-view 60.0 (/ (double (:window-width @*gui-state*)) (:window-height @*gui-state*)) 1.0 1000.0)
    (frustum-view 60.0 (/ (double (:window-width @*gui-state*)) (:window-height @*gui-state*)) 0.1 3000)
    #_(light 0 
         :specular [0.4 0.4 0.4 1.0];:specular [1 1 1 1.0]
         :position [0 1 0 0];;directional can be enabled after the penumbra update         
         ;:position [250 250 -100 1]         
         :diffuse [1 1 1 1])
    (color 1 1 1)
    (clear-color 0.5 0.5 0.5 0)
    #_(Basic3D/initGL)    
    #_(clear)
    (gl-matrix-mode :modelview)
    ;(GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST)
    (gl-load-identity-matrix)
    (use-camera (:camera @*gui-state*))
    (light 0
           :ambient [0 0 0 1]
           ;:specular [0.4 0.4 0.4 1.0];:specular [1 1 1 1.0]
           :position [1 1 1 0];;directional can be enabled after the penumbra update         
           ;:position [250 250 -100 1]         
           ;:diffuse [1 1 1 1]
           )    
    (draw-sky)
   (enable :lighting)
   (disable :texture-2D)
   ;(shade-model :flat)
   ;(enable :depth-test)
   ;(depth-test :less)
   (color 1 1 1)
	  (doseq [obj (get-objects)]
     (when (drawable? obj) ;; add a check to see if the object is in view
       (draw-shape obj)))
   (when @enable-display-text
     (update-display-text [dt t] state))
	  (app/repaint!)
   (when @*screenshot-filename*
     (screenshot @*screenshot-filename* state)
     (reset! *screenshot-filename* nil))
   (when (:record-video @*gui-state*)
     (screenshot (str (:video-name @*gui-state*) "_" @video-counter ".png") state)
     (swap! video-counter inc))   
     ;(screenshot (str (:video-name @*gui-state*) "_" (get-time) ".png") state))
   ))

(defn display
  "Render all objects."
  []
  (let [objs (all-objects)]
    (Basic3D/initFrame)
    #_(gl-matrix-mode :modelview)
    #_(gl-load-identity-matrix)
    (use-camera (:camera @*gui-state*))
    (doseq [obj objs]
      (when (drawable? obj) ;; add a check to see if the object is in view
       (draw-shape obj)))
    (Display/update)    
    ))

(defn simulate
  "Simulation loop."
  [initialize update]
  (let [width 800
        height 600]
    (Display/setLocation (/ (- (.getWidth (Display/getDisplayMode)) width) 2)
                         (/ (- (.getHeight (Display/getDisplayMode)) height) 2))
    (try 
      (Display/setDisplayMode (DisplayMode. width height))
      (Display/setTitle "Brevis")
      (Display/setVSyncEnabled true)
      (Display/create)
      (catch LWJGLException e
        (.printStackTrace e)))
    (try 
      (Keyboard/create)
      (Mouse/create)
      (catch LWJGLException e
        (.printStackTrace e)))
    (Basic3D/initGL)            
    (initialize)    
    (let [startTime (ref (java.lang.System/nanoTime))
          fps (ref 0)]
      (dotimes [k 10000]
        (update [1 1] {})
        (dosync (ref-set fps (inc @fps)))
        (when (> (java.lang.System/nanoTime) @startTime)
          (println "Update" k "FPS:" (double (/ @fps (/ (- (+ 1000000000 (java.lang.System/nanoTime)) @startTime) 1000000000))))
          (dosync 
            (ref-set startTime (+ (java.lang.System/nanoTime) 1000000000))
            (ref-set fps 0)))
        (display)))
    (Keyboard/destroy)
    (Mouse/destroy)
    (Display/destroy)
    ))

(defn start-gui 
  "Start the simulation with a GUI."
  ([initialize]
    (start-gui initialize java-update-world))    
  ([initialize update]
    (reset! *gui-message-board* (sorted-map))
    (when (.contains (System/getProperty "os.name") "indows")
      (reset! enable-display-text false))
	  (reset! *app-thread*
           (Thread. (fn []
                      (simulate initialize update)
                      #_(app/start
                             {:reshape reshape, :init (make-init initialize), :mouse-drag mouse-drag, :key-press key-press :mouse-wheel mouse-wheel, :update update, :display display
                              :key-release key-release
                              ;:mouse-move    (fn [[[dx dy] [x y]] state] (println )
                              ;:mouse-up       (fn [[x y] button state] (println button) state)
                              ;:mouse-click   (fn [[x y] button state] (println button) state)
                              ;:mouse-down    (fn [[x y] button state] (println button) state)
                              ;:mouse-wheel   (fn [dw state] (println dw) state)
                              }        
                             @*gui-state*))))
   (.start @*app-thread*)))

;; Start zee macheen
(defn -main [& args]
  (start-gui initialize-simulation))

;; For autostart with Counterclockwise in Eclipse
(when (find-ns 'ccw.complete)
  (-main))
;(-main :nogui)
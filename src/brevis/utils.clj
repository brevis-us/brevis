(ns brevis.utils
  (:require [me.raynes.conch :refer [programs with-programs let-programs]])
  (:import [brevis Engine BrObject]
           [java.io FileInputStream FileOutputStream
            ObjectInputStream ObjectOutputStream])
  (:use [brevis globals]
        [brevis.physics core]))



(defn get-time
  "Return the current time."
  []
  (.getTime ^Engine @*java-engine*))

(defn get-steps
  "Return the number of timesteps taken thus far. steps*dt==time"
  []
  (.getSteps ^Engine @*java-engine*))

(defn get-wall-time
  "Return the elapsed wall-clock time."
  []
  (.getWallTime ^Engine @*java-engine*))

(defn get-uid
  "Return the UID of an object."
  [^BrObject obj]
  (.getUID obj))

(defn add-object
  "Add an object to the current world."
  [^BrObject obj]
  (.addObject ^brevis.Engine @*java-engine* (get-uid obj) ^BrObject obj) 
  obj)

(defn del-object
  "Add an object to the current world."
  [^BrObject obj]
  (.deleteObject ^brevis.Engine @*java-engine* (get-uid obj)))

(defn add-update-handler
  "Associate an update function with a type."
  [type handler-fn]
  (let [uh (proxy [brevis.Engine$UpdateHandler] []
             (update [#^brevis.Engine engine #^Long uid #^Double dt]
               (let [obj (.getObject engine uid)]
                 (handler-fn obj))))]
    (.addUpdateHandler ^brevis.Engine @*java-engine* (str (name type)) uh)))

(defn add-global-update-handler
  "Add a global update handler with specified priority."
  [priority handler-fn]
  (let [gh (proxy [brevis.Engine$GlobalUpdateHandler] []
             (update [#^brevis.Engine engine]
               (handler-fn)))]
    (.addGlobalUpdateHandler ^brevis.Engine @*java-engine* priority gh)))

(defn all-object-uids-by-type
  "Return the collection of all UIDs."
  [type]
  (seq (.allObjectUIDsByType ^Engine @*java-engine* (name type))))

(defn set-object
  "Set the object at UID to a new version.
Set key :now? to true, to set a current object (make sure you mean it)"
  [^Long uid ^BrObject new-obj 
   & {:keys [now?]
      :or {now? false}}]
  (if now?
    (.setObject ^Engine @*java-engine* uid new-obj)
    (.addObject ^Engine @*java-engine* uid new-obj)))

(defn get-object
  "Return the object by UID"
  [^Long uid]
  (.getObject ^Engine @*java-engine* uid))

(defn add-parallel-update-handler
  "Associate an update function with a type."
  [type handler-fn]
  (add-global-update-handler 0
                             (fn []
                                (let [uids (all-object-uids-by-type type)
                                      agents (map #(agent % :error-handler (fn [agnt except] (println except))) uids)
                                      f (fn [uid]
                                          (let [obj (get-object uid)]
                                            (set-object uid
                                                        (handler-fn obj))))]
                                      (dorun (map #(send % f) agents))
                                      (apply await agents)
                                      (doall (map deref agents))))
                             #_(fn []
                                (let [uids (all-object-uids-by-type type)]
                                  (doall (pmap #(let [obj (get-object %)]
                                                 (set-object %
                                                             (handler-fn obj)))
                                              uids))))))

(defn all-objects
  "Return the collection of all objects."
  []
  (seq (.allObjects ^Engine @*java-engine*)))

(defn all-object-uids
  "Return the collection of all UIDs."
  []
  (seq (.allObjectUIDs ^Engine @*java-engine*)))

(defn get-dt
  []
  (.getDT ^Engine @*java-engine*))

(defn set-dt
  [new-dt]
  (.setDT ^Engine @*java-engine* new-dt))

(defn get-type
  "Return the type of an object."
  [^BrObject obj]
  ;; This should be done properly during make-real instead of faked here
  (keyword (.getType obj))
  #_(.getType obj))

(defn enable-collisions
  "Enable collision handling"
  []
  (.setCollisionsEnabled @*java-engine* true))

(defn disable-collisions
  "Disable collision handling"
  []
  (.setCollisionsEnabled @*java-engine* false))

(defn enable-neighborhoods
  "Enable neighborhood detection"
  []
  (.setNeighborhoodsEnabled @*java-engine* true))

(defn disable-neighborhoods
  "Disable neighborhood detection."
  []
  (.setNeighborhoodsEnabled @*java-engine* false))

(defn get-current-simulation-rate
  "Return the current rate of simulation."
  []  
  (.getCurrentSimulationRate @*java-engine*))

(defn empty-simulation
  "Empty everything from the simulated world."
  []
  (doseq [obj (all-objects)]
    (del-object obj)))


(defn add-terminate-trigger
  "Add a termination trigger. If it is a number, then it is a threshold on time, 
otherwise it should be a function that returns true/false"
  [trigger]
  (add-global-update-handler 10000 
    (fn [] 
      (when (and (not (nil? trigger))
                 (or (and (number? trigger)                      
                          (> (get-time) trigger))
                     (and (not (number? trigger))
                          (trigger))))
        (swap! *gui-state* assoc :close-requested true)))))

(defn get-objects
  "Return all objects in the simulation."
  []
  (seq (.toArray (.getObjects ^Engine @*java-engine*))))

(defn set-parallel
  "set parallel flag"
  [new-flag]
  (.setParallel @*java-engine* new-flag))
                                          
(defn get-parallel
  "get parallel flag"
  []
  (.getParallel @*java-engine*))

(defn enable-fps-display
  "Print FPS."
  []
  (swap! *gui-state* assoc :display-fps true))

(defn disable-fps-display
  "Stop printing FPS."
  []
  (swap! *gui-state* assoc :display-fps false))

(programs mkdir tar)
(defn save-simulation-state
  "[EXPERIMENTAL: be very afraid] Save the state of the simulation to filename."
  [filename]
  #_(mkdir (str filename "_brevis"))
  #_(spit filename
         (with-out-str
           (doseq [obj (all-objects)]
             (println (str obj))))
         :append true)
  (try 
    (let [fos (FileOutputStream. filename)
          out (ObjectOutputStream. fos)]
      (.writeObject out @*java-engine*))
    (catch Exception e (do (.printStackTrace e) (str "caught exception: " (.getMessage e) )))))

(defn load-simulation-state
  "[EXPERIMENTAL: be very afraid] Load a saved simulation state from filename."
  [filename]
  (try 
    (let [fis (FileInputStream. filename)
          in (ObjectInputStream. fis)
          engine (cast Engine (.readObject in @*java-engine*))]
      (reset! *java-engine* engine))
    (catch Exception e (str "caught exception: " (.getMessage e)))))

(defn add-destroy-hook
  "Add a destroy hook called on window close."
  [fn]
  (swap! destroy-hooks conj fn))

(defn make-abstract
  "Make an abstract object."
  [obj]  
  (let [uid (long (hash (gensym)))        ;; might not be safe
        uid (if (zero? uid) (inc uid) uid); 0 is used as a NIL UID
        obj (assoc obj        
                   :uid uid
                   :real false
                   :type :abstract)
        brobj (BrObject.)]    
    (.setUID brobj uid)
    (.setType brobj (str (name (:type obj))))    
    ;(.makeAbstract brobj @*java-engine*)
    brobj))


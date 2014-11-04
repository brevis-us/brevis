(ns brevis.utils
  (:require [me.raynes.conch :refer [programs with-programs let-programs]])
  (:import [brevis Engine]
           [java.io FileInputStream FileOutputStream
            ObjectInputStream ObjectOutputStream])
  (:use [brevis globals]
        [brevis.physics core utils]))

(programs mkdir tar)

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

(defn disable-skybox
  "Disable rendering of the skybox."
  []
  (swap! *gui-state* assoc :disable-skybox true))

(defn enable-skybox
  "Enable rendering of the skybox."
  []
  (swap! *gui-state* dissoc :disable-skybox))

(defn change-skybox
  "Files must contain: front, left, back, right, up, down"
  [files]
  (.changeSkybox brevis.graphics.basic-3D/*sky* files))

(defn add-destroy-hook
  "Add a destroy hook called on window close."
  [fn]
  (swap! destroy-hooks conj fn))


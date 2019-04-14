(ns brevis.graphics.sciview
  (:require [clojure.set :as cset]
            [brevis.utils :as utils]
            [fun.imagej.sciview :as sciview]
            [brevis.physics.utils :as physics]
            [brevis.utils :as utils]))

(defn create-sv-object
  "Create a SciView object for a brevis object"
  [s br-obj]
  (sciview/add-sphere s (physics/get-position br-obj) 1))

(defn init
  "Initialize a SciView, setup all current objects in scene for syncing."
  []
  (println :sciview-init)
  (let [s {:sciview (sciview/get-sciview)}
        br-objs (utils/all-objects)
        sv-objs (doall (map (partial create-sv-object s) br-objs))]
    (assoc s
      :br-sv-map (zipmap br-objs sv-objs))))

(defn remove-objects
  "Remove the sciview objects"
  [s objs]
  (doseq [obj objs]
    (sciview/remove-node (:sciview s) obj)))

(defn sync-objects
  "Sync all the br and sciview objects"
  [s br-objs]
  (doseq [br-obj br-objs]
    (let [^graphics.scenery.Node sv-obj (get (:br-sv-map s) br-obj)]
      (.setPosition sv-obj (physics/get-position br-obj))))); TODO get brevis using sciview Vector3

(defn display
  "Update and sync all objects to the active SciView."
  [s]
  (println :sciview-display)
  (let [br-objs (utils/all-objects)
        prev-br (set (keys (:br-sv-map s)))
        curr-br (set (map utils/get-uid br-objs))
        rem-objs (cset/difference prev-br curr-br)
        add-objs (cset/difference curr-br prev-br)]
    (merge (sync-objects s (remove-objects s rem-objs))
           (zipmap add-objs
                   (doall (map (partial create-sv-object s) add-objs))))))


(ns brevis.graphics.sciview
  (:require [clojure.set :as cset]
            [brevis.utils :as utils]
            [fun.imagej.sciview :as sciview]
            [brevis.physics.utils :as physics]
            [brevis.utils :as utils])
  (:import (sc.iview.vector ClearGLVector3)
           (cleargl GLVector)))

(defn create-sv-object
  "Create a SciView object for a brevis object"
  [s br-obj]
  (let [cp (physics/get-position (utils/get-object br-obj))
        c [(.x cp) (.y cp) (.z cp)]]
    (sciview/add-sphere (:sciview s) c (float 1))))

(defn init
  "Initialize a SciView, setup all current objects in scene for syncing."
  []
  ;(println :sciview-init)
  (let [s {:sciview (sciview/get-sciview)}
        br-objs (map utils/get-uid (utils/all-objects))
        sv-objs (doall (map (partial create-sv-object s) br-objs))]
    (assoc s
      :br-sv-map (zipmap br-objs sv-objs))))

(defn remove-objects
  "Remove the sciview objects"
  [s br-objs]
  (loop [s s
         br-objs br-objs]
    (if (empty? br-objs)
      s
      (let [br-obj (first br-objs)]
        (sciview/remove-node (:sciview s)
                             (get (:br-sv-map s) br-obj))
        (recur (assoc s
                 :br-sv-map (dissoc (:br-sv-map s) br-obj))
               (rest br-objs))))))

(defn sync-objects
  "Sync all the br and sciview objects"
  [s br-objs]
  (loop [br-sv-map {}
         br-objs br-objs]
    (if (empty? br-objs)
      br-sv-map
      (let [br-obj (first br-objs)
            ^graphics.scenery.Node sv-obj (get (:br-sv-map s) br-obj)
            br-pos (physics/get-position (utils/get-object br-obj))]
        (.setPosition sv-obj (GLVector. (float-array [(.x br-pos) (.y br-pos) (.z br-pos)]))); TODO get brevis using sciview Vector3
        (recur (assoc br-sv-map br-obj sv-obj)
               (rest br-objs))))))

(defn display
  "Update and sync all objects to the active SciView."
  [s]
  ;(println :sciview-display)
  (let [br-objs (map utils/get-uid (utils/all-objects))
        prev-br (set (keys (:br-sv-map s)))
        curr-br (set br-objs)
        del-objs (cset/difference prev-br curr-br)
        rem-objs (cset/intersection prev-br curr-br)
        add-objs (cset/difference curr-br prev-br)]
    ;(println (utils/get-time) :display :del-objs (count del-objs) :add-objs (count add-objs) :prev-objs (count prev-br) :curr-objs (count curr-br))
    (assoc s
      :br-sv-map (merge (sync-objects (remove-objects s del-objs)
                                      rem-objs)
                        (zipmap add-objs
                                (doall (map (partial create-sv-object s) add-objs)))))))


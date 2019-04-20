(ns us.brevis.graphics.sciview
  (:require [clojure.set :as cset]
            [fun.imagej.sciview :as sciview]
            [us.brevis.physics.utils :as physics]
            [us.brevis.shape.core :as shape]
            [us.brevis.utils :as utils])
  (:import (sc.iview.vector ClearGLVector3)
           (cleargl GLVector)
           (com.jogamp.opengl.math Quaternion)))

(defn create-sv-object
  "Create a SciView object for a brevis object"
  [s br-obj]
  (let [obj (utils/get-object br-obj)
        cp (physics/get-position obj)
        c (sc.iview.vector.FloatVector3. (.x cp) (.y cp) (.z cp))
        shp (shape/get-shape obj)
        shp-type (.getType shp)]
    (cond (= shp-type "sphere")
          (sciview/add-sphere (:sciview s) [(.x cp) (.y cp) (.z cp)] (float 5))
          (= shp-type "cone")
          (sciview/add-cone (:sciview s) c (float 5) 10)
          (= shp-type "cylinder")
          (sciview/add-cylinder (:sciview s) c (float 5) 10)
          (= shp-type "box")
          (sciview/add-box (:sciview s) c (float 5)))))

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
            br-rot (physics/get-rotation (utils/get-object br-obj))
            br-pos (physics/get-position (utils/get-object br-obj))
            br-vel (physics/get-velocity (utils/get-object br-obj))
            qrot (.setLookAt (Quaternion.)
                             (float-array [(.x br-vel) (.y br-vel) (.z br-vel)])
                             (float-array [0 0 1])
                             (float-array 3)
                             (float-array 3)
                             (float-array 3))];
        (.setPosition sv-obj (GLVector. (float-array [(.x br-pos) (.y br-pos) (.z br-pos)]))); TODO get brevis using sciview Vector3
        ;(.setRotation sv-obj (Quaternion. (.x br-rot) (.y br-rot) (.z br-rot) (.w br-rot))); TODO check the .w, but when i did it only reported 90, so i hard coded the radians value
        (.setRotation sv-obj qrot)
        ;(.setRotation sv-obj (Quaternion. (.x br-rot) (.y br-rot) (.z br-rot) (.w br-rot))); TODO check the .w, but when i did it only reported 90, so i hard coded the radians value
        (println :rotation (.x br-rot) (.y br-rot) (.z br-rot) (.w br-rot) :qrot qrot)
        (.setNeedsUpdate sv-obj true)
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


(ns us.brevis.graphics.sciview
  (:require [clojure.set :as cset]
            [fun.imagej.sciview :as sciview]
            [us.brevis.physics.utils :as physics]
            [us.brevis.shape.core :as shape]
            [us.brevis.utils :as utils]
            [us.brevis.vector :as v]
            [us.brevis.vector :as vector])
  (:import (sc.iview.vector ClearGLVector3 FloatVector3 Vector3)
           (cleargl GLVector)
           (com.jogamp.opengl.math Quaternion)
           (graphics.scenery Node Sphere Material)
           (sc.iview SciView)
           (org.joml Vector4f)))

(defn add-sphere
  "Add a sphere to a sciview instance"
  [^SciView sv ^Vector3 position radius]
  (let [^Node sphere (Sphere. radius 15)]
    (.setPosition sphere (ClearGLVector3/convert position))
    (.addNode sv sphere false)))
;(.addSphere sv position radius)))

(defn create-sv-object
  "Create a SciView object for a brevis object"
  [s br-obj]
  (let [obj (utils/get-object br-obj)
        cp (physics/get-position obj)
        c (FloatVector3. (.x cp) (.y cp) (.z cp))
        shp (shape/get-shape obj)
        shape-size (.getDimension shp); TODO finish this
        shp-type (.getType shp)]
    (cond (= shp-type "sphere")
          (add-sphere (:sciview s) (ClearGLVector3. (.x cp) (.y cp) (.z cp)) (float 5))
          ;(sciview/add-sphere (:sciview s) (ClearGLVector3. (.x cp) (.y cp) (.z cp)) (float 5))
          (= shp-type "cone")
          (sciview/add-cone (:sciview s) c (float (.x shape-size)) (.y shape-size))
          (= shp-type "cylinder")
          (sciview/add-cylinder (:sciview s) c (float (.x shape-size)) (.y shape-size))
          (= shp-type "box")
          (sciview/add-box (:sciview s) c (float (.x shape-size))))))

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
        (.deleteNode ^SciView (:sciview s)
                     ^Node (get (:br-sv-map s) br-obj)
                     false)
        (recur (assoc s
                 :br-sv-map (dissoc (:br-sv-map s) br-obj))
               (rest br-objs))))))

(defn sync-objects
  "Sync all the br and -sciview objects"
  [s br-objs]
  (loop [br-sv-map {}
         br-objs br-objs]
    (if (empty? br-objs)
      br-sv-map
      (let [br-obj (first br-objs)
            ^Node sv-obj (get (:br-sv-map s) br-obj)
            ;br-rot (physics/get-rotation (utils/get-object br-obj))
            br-pos (physics/get-position (utils/get-object br-obj))
            ;br-vel (physics/get-velocity (utils/get-object br-obj))
            col ^Vector4f (physics/get-color (utils/get-object br-obj))
            col ^GLVector (GLVector. (float-array [(.x col) (.y col) (.z col)]))]
            ;target-rot (.setFromAxes ^Quaternion (Quaternion.)
            ;                         (float-array [0 1 0])
            ;                         (float-array [0 0 1])
            ;                         (float-array [1 0 0]))]
            ;target-rot (.setFromAxes (Quaternion.)
            ;                         (float-array [0 0 1])
            ;                         (float-array [1 0 0])
            ;                         (float-array [0 1 0]))
            ;target-rot (.setIdentity  (Quaternion.))
            ;target (float-array 3)
            ;_ (.rotateVector target-rot
            ;                 target
            ;                 0
            ;                 (float-array (vector/vec3-to-seq (vector/normalize-vec3 br-vel)))
            ;                 0)] ;(vector/add br-pos br-vel)
            ;target (float-array (vector/vec3-to-seq (vector/normalize-vec3 br-vel)))]
            ;qrot (.setLookAt (Quaternion.)
            ;                 ;(float-array [(.x target) (.y target) (.z target)])
            ;                 target
            ;                 (float-array [0 0 1])
            ;                 ;(float-array [0 0 1])
            ;                 (float-array 3)
            ;                 (float-array 3)
            ;                 (float-array 3))]
            ;
        (.setPosition sv-obj ^GLVector (GLVector. (float-array [(.x br-pos) (.y br-pos) (.z br-pos)]))); TODO get brevis using sciview Vector3
        ;(.setRotation sv-obj (Quaternion. (.x br-rot) (.y br-rot) (.z br-rot) (.w br-rot))); TODO check the .w, but when i did it only reported 90, so i hard coded the radians value
        ;(.setRotation sv-obj qrot)
        (.setDiffuse ^Material (.getMaterial sv-obj) col)
        (.setAmbient ^Material (.getMaterial sv-obj) col)
        (.setSpecular ^Material (.getMaterial sv-obj) col)
        ;(.setRotation sv-obj test-quat)
        ;(.setRotation sv-obj (Quaternion. (.x br-rot) (.y br-rot) (.z br-rot) (.w br-rot))); TODO check the .w, but when i did it only reported 90, so i hard coded the radians value
        ;(println :p br-pos :v br-vel :r qrot)
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


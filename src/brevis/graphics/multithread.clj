(ns brevis.graphics.multithread
  (:require [brevis-utils.parameters :as parameters])
  (:use [brevis globals])
  (:import (java.util.concurrent TimeUnit)
           (org.lwjgl.opengl SharedDrawable)
           (java.util.concurrent.locks ReentrantLock)))          

(defn begin-with-graphics-thread
   "Everything that follows will have a GL context."
   []
   (when (parameters/get-param :gui)
     #_(:gui @brevis.globals/*gui-state*)
     (.tryLock ^ReentrantLock (:lock @brevis.globals/*graphics*) 250 ^TimeUnit TimeUnit/MILLISECONDS)
     (when-not (.isHeldByCurrentThread ^ReentrantLock (:lock @brevis.globals/*graphics*))
       (throw (Exception. "Can't acquire graphics lock")))
     (when-not (.isCurrent ^SharedDrawable (:drawable @brevis.globals/*graphics*))
       (.makeCurrent ^SharedDrawable (:drawable @brevis.globals/*graphics*)))))

(defn end-with-graphics-thread
  "Finish up with the graphics thread."
  []
  (when (parameters/get-param :gui) 
    (when (.isCurrent ^SharedDrawable (:drawable @brevis.globals/*graphics*))
          (.releaseContext ^SharedDrawable (:drawable @brevis.globals/*graphics*)))
    (.unlock ^ReentrantLock (:lock @brevis.globals/*graphics*))))

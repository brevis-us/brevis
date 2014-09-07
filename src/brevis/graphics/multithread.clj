(ns brevis.graphics.multithread
  (:use [brevis globals])
  (:import (java.util.concurrent TimeUnit)))          

(defn begin-with-graphics-thread
   "Everything that follows will have a GL context."
   []
   (when (:gui @brevis.globals/*gui-state*)
     (.tryLock (:lock @brevis.globals/*graphics*) 250 TimeUnit/MILLISECONDS)
     (when-not (.isHeldByCurrentThread (:lock @brevis.globals/*graphics*))
       (throw (Exception. "Can't acquire graphics lock")))
     (when-not (.isCurrent (:drawable @brevis.globals/*graphics*))
       (.makeCurrent (:drawable @brevis.globals/*graphics*)))))

(defn end-with-graphics-thread
  "Finish up with the graphics thread."
  []
  (when (:gui @brevis.globals/*gui-state*)
    (when (.isCurrent (:drawable @brevis.globals/*graphics*))
          (.releaseContext (:drawable @brevis.globals/*graphics*)))
    (.unlock (:lock @brevis.globals/*graphics*))))

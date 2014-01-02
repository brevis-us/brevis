(ns brevis.graphics.multithread
  (:use [brevis globals])
  (:import (java.util.concurrent TimeUnit)))          

#_(defmacro with-graphics-thread
   "Do stuff with the graphics thread context."
   [& body]
   (do
     (.tryLock (:lock @brevis.globals/*graphics*) 50 TimeUnit/MILLISECONDS)
     (when-not (.isCurrent (:drawable @brevis.globals/*graphics*))
       (.makeCurrent (:drawable @brevis.globals/*graphics*)))  
     `(let [result ~@body]
        (when (.isCurrent (:drawable @brevis.globals/*graphics*))
          (.releaseContext (:drawable @brevis.globals/*graphics*)))
        (.unlock (:lock @brevis.globals/*graphics*))
        result)))

;; Elegance at its worst
#_(defmacro with-graphics-thread
    "Do stuff with the graphics thread context."
    [& body]
    (.tryLock (:lock @brevis.globals/*graphics*) 50 TimeUnit/MILLISECONDS)
    (when-not (.isCurrent (:drawable @brevis.globals/*graphics*))
      (.makeCurrent (:drawable @brevis.globals/*graphics*)))
    #_(println body)
    #_(doseq [expression body] (println expression))
    (doseq [expression (butlast body)] (expression))
    (let [result (last body)]
      (when (.isCurrent (:drawable @brevis.globals/*graphics*))
        (.releaseContext (:drawable @brevis.globals/*graphics*)))
      (.unlock (:lock @brevis.globals/*graphics*))
      result))

(defn begin-with-graphics-thread
  "Everything that follows will have a GL context."
  []
  (.tryLock (:lock @brevis.globals/*graphics*) 50 TimeUnit/MILLISECONDS)
  (when-not (.isHeldByCurrentThread (:lock @brevis.globals/*graphics*))
    (throw (Exception. "Can't acquire graphics lock")))
  (when-not (.isCurrent (:drawable @brevis.globals/*graphics*))
    (.makeCurrent (:drawable @brevis.globals/*graphics*))))

(defn end-with-graphics-thread
  "Finish up with the graphics thread."
  []
  (when (.isCurrent (:drawable @brevis.globals/*graphics*))
        (.releaseContext (:drawable @brevis.globals/*graphics*)))
  (.unlock (:lock @brevis.globals/*graphics*)))

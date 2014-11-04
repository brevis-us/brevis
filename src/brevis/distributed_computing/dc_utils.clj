(ns brevis.distributed-computing.dc-utils  
   (:use [clojure.java.shell]
        [clojure.math.numeric-tower])
  (:require [me.raynes.conch :refer [programs with-programs let-programs]]
            [clojure.string :as string])
  (:import [java.io]))

(def debug false)
(programs ssh)

#_(defn local-command
   [command]
   (when debug (println "local-command:" command))
   #_(sh command)
   (.exec (. Runtime getRuntime) command))

(defn local-command
   [command]
   (when debug (println "local-command:" command))
   #_(sh command)
   (.exec (. Runtime getRuntime) command)
   #_(let [proc (.exec (. Runtime getRuntime) command)]
      (.waitFor proc)
      (println (.getInputStream proc))))

(defn remote-command
  [username server command]
  (let [comm #_(str "ssh " username "@" server " \\\"" command "\\\"")
        (str "ssh " username "@" server " \"" command "\"")]
    (when debug (println "remote-command:" comm))
    (ssh (str username "@" server) command)
    #_(local-command comm)))

(defn upload-files
  [username server source destination]
  ;; source: ../../../
  ;; destination: ~/
  (let [command (str "rsync -avzr " source " " username "@" server ":" destination)]
    (when debug (println "upload-files:" command))    
    (local-command command)))

(defn serialize-map 
  [m sep] 
  (str (clojure.string/join sep (map (fn [[k v]] (str k " " v)) m ))))

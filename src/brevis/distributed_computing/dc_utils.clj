#_"This file is part of brevis.                                                                                                                                                 
                                                                                                                                                                                     
    brevis is free software: you can redistribute it and/or modify                                                                                                           
    it under the terms of the GNU General Public License as published by                                                                                                             
    the Free Software Foundation, either version 3 of the License, or                                                                                                                
    (at your option) any later version.                                                                                                                                              
                                                                                                                                                                                     
    brevis is distributed in the hope that it will be useful,                                                                                                                
    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                                                                                   
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                                                                                    
    GNU General Public License for more details.                                                                                                                                     
                                                                                                                                                                                     
    You should have received a copy of the GNU General Public License                                                                                                                
    along with brevis.  If not, see <http://www.gnu.org/licenses/>.                                                                                                          
                                                                                                                                                                                     
Copyright 2012-2014 Kyle Harrington
"

(ns brevis.distributed-computing.dc-utils  
   (:use [clojure.java.shell]
        [clojure.math.numeric-tower])
  (:require [me.raynes.conch :refer [programs with-programs let-programs]]
            [clojure.string :as string])
  (:import [java.io]))

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

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

This namespace was first contributed by Alex Bardasu.
"

(ns brevis.distributed-computing.lsf
  (:use [clojure.java.shell])
  (:require [me.raynes.conch :refer [programs with-programs let-programs]])
  (:import [java.io]))

;; Function that takes a list of argmaps and generates and starts runs for the cluster
;; (ssh call)
;; Later: hook class that lets you listen to system signals -> dump to serialized files

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

(defn gen-config
  "Takes a set of params(argmap), a configFileName, and the output
parameters to be used in the output log, then generates the
appropriate configuration file to be passed to the hpc."
  [argmap configFileName namespace expName basedir profile]
  (let [out-str (str
                  "#!/bin/sh\n"
                  "source /etc/profile;\n"
                  "cd " basedir ";\n"
                  "lein " (if profile (str "with-profile " profile " ") "") 
                  "run -m "
                  namespace
                  #_expName
                  " "
                  (serialize-map argmap " ")
                  "\n"
                  #_" > "
                  ;expName "_" configFileName "_run$SGE_TASK_ID" ".log"
                  #_".log")]
    (when debug (println "gen-config:" configFileName out-str))    
    (spit configFileName out-str)))

(defn launch-config
  "Launches an experiment from the configuration file numruns times."
  [username server expName configFile numruns duration]
  (let [command (str "source /etc/profile; bsub -W " duration " -J " expName "[1-" (str numruns) "] sh " configFile)]
        #_(str "bsub " optArgs " -t 1-" (str numruns) " -N " expName " " configFile)
    (when debug (println "launch-config:" command))
    (remote-command username server command)))

(defn start-runs
  [argmaps namespace expName username server numruns source destination duration profile]
  (loop [conf 0
         argmaps argmaps]
    (when-not (empty? argmaps)
          (gen-config (first argmaps) (str "job_" expName "_" conf ".sh") namespace expName (str destination expName) profile)
          (recur (inc conf)
                 (rest argmaps))))
  (upload-files username server (str source "/") (str destination expName "/"))
  (println "Uploaded files.")
  (Thread/sleep 2)
  (println "Remotely configuring project.")
  (remote-command username server (str "cd " destination expName "; lein clean; lein compile;"))
  (println "Configuration complete.")
  (dotimes [i (count argmaps)]
    (upload-files username server (str "job_" expName "_" i ".sh") (str destination expName "/" (str "job_" expName "_" i ".sh")))
    (launch-config username server expName (str destination expName "/job_" expName "_" i ".sh") numruns duration)
    (Thread/sleep 0.1)
    #_(Thread/sleep 0.01))
  (println "All runs submitted."))

#_"This namespace was first contributed by Alex Bardasu.
"

(ns brevis.distributed-computing.rocks
  (:use [clojure.java.shell]
        [brevis.distributed-computing.dc-utils])
  (:require [me.raynes.conch :refer [programs with-programs let-programs]])
  (:import [java.io]))

;; Function that takes a list of argmaps and generates and starts runs for the cluster
;; (ssh call)
;; Later: hook class that lets you listen to system signals -> dump to serialized files

;(def debug false)

(defn gen-config
  ;; Takes a set of params(argmap), a configFileName,
  ;; and the output parameters to be used in the output log, then generates the
  ;; appropriate configuration file to be passed to the hpc
  [argmap configFileName namespace expName basedir]
  (let [out-str (str
                  "#!/bin/sh\n"
                  "cd " basedir ";\n"
                  "lein run -m "
                  namespace
                  #_expName
                  " "
                  (serialize-map argmap " ")
                  "\n"
                  #_" > "
                  ;expName "_" configFileName "_run$SGE_TASK_ID" ".log"
                  #_".log")]
    (when @debug-mode (println "gen-config:" configFileName out-str))    
    (spit configFileName out-str)))

(defn launch-config
  ;; Launches an experiment from the configuration file numruns times.
  [username server expName configFile numruns optArgs]
  (let [command (str "qsub " configFile " -t 1-" (str numruns) " -N " expName)];  used to include optArgs, removed because it returned {}
    (when @debug-mode (println "launch-config:" command))
    (remote-command username server command)))

(defn start-runs
  [argmaps namespace expName username server numruns source destination optArgs]
  (loop [conf 0
         argmaps argmaps]
    (when-not (empty? argmaps)
          (gen-config (first argmaps) (str "job_" expName "_" conf ".sh") namespace expName (str destination expName))
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
    (launch-config username server expName (str destination expName "/job_" expName "_" i ".sh") numruns optArgs)
    (Thread/sleep 0.1)
    #_(Thread/sleep 0.01))
  (println "All runs submitted."))

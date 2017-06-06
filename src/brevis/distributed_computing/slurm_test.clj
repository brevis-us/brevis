(ns brevis.distributed-computing.slurm-test
  (:use [clojure.test])
  (:require [brevis.distributed-computing.slurm :as slurm]
            [brevis.distributed-computing.dc-utils :as dc-utils]))

(defn -main [& args]
  (let [;; First put everything into a map
        argmap (apply hash-map
                      (mapcat #(vector (read-string (first %)) (second %) #_(read-string (second %)))
                              (partition 2 args)))
        ;; Then read-string on *some* args, but ignore others
        argmap (apply hash-map
                      (apply concat
                             (for [[k v] argmap]
                               [k (cond (= k :output-file) v
                                        :else (read-string v))])))]
    (println argmap)
    (spit (:output-file argmap)
          (with-out-str
            (println "Slurm test")
            (println argmap)))))

(defn cluster-launch-slurm
   []
   (let [experiment-name "slurm_test_001"
         argmaps [{:tag "testrun" 
                   :output-file "testfile.txt"}]]
     (dotimes [k (count argmaps)]
       (let [argmap (nth argmaps k)
             experiment-name (str experiment-name "_" k)]
         (slurm/start-runs
           [argmap]
           "brevis.distributed-computing.slurm-test"
           experiment-name
           "kharrington"
           "fortyfour.ibest.uidaho.edu"
           1
           "/Users/kharrington/git/brevis/"
           "brevis_dir/"
           "00:05:00")))))

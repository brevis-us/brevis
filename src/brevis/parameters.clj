(ns brevis.parameters
  (:use [brevis.random]))

(def params (atom {}))

(defn set-param
  "Set the value of a parameter."
  [param val]
  (swap! params assoc param val))

(defn get-param
  "Get the value of a parameter."
  [param]
  (get @params param))

#_(defn set-random-seed
   "Set the random seed."
   [random-seed]
   (set-param :random-seed
              (str "[" (random-seed-to-string random-seed) "]")))
   
(defn print-params
  "Print the current parameter map."
  ([]
    (print-params @params))
  ([ps]
    (doseq [[k v] ps]
      (cond (= k :random-seed)
            (println k (str "[" (random-seed-to-string v) "]"))
            :else
            (println k v)))))

(defn params-from-argseq
  "Load params from a sequence of arguments. Autoconverts strings, so this can be risky."
  [args]
  (let [;; First put everything into a map                                                                                                                                                                                                                                                                                 
        argmap (apply hash-map
                      (mapcat #(vector (read-string (first %)) (second %))
                              (partition 2 args)))
        ;; Then read-string on *some* args, but ignore others                                                                                                                                                                                                                                                              
        argmap (apply hash-map
                      (apply concat
                             (for [[k v] argmap]
                               [k (cond (= k :output-directory) v
                                        :else (read-string v))])))   
        random-seed (if (:random-seed argmap)
                      (byte-array (map byte (read-string (:random-seed argmap)))) 
                      (generate-random-seed))]
    (swap! params merge argmap)))

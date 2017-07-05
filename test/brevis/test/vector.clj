(ns brevis.test.vector  
  (:use [clojure.test])
  (:require [brevis.vector :as v]))

(deftest test-add-vec3
  (let [v1 (v/vec3 1 1 1)
        v2 (v/vec3 -1 -1 -1)]
    (is (zero? (v/length (v/add v1 v2))))))

(deftest test-add-vec4
  (let [v1 (v/vec4 1 1 1 1)
        v2 (v/vec4 -1 -1 -1 -1)]
    (is (zero? (v/length (v/add v1 v2))))))



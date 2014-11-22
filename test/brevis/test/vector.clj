(ns brevis.test.vector  
  (:use [clojure.test]
        [brevis vector]))

(deftest test-add-vec3
  (let [v1 (vec3 1 1 1)
        v2 (vec3 -1 -1 -1)]
    (is (zero? (length (add v1 v2))))))

(deftest test-add-vec4
  (let [v1 (vec4 1 1 1 1)
        v2 (vec4 -1 -1 -1 -1)]
    (is (zero? (length (add v1 v2))))))



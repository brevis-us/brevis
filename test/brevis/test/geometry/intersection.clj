(ns brevis.test.geometry.intersection
  (:use [clojure.test]
        [brevis.vector]
        [brevis.geometry intersection]))

(deftest test-cylinder-contains?-maker
  (let [cylinder-contains? (cylinder-contains?-maker (vec3 1 0 0) (vec3 10 0 0) 5 5)]
    (is (not (cylinder-contains? (vec3 0 0 0))))
    (is (cylinder-contains? (vec3 1 0 0)))
    (is (cylinder-contains? (vec3 10 0 0))) 
    (is (not (cylinder-contains? (vec3 11 0 0)))) 
    (is (cylinder-contains? (vec3 5 0 0))) 
    ))

(deftest test-sphere-contains?-maker
  (let [sphere-contains? (sphere-contains?-maker (vec3 0 0 0) 5)]
    (is (sphere-contains? (vec3 0 0 0)))
    (is (not (sphere-contains? (vec3 6 0 0))))
    (is (sphere-contains? (vec3 5 0 0))) 
    (is (not (sphere-contains? (vec3 0 0 6)))) 
    (is (sphere-contains? (vec3 0 0 5))) 
    ))


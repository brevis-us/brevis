(ns brevis.test.random
  (:use [clojure.test]
        [brevis random]))

(deftest test-seedstrings
  (let [seed (generate-random-seed)
        seed-str (seed-to-string seed)
        seed2 (string-to-seed seed-str)
        seed2-str (seed-to-string seed2)]
    (is (= seed-str seed2-str))))

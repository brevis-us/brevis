(ns brevis.test.parameters
  (:use [clojure.test]
        [brevis parameters]))

(deftest test-set-param
  (let [v 42]
    (set-param :test v)
    (is (= (get-param :test) v))))

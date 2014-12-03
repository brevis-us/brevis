(ns brevis.test.parameters
  (:use [clojure.test]
        [brevis parameters]))

(deftest test-set-param
  (let [v 42]
    (set-param :test v)
    (is (= (get-param :test) v))))

(deftest test-param-file-io
  (let [test-params {:integer-test 3
                     :double-test 13.0}; yeah string test i know i know
        ]
    (reset! params test-params)
    (write-params "test-params.clj")
    (read-params "test-params.clj")
    (is (= test-params @params))))

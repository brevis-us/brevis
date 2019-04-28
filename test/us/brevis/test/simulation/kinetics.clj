(ns us.brevis.test.simulation.kinetics
  (:use [clojure.test])
  (:require [us.brevis.core :as brevis]
            [us.brevis.utils :as utils]
            [us.brevis.vector :as v]
            [us.brevis.shape.cone :as cone]
            [us.brevis.physics.utils :as physics]
            [brevis-utils.parameters :as parameters]))

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (physics/init-world)
  (brevis/init-view)  
  
  (utils/set-dt 1)
  (parameters/set-param :gui false)

  (utils/add-object
    (physics/set-velocity
      (physics/move
        (physics/make-real {:type :test-bird})
                          ;:shape (cone/create-cone 10.2 1.5)})
        (v/vec3 0 0 0))
      (v/vec3 0 1 0)))
  (physics/enable-kinematics-update :test-bird)
  
  (utils/add-terminate-trigger 0))

(deftest test-single-object-velocity
  (brevis/start-nogui initialize-simulation)
  (is (reduce #(and %1 %2)
              (map (comp zero? -) 
                   (v/vec3-to-seq (physics/get-position (first (utils/all-objects))))
                   [0 1 0]))))

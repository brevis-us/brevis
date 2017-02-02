(ns brevis.test.simulation.kinetics
  (:use [clojure.test])
  (:require [brevis.core :as brevis]
            [brevis.utils :as utils]
            [brevis.vector :as v]
            [brevis.shape.cone :as cone]
            [brevis.physics.utils :as physics]
            [brevis.physics.space :as space]
            [brevis.parameters :as parameters]))
#_(:use [brevis.physics collision core space utils]
        [brevis.shape box sphere cone]
        [brevis core osd vector camera utils display image])

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (space/init-world)
  (brevis/init-view)  
  
  (utils/set-dt 1)
  (parameters/set-param :gui false)

  (utils/add-object
    (physics/set-velocity
      (space/move 
        (space/make-real {:type :test-bird})
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

(ns brevis.random
  (:require [clj-random.core :as random])
  (:use [brevis vector])
  (:import [java.security.SecureRandom]
           [ec.util.MersenneTwisterFast]))

(def lrand random/lrand)
(def lrand-int random/lrand-int)
(def lrand-nth random/lrand-nth)
(def lshuffle random/lshuffle)

(defn lrand-vec3
  "Return a random vec3."
  ([]
    (lrand-vec3 1 1 1))
  ([x y z]
    (vec3 (lrand x) (lrand y) (lrand z)))
  ([xmn xmx ymn ymx zmn zmx]
    (vec3 (lrand xmn xmx) (lrand ymn ymx) (lrand zmn zmx)))) 

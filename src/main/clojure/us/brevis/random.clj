(ns us.brevis.random
  (:require [clj-random.core :as random]
            [clojure.string :as string])
  (:use [us.brevis vector])
  (:import [java.security.SecureRandom]
           [ec.util.MersenneTwisterFast]))

#_(def #^:dynamic brevis-RNG random/*RNG*)

(def generate-random-seed
  random/generate-mersennetwister-seed)

(defn make-RNG
  [new-seed]
  (random/make-mersennetwister-rng new-seed))

(def random-seed-to-string random/seed-to-string); this binding will be deprecated
(def seed-to-string random/seed-to-string)
(def lrand random/lrand)
(def lrand-int random/lrand-int)
(def lrand-nth random/lrand-nth)
(def lshuffle random/lshuffle)
(def lrand-gaussian random/lrand-gaussian)

(defmacro with-rng
  "Use a specific RNG with all lrand calls within the body."
  [my-rng & body]
  `(binding [random/*RNG* ~my-rng]
     ~@body))

(defn string-to-seed
  "Convert a string to a random seed (a byte array)."
  [s]
  (byte-array (map #(java.lang.Byte. %)
                   (string/split s #" "))))

(defn lrand-vec3
  "Return a random vec3."
  ([]
    (lrand-vec3 1 1 1))
  ([x y z]
    (vec3 (lrand x) (lrand y) (lrand z)))
  ([xmn xmx ymn ymx zmn zmx]
    (vec3 (lrand xmn xmx) (lrand ymn ymx) (lrand zmn zmx)))) 

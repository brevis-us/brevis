(ns brevis.random
  (:import [java.security.SecureRandom]))

(def #^:dynamic *RNG-num-seed-bytes* 20)
(def #^:dynamic *RNG* (java.security.SecureRandom. (.generateSeed (java.security.SecureRandom.) *RNG-num-seed-bytes*)))

(defn make-rng-seed
  "Generate a random number seed."
  []
  (.generateSeed *RNG* *RNG-num-seed-bytes*))

(defn seed-to-string
  "Convert a byte-array seed into a string."
  [seed]
  (reduce #(str %1 " " %2)
          (map str (seq seed))))      

(defn make-rng
  "Make a random number generator with a given seed (if none specified, one will be generated)."
  ([]
    (make-rng (make-rng-seed)))
  ([seed]
    (java.security.SecureRandom. seed)))

(defn lrand
  "A local random double in [0,n], where n is 1 if no arguments are specified."
  ([]
    (lrand 1.0))
  ([n]
    (.nextDouble *RNG*)))

(defn lrand-int
  "A local random int (actually a long) in [0,n]."
  [n]
  (.nextLong *RNG*))

(defn lrand-gaussian
  "A local random gaussian."
  []
  (.nextGaussian *RNG*))

(defn lrand-nth
  "Return a random element of a sequence."
  [coll]
  (nth coll (lrand-int (count coll))))

(defn lrand-shuffle
  "Return a random permutation of coll (Adapted from clojure.core)"
  {:static true}
  [^java.util.Collection coll]
  (let [al (java.util.ArrayList. coll)]
    (java.util.Collections/shuffle al *RNG*)
    (clojure.lang.RT/vector (.toArray al))))

(defmacro with-rng
  [my-rng & body]
  `(binding [*RNG* my-rng#]
     ~@body))


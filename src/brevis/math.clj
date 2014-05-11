(ns brevis.math)

;; the following functions from ztellman's cantor (see github)
(defn radians
 "Transforms degrees to radians."
 [x]
 (* (/ Math/PI 180.0) (double x)))

(defn degrees
 "Transforms radians to degrees."
 [x]
 (* (/ 180.0 Math/PI) (double x)))

(defn polar-to-cartesian
  "Create polar coordinate."
  ([theta phi] (polar-to-cartesian theta phi 1))
  ([theta phi r] 
   (let [theta (radians theta)
         phi (radians (- 90 phi))
         ts (Math/sin theta)
         tc (Math/cos theta)
         ps (Math/sin phi)
         pc (Math/cos phi)]
     [(* r ps tc) (* r pc) (* r ps ts)])))

;; from rosettacode.org
(defn std-dev [samples]
  (let [n (count samples)
	mean (/ (reduce + samples) n)
	intermediate (map #(Math/pow (- %1 mean) 2) samples)]
    (Math/sqrt 
     (/ (reduce + intermediate) n))))   

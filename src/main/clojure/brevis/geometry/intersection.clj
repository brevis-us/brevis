(ns brevis.geometry.intersection
  (:import [org.lwjgl.util.vector Vector3f Vector4f])
  (:require [brevis.vector :as v])
  #_(:use [brevis vector utils]        
         [brevis.shape box sphere cone cylinder]))

(defn cylinder-contains?-maker
  "Make a cylinder-contains? function for a specific cylinder. 
     Takes 2 points and 2 radii. Currently only uses the first radius"
  [^Vector3f A ^Vector3f B
   Ra Rb]
  (let [^Vector3f AB (v/sub-vec3 B A)
        ^Vector3f BA (v/sub-vec3 A B)
        ab (v/length-vec3 AB)
        ab2 (java.lang.Math/pow ab 2)
        r Ra
        r2 (* r r)]           
    (fn [point]
      (let [AP (v/sub-vec3 point A)
            BP (v/sub-vec3 point B)]
        (when (and (>= (v/dot-vec3 AP AB) 0)
                   (>= (v/dot-vec3 BP BA) 0))
          (let [ap-dot-ab (v/dot-vec3 AP AB)
                t (/ ap-dot-ab ab2)]
            (let [^Vector3f Pcore (v/add-vec3 A (v/mul-vec3 AB t))
                  ^Vector3f Dcore (v/sub-vec3 point Pcore)
                  d-core (v/length-vec3 Dcore)]
              (< d-core r))))))))

(defn sphere-contains?-maker
  "Make a sphere-contains? function for a specific sphere.
   Takes 1 point and 1 radius."
  [^Vector3f A r]
  (fn [point]
    (<= (v/length-vec3 (v/sub-vec3 point A)) r)))



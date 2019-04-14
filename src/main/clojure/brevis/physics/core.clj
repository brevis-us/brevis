(ns brevis.physics.core
  (:import (brevis Engine)))

;; ## Globals

;; Hash map keyed on pairs of types with values of the respective collision function.
;; 
;; Keys are of the form [:ball :floor]
;;
;; Collision functions take [collider collidee] and return [collider collidee].
;;
;; Both can be modified; however, two independent collisions are actually computed [a b] and [b a]."
(def #^:dynamic *collision-handlers*
  (atom {}))

(def #^:dynamic *collisions* (atom #{}))

;; Hash map keyed on type with values of the respective update function.
;;
;; An update function should take 3 arguments:
;;
;; [object dt neighbors] and return an updated version of object                                                                                                 
;; given that dt amount of time has passed.
(def #^:dynamic *update-handlers*
  (atom {}))

#_(def simulation-boundary
  (box3 (vec3 100 100 100)
        (vec3 -100 -100 -100)))

(def #^:dynamic *dt* (atom 1))
(def #^:dynamic *neighborhood-radius* (atom 8.0))
(def #^:dynamic *physics* (atom nil))
(def #^:dynamic *objects* (atom {}))
(def #^:dynamic *added-objects* (atom {}))
(def #^:dynamic *deleted-objects* (atom #{}))
(def collisions-enabled (atom true))
(def neighborhoods-enabled (atom true))
(def #^:dynamic *brevis-parallel* (atom true))

#_(def #^:dynamic *java-engine* (atom nil))

(def #^:dynamic *java-engine* (atom (Engine.)))

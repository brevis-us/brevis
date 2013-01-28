(ns brevis.shape.core)

(defn resize-shape
  "Change the dimension of an object's shape."
  [obj new-dim]
  (assoc-in obj [:shape :dim] new-dim))

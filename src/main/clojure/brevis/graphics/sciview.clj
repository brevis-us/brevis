(ns brevis.graphics.sciview)


(defn init
  "Initialize a SciView, setup all current objects in scene for syncing."
  []
  (println :sciview-init)
  {})

(defn display
  "Update and sync all objects to the active SciView."
  [s]
  (println :sciview-sync)
  s)

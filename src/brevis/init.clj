(ns brevis.init
  (:import (brevis BrInput SystemUtils Natives)))

(Natives/extractNativeLibs (SystemUtils/getPlatform) "LWGL")

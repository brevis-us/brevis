(ns brevis.init
  (:import (brevis BrInput SystemUtils Natives)))

(when-not (System/getProperty "brevisHeadless")
  (Natives/extractNativeLibs (SystemUtils/getPlatform) "LWGL"))

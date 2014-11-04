(ns brevis.display
  (:use [brevis globals image]
        [brevis.graphics multithread])
  (:require [clojure.java.io :as io])
  (:import (java.awt AWTException Robot Rectangle Toolkit)
           (java.awt.geom AffineTransform)
           (java.awt.image AffineTransformOp BufferedImage)
           (java.nio ByteBuffer)
           (java.io File IOException)
           (javax.imageio ImageIO)
           (brevis.graphics Basic3D BrMesh)))


(defn screenshot-image
   "Take a screenshot and return an image (BufferedImage for now)."
   []
   (begin-with-graphics-thread)
   (let [img (Basic3D/screenshotImage)]     
     (end-with-graphics-thread)
     img))

(defn screenshot
    "Take a screenshot."
    [filename]
    (write-image filename (screenshot-image)))

(defn regen-mesh
  "Regenerate a mesh's openGL list."
  [msh]
  (begin-with-graphics-thread)
  (.opengldrawtolist ^BrMesh msh)
  (end-with-graphics-thread))



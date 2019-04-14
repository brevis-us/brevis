(ns brevis.image  
  (:require [clojure.java.io :as io])
  (:import [java.awt.image BufferedImage]
           [javax.imageio ImageIO]))

;; Namespace for image IO, manipulation

(defn read-image
  "Read an image."
  [filename]
  (ImageIO/read (io/file filename)))

(defn create-image
  "Create an image."
  ([width height]
    (BufferedImage. width height 4))
  ([width height num-colors]
    (BufferedImage. width height (if (= num-colors 3)
                                   BufferedImage/TYPE_INT_RGB
                                   BufferedImage/TYPE_INT_ARGB))));; some other things might be expecting RGBA
  

(defn vector-to-image
  "Create an image from a vector."
  [width height num-colors v]
  (when (= (* width height num-colors) (count v));; must have same number of elements
    (let [img (create-image width height num-colors)]
      (.setRGB img 0 0 width height (int-array v) 0 width)      
      img)))

(defn write-image
  "Write an image to file."
  [filename img]
  (let [format-type "png"]
    (ImageIO/write img format-type (io/file filename))))

(defn image-difference
  "Take the difference (absolute value of subtraction) of 2 images. Assumes equal size (uses first images dimensions)"
  [^BufferedImage img-1 ^BufferedImage img-2]
  (let [width (.getWidth img-1)
        height (.getHeight img-1)
        out-img (BufferedImage. width height (.getType img-1))]    
    (doall (for [x (range width)
                 y (range height)]
             (let [rgb1 (.getRGB img-1 x y)
                   a1 (bit-and (bit-shift-right rgb1 24)  0xff)
                   r1 (bit-and (bit-shift-right rgb1 16)  0xff)
                   g1 (bit-and (bit-shift-right rgb1 8)  0xff)
                   b1 (bit-and rgb1 0xff)
                   rgb2 (.getRGB img-2 x y)
                   a2 (bit-and (bit-shift-right rgb2 24)  0xff)
                   r2 (bit-and (bit-shift-right rgb2 16)  0xff)
                   g2 (bit-and (bit-shift-right rgb2 8)  0xff)
                   b2 (bit-and rgb2 0xff)
                   diff-a (Math/abs (- a1 a2))
                   diff-r (Math/abs (- r1 r2))
                   diff-g (Math/abs (- g1 g2))
                   diff-b (Math/abs (- b1 g2))
                   diff-rgb (bit-or (bit-shift-left diff-a 24)
                                    (bit-shift-left diff-r 16)
                                    (bit-shift-left diff-g 8)
                                    diff-b)]
               (.setRGB out-img x y diff-rgb))))
    out-img))

(defn sum-image-difference
  "Take the difference (absolute value of subtraction) of 2 images. Assumes equal size (uses first images dimensions). sums over all channels"
  [^BufferedImage img-1 ^BufferedImage img-2]
  (let [width (.getWidth img-1)
        height (.getHeight img-1)]
    (reduce + 
            (flatten (doall (for [x (range width)
                                  y (range height)]
                              (let [rgb1 (.getRGB img-1 x y)
                                    a1 (bit-and (bit-shift-right rgb1 24)  0xff)
                                    r1 (bit-and (bit-shift-right rgb1 16)  0xff)
                                    g1 (bit-and (bit-shift-right rgb1 8)  0xff)
                                    b1 (bit-and rgb1 0xff)
                                    rgb2 (.getRGB img-2 x y)
                                    a2 (bit-and (bit-shift-right rgb2 24)  0xff)
                                    r2 (bit-and (bit-shift-right rgb2 16)  0xff)
                                    g2 (bit-and (bit-shift-right rgb2 8)  0xff)
                                    b2 (bit-and rgb2 0xff)
                                    diff-a (Math/abs (- a1 a2))
                                    diff-r (Math/abs (- r1 r2))
                                    diff-g (Math/abs (- g1 g2))
                                    diff-b (Math/abs (- b1 g2))]
                                [diff-a diff-r diff-g diff-b])))))))  

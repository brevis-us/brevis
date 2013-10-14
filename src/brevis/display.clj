#_"This file is part of brevis.                                                                                                                                                 
                                                                                                                                                                                     
    brevis is free software: you can redistribute it and/or modify                                                                                                           
    it under the terms of the GNU General Public License as published by                                                                                                             
    the Free Software Foundation, either version 3 of the License, or                                                                                                                
    (at your option) any later version.                                                                                                                                              
                                                                                                                                                                                     
    brevis is distributed in the hope that it will be useful,                                                                                                                
    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                                                                                   
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                                                                                    
    GNU General Public License for more details.                                                                                                                                     
                                                                                                                                                                                     
    You should have received a copy of the GNU General Public License                                                                                                                
    along with brevis.  If not, see <http://www.gnu.org/licenses/>.                                                                                                          
                                                                                                                                                                                     
Copyright 2012, 2013 Kyle Harrington"     

(ns brevis.display
  (:use [brevis globals])
  (:require [penumbra.opengl.frame-buffer :as fb])
  (:import (java.awt AWTException Robot Rectangle Toolkit)
           (java.awt.geom AffineTransform)
           (java.awt.image AffineTransformOp BufferedImage)
           (java.nio ByteBuffer)
           (java.io File IOException)
           (javax.imageio ImageIO)
           (org.lwjgl.opengl Display GL11)
           (org.lwjgl BufferUtils)))

#_(defn screenshot
  "Take a screenshot."
  [filename state]
  (let [pixels (int-array (* (:window-width state) (:window-height state))); Creating an rbg array of total pixels
        fb (ByteBuffer/allocateDirect (* 3 (:window-width state) (:window-height state))); allocate space for RBG pixels
        img-type (second (re-find (re-matcher #"\.(\w+)$" filename)))]        
    (fb/gl-read-pixels (int 0) (int 0) (int (:window-width state)) (int (:window-height state)) :rgb :unsigned-byte fb)
    (let [imageIn (BufferedImage. (:window-width state) (:window-height state) BufferedImage/TYPE_INT_RGB)]
      (dotimes [i (alength pixels)]
        (let [bidx (* 3 i)]
          (aset pixels i 
                (+ (bit-shift-left (.get fb bidx) 16) 
                   (bit-shift-left (.get fb (inc bidx)) 8) 
                   (bit-shift-left (.get fb (inc (inc bidx))) 0))))) 
     (.setRGB imageIn 0 0 (:window-width state) (:window-height state) pixels 0 (:window-width state)); Allocate colored pixel to buffered Image
     (let [at (AffineTransform/getScaleInstance 1 -1)]; Creating the transformation direction (horizontal)
       (.translate at 0 (- (.getHeight imageIn)))
       (let [opRotated (AffineTransformOp. at AffineTransformOp/TYPE_BILINEAR);//Applying transformation
             imageOut (. opRotated filter imageIn nil)
             file (File. filename)]
         ;; probably should use try-catch
         (ImageIO/write imageOut img-type file))))))

(defn screenshot
  "Take a screenshot."
  [filename]
  (let [pixels (int-array (* (:window-width @*gui-state*) (:window-height @*gui-state*))); Creating an rbg array of total pixels
        fb (ByteBuffer/allocateDirect (* 3 (:window-width @*gui-state*) (:window-height @*gui-state*))); allocate space for RBG pixels
        img-type (second (re-find (re-matcher #"\.(\w+)$" filename)))]        
    (fb/gl-read-pixels (int 0) (int 0) (int (:window-width @*gui-state*)) (int (:window-height @*gui-state*)) :rgb :unsigned-byte fb)
    (let [imageIn (BufferedImage. (:window-width @*gui-state*) (:window-height @*gui-state*) BufferedImage/TYPE_INT_RGB)]
      (dotimes [i (alength pixels)]
        (let [bidx (* 3 i)]
          (aset pixels i 
                (+ (bit-shift-left (.get fb bidx) 16) 
                   (bit-shift-left (.get fb (inc bidx)) 8) 
                   (bit-shift-left (.get fb (inc (inc bidx))) 0))))) 
     (.setRGB imageIn 0 0 (:window-width @*gui-state*) (:window-height @*gui-state*) pixels 0 (:window-width @*gui-state*)); Allocate colored pixel to buffered Image
     (let [at (AffineTransform/getScaleInstance 1 -1)]; Creating the transformation direction (horizontal)
       (.translate at 0 (- (.getHeight imageIn)))
       (let [opRotated (AffineTransformOp. at AffineTransformOp/TYPE_BILINEAR);//Applying transformation
             imageOut (. opRotated filter imageIn nil)
             file (File. filename)]
         ;; probably should use try-catch
         (ImageIO/write imageOut img-type file))))))

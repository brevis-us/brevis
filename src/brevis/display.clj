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
    (write-image (str filename ".png") (screenshot-image)))

(defn regen-mesh
  "Regenerate a mesh's openGL list."
  [msh]
  (begin-with-graphics-thread)
  (.opengldrawtolist ^BrMesh msh)
  (end-with-graphics-thread))



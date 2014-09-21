(ns brevis.graphics.visual-overlays
  (:use [brevis globals utils input osd display vector]
        [brevis.graphics basic-3D multithread]
        [brevis.physics core space utils]
        [brevis.shape core box sphere cone])       
  (:require [clojure.math.numeric-tower :as math])
  (:import (brevis.graphics Basic3D) 
           (brevis BrInput SystemUtils Natives)
           (java.awt AWTException Robot Rectangle Toolkit)
           (java.awt.geom AffineTransform)
           (java.awt.image AffineTransformOp BufferedImage)
           (java.nio ByteBuffer)
           (java.io File IOException)
           (java.util.concurrent.locks ReentrantLock)
           (java.util.concurrent TimeUnit)
           (javax.imageio ImageIO)
           (org.lwjgl.opengl SharedDrawable)
           (org.lwjgl.input Keyboard Mouse)
           (org.lwjgl.opengl Display GL11 DisplayMode GLContext)
           (org.lwjgl BufferUtils LWJGLException Sys)
           ))

; Visual overlays
(def visual-overlays (atom []))

(defn add-line
  "Create a 3D line. source/destination may be vec3 or uuid"
  [source destination]
  (let [line {:type :line
              :visible? true
              :source source
              :destination destination}]
    (swap! visual-overlays conj line)))

(defn draw-line
  "Draw a line described as a visual overlay map."
  [vo]
  (let [source (if (vec3? (:source vo))
                 (:source vo)
                 (get-position (get-object (:source vo))))
        destination (if (vec3? (:destination vo))
                      (:destination vo)
                      (get-position (get-object (:destination vo))))
        color (if (:color vo) (:color vo) (vec4 1 1 1 1))]
    (Basic3D/drawLine source destination color)))

(defn draw-visual-overlay
  "draw a visual overlay."
  [vo]
  (cond (= (:type vo) :line)
        (draw-line vo)))

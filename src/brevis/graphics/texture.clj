(ns brevis.graphics.texture
  (:import (org.lwjgl.util.vector Vector3f Vector4f))
  (:import java.lang.Math)  
  (:import (brevis Engine BrPhysics BrObject))
  (:use [brevis vector math utils]
        [brevis.shape core box]        
        [brevis.graphics multithread]
        [brevis.physics core]))
  

(defn get-texture
  "Return the texture of an object."
  [^BrObject obj]
  (.getTexture obj))

(defn set-texture
  "set the texture of an object."
  [obj new-tex]  
  (begin-with-graphics-thread)
  (when (:gui @brevis.globals/*gui-state*);; for now textures shouldn't matter without graphics, they may eventually though  
    (.setTexture obj new-tex) )
  (end-with-graphics-thread)
  obj)  

(defn set-texture-image
  "set the texture of an object to a bufferedimage."
  [obj new-tex-img]
  (begin-with-graphics-thread)
  ;when? see set-texture
  (.setTextureImage ^brevis.BrObject obj new-tex-img)
  (end-with-graphics-thread)
  obj)



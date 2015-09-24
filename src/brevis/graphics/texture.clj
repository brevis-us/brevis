(ns brevis.graphics.texture
  (:import (org.lwjgl.util.vector Vector3f Vector4f))  
  (:import java.lang.Math)  
  (:import (brevis Engine BrPhysics BrObject))
  (:use [brevis vector math utils]
        [brevis.shape core box]        
        [brevis.graphics multithread]
        [brevis.physics core])
  #_(:import (org.lwjgl.opengl Display GL11 DisplayMode GLContext)))
  

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

(defn get-next-pow2
  "Get the next power of 2 above the number."
  [v]
  (loop [pv 2]
    (if (< v pv) (recur (* pv 2)) pv)))

(defn load-texture-from-imp
 "Load an ImagePlus into a texture."
 [texture-id imp]
 (begin-with-graphics-thread)
 (org.lwjgl.opengl.GL11/glEnable org.lwjgl.opengl.GL11/GL_TEXTURE_2D)
 (let [^org.newdawn.slick.opengl.TextureImpl timp (org.newdawn.slick.opengl.TextureImpl. "NORESOURCE" org.lwjgl.opengl.GL11/GL_TEXTURE_2D texture-id)		
       ^org.newdawn.slick.opengl.ImageIOImageData iiid (org.newdawn.slick.opengl.ImageIOImageData.)				
       ^java.awt.image.BufferedImage buffered-image (.getBufferedImage imp)
       ^ByteBuffer buffer (.imageToByteBuffer iiid buffered-image false false nil)        
       width (.getWidth buffered-image)
       height (.getHeight buffered-image)       
       hasAlpha (.hasAlpha (.getColorModel buffered-image))
       texWidth (int (Math/pow 2 (Math/ceil (/ (Math/log width) (Math/log 2)))))
       texHeight  (int (Math/pow 2 (Math/ceil (/ (Math/log height) (Math/log 2)))))
       srcPixelFormat (if hasAlpha org.lwjgl.opengl.GL11/GL_RGBA org.lwjgl.opengl.GL11/GL_RGB)
       componentCount (if hasAlpha 4 3)        
       minFilter 0
       magFilter 0
       ^java.nio.IntBuffer temp (org.lwjgl.BufferUtils/createIntBuffer 16)]        
   (.setAlpha timp hasAlpha )
   (.setHeight timp height )
   (.setWidth timp width )
   (.setTextureID timp texture-id )
   (.setTextureHeight timp texHeight )
   (.setTextureWidth timp texWidth )                       
   (.setTextureData timp srcPixelFormat componentCount minFilter magFilter buffer)                
   (org.lwjgl.opengl.GL13/glActiveTexture org.lwjgl.opengl.GL13/GL_TEXTURE0 )
   (org.lwjgl.opengl.GL11/glBindTexture org.lwjgl.opengl.GL11/GL_TEXTURE_2D texture-id)         
   (org.lwjgl.opengl.GL11/glPixelStorei org.lwjgl.opengl.GL11/GL_UNPACK_ALIGNMENT 1)
                        
   (org.lwjgl.opengl.GL11/glGetInteger org.lwjgl.opengl.GL11/GL_MAX_TEXTURE_SIZE temp)
    
   (if (or (> texWidth (.get temp 0))
           (> texHeight (.get temp 0)))
     (do
       (end-with-graphics-thread)
       (println "Trying to allocate too large of a texture for current hardware. Whoopsies!"))
     (do
       (org.lwjgl.opengl.GL11/glTexImage2D org.lwjgl.opengl.GL11/GL_TEXTURE_2D 
                          0
                          ;private int dstPixelFormat = SGL.GL_RGBA8; this is hard coded now
                          org.newdawn.slick.opengl.renderer.SGL/GL_RGBA8
                          (get-next-pow2 width)
                          (get-next-pow2 height)
                          0 
                          srcPixelFormat 
                          org.lwjgl.opengl.GL11/GL_UNSIGNED_BYTE 
                          buffer)   
       (org.lwjgl.opengl.GL30/glGenerateMipmap org.lwjgl.opengl.GL11/GL_TEXTURE_2D)                
       (org.lwjgl.opengl.GL11/glTexParameteri org.lwjgl.opengl.GL11/GL_TEXTURE_2D org.lwjgl.opengl.GL11/GL_TEXTURE_WRAP_S org.lwjgl.opengl.GL11/GL_REPEAT);
       (org.lwjgl.opengl.GL11/glTexParameteri org.lwjgl.opengl.GL11/GL_TEXTURE_2D org.lwjgl.opengl.GL11/GL_TEXTURE_WRAP_T org.lwjgl.opengl.GL11/GL_REPEAT);
        
       (org.lwjgl.opengl.GL11/glTexParameteri org.lwjgl.opengl.GL11/GL_TEXTURE_2D org.lwjgl.opengl.GL11/GL_TEXTURE_MAG_FILTER org.lwjgl.opengl.GL11/GL_NEAREST);
       (org.lwjgl.opengl.GL11/glTexParameteri org.lwjgl.opengl.GL11/GL_TEXTURE_2D org.lwjgl.opengl.GL11/GL_TEXTURE_MIN_FILTER org.lwjgl.opengl.GL11/GL_LINEAR_MIPMAP_LINEAR);
        
       (end-with-graphics-thread)))))

(ns brevis.video
  #_(:require [me.raynes.conch :refer [programs with-programs let-programs]])
  (:use [fun.imagej imp]
        [fun.imagej.video ffmpeg]))

#_(programs ffmpeg)

#_(let-programs [ffmpeg "/usr/local/bin/ffmpeg"]
  (defn make-mp4
    "Make a mp4 video from a directory."
    [directory input-pattern out-filename]
    (ffmpeg "-i" (str directory "/" input-pattern) "-c:v" "libx264" "-r" "30" "-pix_fmt" "yuv420p" out-filename))

  #_(defn make-avi
     "Make a avi video from a directory."
     [directory input-pattern out-filename]
     (ffmpeg "-r" "30" "-i" (str directory "/" input-pattern) out-filename)))

(defn make-avi
  "Make an avi video from a directory of images."
  [directory out-filename]
  (let [imp (open-image-sequence directory)]
    (save-z-as-avi imp out-filename)))

(defn get-chunk-name
  "Return the name of a video chunk."
  [image-directory time is-shutdown?]
  (str (gensym)))

#_(defn make-screenshot-agent
   "A screenshot agent manages screenshot recording, which may involve complex video editing/splicing/etc... Must be triggered to start after this function is called.
It should eventually be possible to bind this to a specific BrCamera."
   [& args]
   (let [argmap (apply hash-map args)]
     (when true; maybe we will add a flag for this
       (add-shutdown-hook (fn [] 
                            (make-avi (:screenshot-directory argmap) (get-chunk-name argmap (get-time) true)))))
     (assoc argmap
            :initialize-fn (fn []
                             (add-global-update-handler
                               (inc (get-max-global-update-handler-id))
                               (fn [] 
                                 (when ((:record-time? argmap) (get-time))
                                   (screenshot))
                                 (when ((:end-of-chunk? argmap) (get-time))
                                   (make-avi (:screenshot-directory argmap) (get-chunk-name argmap (get-time) false)))))))))

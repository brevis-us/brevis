(ns brevis.video
  (:require [me.raynes.conch :refer [programs with-programs let-programs]]))

#_(programs ffmpeg)

(let-programs [ffmpeg "/usr/local/bin/ffmpeg"]
  (defn make-mp4
    "Make a mp4 video from a directory."
    [directory input-pattern out-filename]
    (ffmpeg "-i" (str directory "/" input-pattern) "-c:v" "libx264" "-r" "30" "-pix_fmt" "yuv420p" out-filename))

  (defn make-avi
    "Make a avi video from a directory."
    [directory input-pattern out-filename]
    (ffmpeg "-r" "30" "-i" (str directory "/" input-pattern) out-filename)))


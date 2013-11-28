(ns brevis.ui.profile
  (:require [clojure.java.io :as io]
            [leiningen.core.project :as project])
  (:use [clojure.pprint]))

(def brevis-directory "/Users/kyle/.brevis/")
(def current-profile-filename (atom (str brevis-directory "default")))

(def current-profile (atom {}))

(def default-profile
  {:author "Kyle Harrington"
   :current-project "/Users/kyle/git/brevis"
   :current-filename "/Users/kyle/git/brevis/src/brevis/example/swarm.clj"
   :projects []})

(defn read-profile
  "Read a brevis profile."
  []
  (read-string (slurp @current-profile-filename)))

(defn write-profile
  "Write the profile."
  [p]
  (when-not (.exists (io/file brevis-directory))
    (io/make-parents @current-profile-filename))
  (spit @current-profile-filename
        (with-out-str (pprint p))))

(defn read-project
  "Read a project from its directory."
  [d]
  (let [filename (str d "/project.clj")]
    (when (.exists (io/file filename))
      (let [p (project/read filename)]
        {:project-file filename
         :group (:group p)
         :name (:name p)}
        #_(project/read filename)))))

(defn import-project-directory
  "Look through a directory (similar to Eclipse's workspace) for all projects."
  [d]
  (let [dir (filter #(.isDirectory %) (.listFiles (io/file d)))]
    #_(doseq [f dir] (println (.toString f)))
    #_(map #(.toString %) dir)
    (filter identity (map read-project dir))
    ))

(reset! current-profile (assoc (read-profile)
                               :projects (import-project-directory "/Users/kyle/git")))
(write-profile @current-profile)

#_(import-project-directory "/Users/kyle/git")

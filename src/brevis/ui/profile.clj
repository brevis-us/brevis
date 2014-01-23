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

(ns brevis.ui.profile
  (:import [java.io File])
  (:require [clojure.java.io :as io]
            [leiningen.core.project :as project])
  (:use [clojure.pprint]))

(def brevis-directory (str (io/file (System/getProperty "user.home")) File/separator ".brevis"))
(def current-profile-filename (atom (str brevis-directory File/separator "default")))

(def current-profile (atom {}))

(def default-profile
  {:author "Brevis h4x3r"
   :current-project (str (io/file (System/getProperty "user.home")) File/separator "git" File/separator "brevis")
   :current-filename (str (io/file (System/getProperty "user.home")) File/separator "git" File/separator "brevis" File/separator "src"File/separator "brevis" File/separator "example" File/separator "swarm.clj")
   :projects []})

(defn read-profile
  "Read a brevis profile."
  []
  (if (.exists (io/file @current-profile-filename))    
    (read-string (slurp @current-profile-filename))
    default-profile))

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
  (let [filename (str d File/separator "project.clj")]
    (when (.exists (io/file filename))
      (let [p (project/read filename)]
        {:project-file filename
         :directory (if (string? d) d (.toString d))
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
                               :projects (import-project-directory (io/file (System/getProperty "user.home") "git") #_"/Users/kyle/git")))
(write-profile @current-profile)

#_(import-project-directory "/Users/kyle/git")

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

(ns brevis.shape.cone
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [brevis.shape.core]
        [cantor])) 

(defn create-cone
  "Create a cone object."
  ([]
     (create-cone 1 1))
  ([length base]
     {:dim (vec3 (Math/max length base) (Math/max length base) (Math/max length base))
      :type :cone
      }))

(defn gen-cone-faces
  [length lod]
  (let [interval (/ 360 lod)
        base (range 0 361 interval)]
    (for [theta (rest base)]
      (let [p1 (Math/sin (- theta interval)) 
            p2 (Math/cos (- theta interval))]
        [(vec3 0 0 length) (vec3 p1 p2 0) (vec3 p2 p1 0)]))))

#_(defonce sphere-vertices (gen-sphere-vertices 50))

(defn init-cone
  []
  (def cone-mesh
    (define-display-list :triangles (gen-cone-faces 1 5))))

;; figure out the vertices as we go
#_(defn draw-sphere
  []
  (doseq [arcs (partition 2 1 sphere-vertices)]
    (with-enabled :auto-normal
      (draw-quad-strip
        (doseq [[a b] (map list (first arcs) (second arcs))]
          (vertex a) (vertex b))))))

(defn draw-cone
  []
  (cone-mesh))


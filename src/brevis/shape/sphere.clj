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

(ns brevis.shape.sphere
  (:import [brevis BrShape])
  (:use [penumbra opengl compute]
        [penumbra.opengl core]
        [brevis.shape.core])) 

#_(defn create-sphere
  "Create a sphere object."
  ([]
     (create-sphere 1))
  ([radius]
     {:dim (vec3 radius radius radius)
      :type :sphere
      }))

(defn create-sphere
  "Create a sphere object."
  ([]
     (create-sphere 1))
  ([radius]
    (BrShape/createSphere radius)))

;; currently mostly taken from ztellman's penumbra

#_(defn gen-sphere-vertices
  [lod]
  (for [theta (range 0 361 (/ 360 lod))]
    (for [phi (range -90 91 (/ 180 (/ lod 2)))]
      (cartesian (polar3 theta phi)))))

#_(defonce sphere-vertices (gen-sphere-vertices 50))

#_(defn init-sphere
  []
  (def sphere-mesh 
       (create-display-list (doseq [arcs (partition 2 1 sphere-vertices)]                              
                              (draw-quad-strip
                                (doseq [[a b] (map list (first arcs) (second arcs))]
                                  (normal (div a (length a)))
                                  (vertex a) (vertex b)))))))

;; figure out the vertices as we go
#_(defn draw-sphere
  []
  (doseq [arcs (partition 2 1 sphere-vertices)]
    (with-enabled :auto-normal
      (draw-quad-strip
        (doseq [[a b] (map list (first arcs) (second arcs))]
          (vertex a) (vertex b))))))

#_(defn draw-sphere
  []
  (sphere-mesh))


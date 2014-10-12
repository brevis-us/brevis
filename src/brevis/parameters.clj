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
                                                                                                                                                                                     
Copyright 2012-2014 Kyle Harrington
"

(ns brevis.parameters)

(def params (atom {}))

(defn set-param
  "Set the value of a parameter."
  [param val]
  (swap! params assoc param val))

#_(defn set-random-seed
   "Set the random seed."
   [random-seed]
   (set-param :random-seed
              (str "[" (random-seed-to-string random-seed) "]")))
   
(defn print-params
  "Print the current parameter map."
  ([]
    (print-params @params))
  ([ps]
    (doseq [[k v] ps]
      (cond (= k :random-seed)
            (println k (str "[" (random-seed-to-string v) "]"))
            :else
            (println k v)))))

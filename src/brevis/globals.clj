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

(ns brevis.globals
  (:import [brevis.graphics BrCamera]))

(def enable-display-text (atom true))

(def default-gui-state {:rotate-mode :none :translate-mode :none
                        ;:camera (BrCamera. 300 300 -50)
                        :camera (BrCamera. 300 300 -50 90 -90 0 60 (/ 4 3) 0.1 4000)
                        :rot-x 90 :rot-y -90 :rot-z -45
                        :shift-x 300 :shift-y 300 :shift-z -50;-30                                   
                        :last-report-time 0 :simulation-time 0})
  
(def #^:dynamic *gui-state* (atom default-gui-state))
(def #^:dynamic *gui-message-board* (atom (sorted-map))) 
(def #^:dynamic *app-thread* (atom nil))
(def #^:dynamic *screenshot-filename* (atom nil))
(def #^:dynamic *simulation-state* (atom {}))

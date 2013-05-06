(ns brevis.example.tutorial.shape
  (:import java.lang.Math)  
  (:use [brevis.graphics.basic-3D]
        [brevis.physics collision core space utils]
        [brevis.shape box sphere cone]
        [brevis.core]
        [cantor]))    

;; ## Tutorial: shape
;; 
;; Use this tutorial to learn how to quickly draw shapes, change their colors, and move shapes around the world.
;; 

(defn shape-position
  "Return the shape of the position for a given time."
  [time num-shapes shape-num]
  #_(println time num-shapes shape-num)
  (vec3  (* 50 (cos (+ (* 2 Math/PI (/ shape-num num-shapes) time))))
         10
         (* 50 (sin (+ (* 2 Math/PI (/ shape-num num-shapes) time))))))

(defn make-shapes
  []
  (let [shapes [(create-sphere 5) (create-box 5 5 5)]
        ]
    (for [num (range (count shapes))]
      (let [position (shape-position (get-time) (count shapes) num)]            
        (move (make-real {:type :demoshape
                          :color [(- 1 (/ (inc num))) (/ (inc num)) 0]
                          :shape (nth shapes num)
                          :shape-number num})
              position)))))

(defn update-demoshape
  "Update a demoshape."
  [demoshape dt objects]
  #_(println (:type demoshape) demoshape)
  (let [new-position (shape-position (get-time) (count objects) (inc (:shape-number demoshape)))]
    #_(println (get-position demoshape) new-position)
    (assoc (move demoshape new-position)
           :color [0 (- 1 (:shape-number demoshape)) (:shape-number demoshape) 1] 
           #_(if (< (first (:color demoshape)) 0.05)
                    [(rand) (rand) (rand)]
                    (into [] (map #(- % 0.01) (:color demoshape)))))))           
      
(add-update-handler :demoshape update-demoshape); This tells the simulator how to update these objects

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ## brevis control code

(defn initialize-simulation
  "This is the function where you add everything to the world."
  []  
  (init-world);; Make the world 
  (set-dt 0.01)
  (let [shapes (make-shapes)];; Make our object
    (doseq [shape shapes]
      (add-object shape))));; Add the object to the simulation

;; This function is invoked by the command-line
(defn -main [& args]
  (start-gui initialize-simulation))

;; Start zee macheen
(-main)

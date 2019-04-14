(ns brevis.shape.icosahedron
  (:import [brevis BrShape])
  (:require [brevis-utils.parameters :as parameters])
  (:use [brevis.shape.core]
        [brevis vector])) 

(defn create-icosahedron
  "Create a sphere object."
  ([]
     (create-icosahedron 1))
  ([radius]
    (let [icos (BrShape/createIcosahedron radius (parameters/get-param :gui) )]
      (.setDimension ^BrShape icos (vec3 radius radius radius) (parameters/get-param :gui) )
      icos)))

#_(def icos (create-icosahedron))

#_(def icosahedron-vertices
   (let [fa (.trimeshVertices (.getMesh icos))]
     (zipmap (range)
             (map #(apply vec3 %) (partition 3 (into [] fa))))))
  
#_(print "{")
#_(doseq [[k v] icosahedron-vertices]
   (println k 
            "(vec3" (.x v) (.y v) (.z v) "),"))
#_(print "}")

(def icosahedron-vertices 
  {0 (vec3 0.850651 0.0 -0.525731 ),
   1 (vec3 0.850651 0.0 0.5257311 ),
   2 (vec3 -0.850651 0.0 0.5257311 ),
   3 (vec3 -0.850651 0.0 -0.525731 ),
   4 (vec3 0.0 -0.525731 0.850651 ),
   5 (vec3 0.0 0.5257311 0.850651 ),
   6 (vec3 0.0 0.5257311 -0.850651 ),
   7 (vec3 0.0 -0.525731 -0.850651 ),
   8 (vec3 -0.525731 -0.850651 0.0 ),
   9 (vec3 0.5257311 -0.850651 0.0 ),
   10 (vec3 0.5257311 0.850651 0.0 ),
   11 (vec3 -0.525731 0.850651 0.0 )})

#_(def icosahedron-faces
   (let [inds (.trimeshIndices (.getMesh icos))]
     (zipmap (range)
             (map #(into [] %) (partition 3 (into [] inds))))))

#_(print "{")
#_(doseq [[k v] icosahedron-faces]
   (println k 
            (str v ",")))
#_(print "}")

(def icosahedron-faces 
  {0 [1 9 0],
	1 [0 10 1],
	2 [0 7 6],
	3 [0 6 10],
	4 [0 9 7],
	5 [4 1 5],
	6 [9 1 4],
	7 [1 10 5],
	8 [3 8 2],
	9 [2 11 3],
	10 [4 5 2],
	11 [2 8 4],
	12 [5 11 2],
	13 [6 7 3],
	14 [3 11 6],
	15 [3 7 8],
	16 [4 8 9],
	17 [5 10 11],
	18 [6 11 10],
	19 [7 9 8]})

#_(def icosahedron-centers {})

#_(def icosahedron-centers
   (zipmap (keys icosahedron-faces)
           (map #(div (add (add (icosahedron-vertices (first %))
                                (icosahedron-vertices (second %)))
                           (icosahedron-vertices (last %))) 3)
                (vals icosahedron-faces))))

#_(print "{")
#_(doseq [[k v] icosahedron-centers]
   (println k 
            "(vec3" (.x v) (.y v) (.z v) "),"))
#_(print "}")

(def icosahedron-centers 
  {0 (vec3 0.7423444 -0.28355035 1.9868216E-8 ),
   1 (vec3 0.7423444 0.28355035 1.9868216E-8 ),
   2 (vec3 0.28355035 1.9868216E-8 -0.7423444 ),
   3 (vec3 0.45879406 0.45879406 -0.45879406 ),
   4 (vec3 0.45879406 -0.45879406 -0.45879406 ),
   5 (vec3 0.28355035 1.9868216E-8 0.7423444 ),
   6 (vec3 0.45879406 -0.45879406 0.45879406 ),
   7 (vec3 0.45879406 0.45879406 0.45879406 ),
   8 (vec3 -0.7423444 -0.28355035 1.9868216E-8 ),
   9 (vec3 -0.7423444 0.28355035 1.9868216E-8 ),
   10 (vec3 -0.28355035 1.9868216E-8 0.7423444 ),
   11 (vec3 -0.45879406 -0.45879406 0.45879406 ),
   12 (vec3 -0.45879406 0.45879406 0.45879406 ),
   13 (vec3 -0.28355035 1.9868216E-8 -0.7423444 ),
   14 (vec3 -0.45879406 0.45879406 -0.45879406 ),
   15 (vec3 -0.45879406 -0.45879406 -0.45879406 ),
   16 (vec3 1.9868216E-8 -0.7423444 0.28355035 ),
   17 (vec3 1.9868216E-8 0.7423444 0.28355035 ),
   18 (vec3 1.9868216E-8 0.7423444 -0.28355035 ),
   19 (vec3 1.9868216E-8 -0.7423444 -0.28355035 )})

#_(def icosahedron-neighbors
   (let [c1 (first (vals icosahedron-centers))
         min-dist (apply min 
                         (map #(length (sub c1 %)) (rest (vals icosahedron-centers))))]
     (apply hash-map 
            (apply concat
            (for [[cent-id cent-v] icosahedron-centers]
              [cent-id
               (into [] 
                     (filter identity
                             (for [[othr-id othr-v] icosahedron-centers]
                               (when (and (not= cent-id othr-id);; not self
                                          (< (- (length (sub cent-v othr-v)) min-dist) 0.01));; little fudge factor for matching
                                 #_(println (length (sub cent-v othr-v)))
                                 othr-id))))])))))


#_(print "{")
#_(doseq [[k v] icosahedron-neighbors]
   (println k 
            (str v ",")))
#_(print "}")

(def icosahedron-neighbors 
  {0 [1 4 6],
   1 [0 3 7],
   2 [3 4 13],
   3 [1 2 18],
   4 [0 2 19],
   5 [6 7 10],
   6 [0 5 16],
   7 [1 5 17],
   8 [9 11 15],
   9 [8 12 14],
   10 [5 11 12],
   11 [8 10 16],
   12 [9 10 17],
   13 [2 14 15],
   14 [9 13 18],
   15 [8 13 19],
   16 [6 11 19],
   17 [7 12 18],
   18 [3 14 17],
   19 [4 15 16]})


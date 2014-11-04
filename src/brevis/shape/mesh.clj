(ns brevis.shape.mesh
  (:import [brevis BrShape]
           [brevis.graphics BrMesh])
  (:use [brevis.vector]
        [brevis.shape.core]
        [brevis.graphics multithread]))        

(defn create-mesh
  "Create a mesh object."
  ([filename is-resource?]
    (create-mesh filename is-resource? (vec3 1 1 1)))
  ([filename is-resource? scale]
    (begin-with-graphics-thread)
    (let [result (BrShape/createMeshFromFile filename is-resource? (:gui @brevis.globals/*gui-state*) scale)]
      (end-with-graphics-thread)
      result)))

(defn shape-from-mesh
  "Create a mesh object."
  [mesh]
  #_(println mesh)
  (BrShape/createMeshFromBrMesh mesh)
  #_(BrShape. filename)
  #_(BrShape/loadMesh (BrShape.) filename))

(defn num-vertices
  "Return the number of vertices in a mesh."
  [mesh]
  (.numVertices ^BrMesh mesh))

(defn get-vertex
  "Return a vertex on a mesh."
  [mesh idx]
  (.getVertex ^BrMesh mesh ^int idx))

(defn set-vertex
  "Return a vertex on a mesh."
  [mesh idx v]
  (.setVertex ^BrMesh mesh ^int idx ^floats v))

(defn num-faces
  "Return the number of faces in a mesh."
  [mesh]
  (.numFaces ^BrMesh mesh))

(defn get-face
  "Return a face on a mesh."
  [mesh idx]
  (.getFace ^BrMesh mesh ^int idx))

(ns us.brevis.shape.mesh
  (:require [brevis-utils.parameters :as parameters])
  (:import [us.brevis BrShape]
           [us.brevis.graphics BrMesh])
  (:use [us.brevis.vector]
        [us.brevis.shape.core]))

(defn create-mesh
  "Create a mesh object."
  ([filename is-resource?]
   (create-mesh filename is-resource? (vec3 1 1 1)))
  ([filename is-resource? scale]
   (let [result (BrShape/createMeshFromFile filename is-resource? (parameters/get-param :gui) scale)]
      result)))

(defn shape-from-mesh
  "Create a mesh object."
  [^us.brevis.graphics.BrMesh mesh]
  #_(println mesh)
  (BrShape/createMeshFromBrMesh mesh)
  #_(BrShape. filename)
  #_(BrShape/loadMesh (BrShape.) filename))

(defn num-vertices
  "Return the number of vertices in a mesh."
  [^us.brevis.graphics.BrMesh mesh]
  (.numVertices ^BrMesh mesh))

(defn get-vertex
  "Return a vertex on a mesh."
  [^us.brevis.graphics.BrMesh mesh idx]
  (.getVertex ^BrMesh mesh ^int idx))

(defn set-vertex
  "Return a vertex on a mesh."
  [^us.brevis.graphics.BrMesh mesh idx v]
  (.setVertex ^BrMesh mesh ^int idx ^floats v))

(defn num-faces
  "Return the number of faces in a mesh."
  [^us.brevis.graphics.BrMesh mesh]
  (.numFaces ^BrMesh mesh))

(defn get-face
  "Return a face on a mesh."
  [^us.brevis.graphics.BrMesh mesh idx]
  (.getFace ^BrMesh mesh ^int idx))

(defn get-face-normal
  "Return the normal for a face."
  [^us.brevis.graphics.BrMesh mesh idx]
  (apply vec3 (.getFaceNormal ^BrMesh mesh ^int idx)))

(defn get-face-center
  "Return the center point of a face."
  [^us.brevis.graphics.BrMesh mesh idx]
  (let [face (map dec (get-face mesh idx))
        verts (map (comp (partial apply vec3) (partial get-vertex mesh)) face)]
    (mul-vec3 (apply add-vec3 verts) (/ 3))))

(defn create-mesh-from-triangles
  "Create a mesh from a list of triangles, sequences of 3 vertices describing each triangle."
  [triangles]
  (let [result (us.brevis.BrShape/createMeshFromTriangles triangles)]
    result))

(defn bounding-box
  "Return the bounding box of a mesh as 2 corners."
  [^us.brevis.graphics.BrMesh mesh]
  {:max (vec3 (.rightpoint mesh) (.toppoint mesh) (.nearpoint mesh))
   :min (vec3 (.leftpoint mesh) (.bottompoint mesh) (.farpoint mesh))})

(defn rescale-mesh
  "Rescale a mesh."
  [^us.brevis.graphics.BrMesh mesh w h d]
  (.rescaleMesh mesh w h d false))

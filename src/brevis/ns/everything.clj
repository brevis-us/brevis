(ns brevis.ns.everything)

(defn immigrate
 "Create a public var in this namespace for each public var in the
 namespaces named by ns-names. The created vars have the same name, value
 and metadata as the original except that their :ns metadata value is this
 namespace."
 [& ns-names]
 (doseq [ns ns-names]
   (doseq [[sym var] (ns-publics ns)]
     (let [sym (with-meta sym (assoc (meta var) :orig-ns ns))]
       (if (.isBound var)
         (intern *ns* sym (var-get var))
         (intern *ns* sym))))))

(defn require-and-immigrate
  "Require a bunch of namespaces then immigrate them."
  [& ns-names]
  (doseq [ns ns-names]
    (require ns))
  (apply immigrate ns-names)) 

(require-and-immigrate
  'brevis.camera 'brevis.image 'brevis.math 'brevis.input 'brevis.parameters 'brevis.plot 'brevis.random 'brevis.utils 'brevis.vector 'brevis.video 'brevis.core 'brevis.display
  'brevis.distributed-computing.dc-utils
  'brevis.graphics.multithread 'brevis.graphics.texture 'brevis.graphics.basic-3D
  'brevis.physics.collision 'brevis.physics.space 'brevis.physics.utils
  'brevis.shape.box 'brevis.shape.cone 'brevis.shape.core 'brevis.shape.cylinder 'brevis.shape.mesh 'brevis.shape.sphere)

; Should change this to require into a conventional shorthand: v/vec3 etc..

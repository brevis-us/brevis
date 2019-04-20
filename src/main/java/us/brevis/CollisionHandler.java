package us.brevis;

import java.io.IOException;
import java.io.Serializable;

public class CollisionHandler implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -5029373761039142328L;

    public clojure.lang.PersistentVector collide( Engine engine, BrObject subj, BrObject othr, Double dt) {
        clojure.lang.PersistentVector v = clojure.lang.PersistentVector.create( subj, othr );
        return v;
    }
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
         out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
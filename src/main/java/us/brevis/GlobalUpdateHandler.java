package us.brevis;

import java.io.IOException;
import java.io.Serializable;

public class GlobalUpdateHandler implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 5310171212429743564L;
    public Long priority = (long) 0;
    public void update( Engine engine ) {
    }
    public Long getPriority () {
        return priority;
    }
    public void setPriority( Long priority2 ) {
        priority = priority2;
    }
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
         out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
package us.brevis;

import java.io.IOException;
import java.io.Serializable;

public class UpdateHandler implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 7301251223848548305L;

    public BrObject update( Engine engine, Long uid, Double dt ) {
        BrObject obj = engine.objects.get( uid );
        return obj;
    }
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
         out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}
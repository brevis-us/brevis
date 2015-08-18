package brevis.math;

import java.util.List;

import org.ojalgo.access.Access2D.Builder;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.BasicMatrix.Factory;

public class MatrixUtils {
	static public BasicMatrix makeMatrix2D( long r, long c ) {
		final Factory<PrimitiveMatrix> tmpFactory = PrimitiveMatrix.FACTORY;
				
		return tmpFactory.makeZero(500, 500);				
	}

	static public BasicMatrix identity( long r, long c ) {
		final Factory<PrimitiveMatrix> tmpFactory = PrimitiveMatrix.FACTORY;

        return tmpFactory.makeEye( r, c );
	}
	
	static public BasicMatrix collectionToMatrix( int r, int c, List<Object> coll ) {
		final Factory<PrimitiveMatrix> tmpFactory = PrimitiveMatrix.FACTORY;

		final Builder<PrimitiveMatrix> tmpBuilder = tmpFactory.getBuilder(r, c);
        for (int j = 0; j < c; j++) {
        	//List<Number> row = (List<Number>) coll.get(j);
            for (int i = 0; i < r; i++) {
                //tmpBuilder.set(i, j, row.get(i));
            	tmpBuilder.set(i, j, (Number) coll.get((int) (j*r + i)));
            }
        }
        return tmpBuilder.build();
	}
	
}

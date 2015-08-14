package brevis.math;

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
		
}

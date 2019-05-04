
package us.brevis;

import org.joml.Vector3f;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import sc.iview.vector.JOMLVector3;
import sc.iview.vector.Vector3;

public class Utils {
	public static Vector3 DVector3CToVector3(DVector3C odev ) {
		return new JOMLVector3( (float)odev.get0(), (float)odev.get1(), (float)odev.get2() );
	}
	
	public static DVector3 Vector3ToDVector3( Vector3 javav ) {
		return new DVector3( javav.xf(), javav.yf(), javav.zf() );
	}
}

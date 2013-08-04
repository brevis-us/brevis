package brevis;

import javax.vecmath.Vector3d;

import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.OdeHelper;

public class BrShape {
	public enum BrShapeType {
		BOX, SPHERE, CONE, MESH
	};
	
	public BrShapeType type;
	public Vector3d dim;
	
	BrShape( BrShapeType t, Vector3d d ) {
		//type = BrShapeType.SPHERE;
		//dim = new Vector3d(1,1,1);
		type = t;
		dim = d;
	}
	
	public void draw() {
		
	}
	
	/*
	 * Return a mass that is appropriate for this object and its dimensions
	 */
	public DMass createMass( double density ) {
		DMass m = OdeHelper.createMass();
		if( type == BrShapeType.BOX ) {
			m.setBox(density, dim.x, dim.y, dim.z );
		} else if( type == BrShapeType.SPHERE ) {
			m.setSphere( density, dim.x );
		} else if( type == BrShapeType.CONE ) {
			m.setSphere(density, dim.x);
		}
		return m;
	}
	
	public DGeom createGeom( DSpace space ) {
		return OdeHelper.createSphere( space, 1 );
	}
	
	public Vector3d getDimension() {
		return dim;
	}
	
	public static BrShape createSphere( double r ) {
		return ( new BrShape( BrShapeType.SPHERE, new Vector3d( r, r, r ) ) );
	}
	
	public static BrShape createBox( double x, double y, double z ) {
		return ( new BrShape( BrShapeType.BOX, new Vector3d( x, y, z ) ) );
	}
	
	public static BrShape createCone( double length, double base ) {
		return ( new BrShape( BrShapeType.CONE, new Vector3d( length, base, 10 )));	// last element of vector is # of sides
	}
}

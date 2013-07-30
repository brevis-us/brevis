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
	
	BrShape() {
		type = BrShapeType.SPHERE;
	}
	
	public void draw() {
		
	}
	
	/*
	 * Return a mass that is appropriate for this object and its dimensions
	 */
	public DMass createMass( double density ) {
		DMass m = OdeHelper.createMass();
		m.setBox(density, 1, 1, 1);
		return m;
	}
	
	public DGeom createGeom( DSpace space ) {
		return OdeHelper.createSphere( space, 1 );
	}
	
	public Vector3d getDimension() {
		Vector3d v = new Vector3d(1,1,1);
		return v;
	}
}

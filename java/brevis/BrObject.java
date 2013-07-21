package brevis;

import java.util.Vector;

import javax.vecmath.Vector3d;

public class BrObject {
	public Long uid;
	public String type;
	public Vector3d acceleration;
	public Vector3d velocity;
	public Vector3d position;
	public double density = 1;
	public BrShape shape;
	
	public Vector<Long> nbrs;
	
	public double distanceTo( BrObject other ) {
		Vector3d delta = (Vector3d) position.clone();
		delta.sub( other.position );
		return delta.length();
	}
}

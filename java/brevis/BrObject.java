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
	
	BrObject() {
		uid = (long)-1;
		type = "Unassigned";
		acceleration = new Vector3d( 0, 0, 0 );
		velocity = new Vector3d( 0, 0, 0 );
		position = new Vector3d( 0, 0, 0 );
		shape = new BrShape();
	}
	
	public double distanceTo( BrObject other ) {
		Vector3d delta = (Vector3d) position.clone();
		delta.sub( other.position );
		return delta.length();
	}
	
	public void setUID( Long UID ) {
		uid = UID;
	}
	
	public void setType( String newType ) {
		type = newType;
	}
	
	public Vector<Long> getNeighbors() {
		return nbrs;
	}
	
	public void clearNeighbors() {
		nbrs.clear();
	}
	
	public void addNeighbor( Long UID ) {
		nbrs.add( UID );
	}
}

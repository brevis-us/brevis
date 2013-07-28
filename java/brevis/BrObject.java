package brevis;

import java.util.HashMap;
import java.util.Vector;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.OdeHelper;

public class BrObject {
	public Long uid;
	public String type;
	public Vector3d acceleration;
	public Vector3d velocity;
	public Vector3d position;
	public double density = 1;
	public BrShape shape;
	public DMass mass;
	public Vector4d rotation;
	
	public Vector<Long> nbrs;
	
	// Physics
	public DBody body;
	public DGeom geom;
	
	public BrObject() {
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
	
	public Long getUID( ) {
		return uid;	
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
	
	public Vector3d getPosition() {
		return position;
	}
	
	public Vector3d getVelocity() {
		return velocity;
	}
	
	public Vector3d getAcceleration() {
		return acceleration;
	}
	
	public void setAcceleration( Vector3d v ) {
		acceleration = v;
	}
	
	public void setVelocity( Vector3d v ) {
		velocity = v;
	}
	
	public void setPosition( Vector3d v ) {
		position = v;
	}
	
	public DBody getBody( ) {
		return body;
	}
	
	public void setBody( DBody b ) {
		body = b;
	}
	
	public DGeom getGeom() {
		return geom;
	}
	
	public void setGeom( DGeom g ) {
		geom = g;
	}
		
	public BrShape getShape( ) {
		return shape;
	}
	
	public void setShape( BrShape s ) {
		shape = s;
	}
	
	public void makeReal( Engine e ) {
		mass = shape.createMass( density );
		
		body = OdeHelper.createBody( e.getWorld() );
		body.setMass( mass );
		HashMap<String,Object> bodymap = new HashMap<String,Object>();
		bodymap.put( "uid", uid );
		bodymap.put( "type", type );
		body.setData( bodymap );
		
		geom = shape.createGeom( e.physics.getSpace() );
		geom.setBody( body );
		geom.setOffsetWorldPosition( position.x, position.y, position.z );		
	}
}

/*
 * brevis is free software: you can redistribute it and/or modify                                                                                                           
    it under the terms of the GNU General Public License as published by                                                                                                             
    the Free Software Foundation, either version 3 of the License, or                                                                                                                
    (at your option) any later version.                                                                                                                                              
                                                                                                                                                                                     
    brevis is distributed in the hope that it will be useful,                                                                                                                
    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                                                                                   
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                                                                                    
    GNU General Public License for more details.                                                                                                                                     
                                                                                                                                                                                     
    You should have received a copy of the GNU General Public License                                                                                                                
    along with brevis.  If not, see <http://www.gnu.org/licenses/>.                                                                                                          
                                                                                                                                                                                     
	Copyright 2012, 2013 Kyle Harrington
 */

package brevis;

import java.util.HashMap;
import java.util.Vector;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.OdeHelper;

import brevis.Utils;

//public class BrObject {
public class BrObject implements clojure.lang.IRecord {
	public Long uid;
	public String type;
	public Vector3d acceleration;
	public Vector3d velocity;
	public Vector3d position;
	public double density = 1;
	public BrShape shape;
	public DMass mass;
	public Vector4d rotation;
	public Vector4d color;
	
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
		shape = BrShape.createSphere( 1 );
		color = new Vector4d( 1, 1, 1, 1 );
		rotation = new Vector4d( 1, 0, 0, 0 );
	}
	
	public double distanceTo( BrObject other ) {
		/*Vector3d delta = (Vector3d) position.clone();
		delta.sub( other.position );
		System.out.println( "distanceTo " + position + " " + other.position + " " + delta );*/
		Vector3d delta = getPosition();
		delta.sub( other.getPosition() );		
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
		//return position;
		return brevis.Utils.DVector3CToVector3d( body.getPosition() );
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
		//position = v;
		body.setPosition( brevis.Utils.Vector3dToDVector3( v ) );
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
	
	public void setColor( Vector4d c ) {
		color = c;
	}
	
	public Vector4d getColor() {
		return color;
	}
	
	public Vector3d getDimension() {
		return shape.getDimension();
	}
	
	public Vector4d getRotation() {
		return rotation;
	}
	
	public DMass getMass() {
		return mass;
	}
	
	public double getDoubleMass() {
		return mass.getMass();	
	}
	
}

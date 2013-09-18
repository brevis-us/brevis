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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ClasspathLocation;
import org.newdawn.slick.util.ResourceLoader;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.OdeHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import clojure.lang.*;
import brevis.Utils;

//public class BrObject {
//public class BrObject implements clojure.lang.IRecord {
public class BrObject implements clojure.lang.IPersistentMap {
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
	//public BufferedImage texture;	
	public Texture texture;	
	public Object data;
	
	public HashMap<Object,Object> myMap;
	
	public Vector<Long> nbrs;
	protected int texId = -1;
	
	public boolean drawable = true;
	
	// Physics
	public DBody body;
	public DGeom geom;
	
	public String toString() {
		String s = "#BrObject{ :UID " + uid + ", :type " + type + ", :acceleration " + acceleration +
				", :velocity " + velocity + ", :position " + position + ", :density " + density +
				", :rotation " + rotation + ", : color " + color + ", :shape " + shape +
				"}";		
		return s;
	}
	
	public BrObject() {
		uid = (long)-1;
		type = "Unassigned";
		acceleration = new Vector3d( 0, 0, 0 );
		velocity = new Vector3d( 0, 0, 0 );
		position = new Vector3d( 0, 0, 0 );
		shape = BrShape.createSphere( 1 );
		color = new Vector4d( 1, 1, 1, 1 );
		rotation = new Vector4d( 1, 0, 0, 0 );
		data = null;
		myMap = new HashMap<Object,Object>();
		texture = null;
	}
	
	public boolean isDrawable() {
		return drawable;
	}
	
	public BrObject assoc(Object key, Object val) {
		myMap.put(key, val);
		return this;
	}

	public BrObject assocEx(Object key, Object val) {
		// no clue if this is supposed to behave differently from assoc
		myMap.put(key, val);
		return this;
	}

	public BrObject without(Object key) {
		myMap.remove(key);
		return this;
	}
	
	// Should add this to a destructor
	//GL11.glDeleteTextures( texId );
	
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
	
	public String getType() {
		return type;
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
		return brevis.Utils.DVector3CToVector3d( body.getLinearVel() );
		//return velocity;
	}
	
	public Vector3d getForce() {
		return brevis.Utils.DVector3CToVector3d( body.getForce() );
		//return velocity;
	}
	
	public Vector3d getAcceleration() {
		return acceleration;
	}
	
	public void setAcceleration( Vector3d v ) {
		acceleration = v;
	}
	
	public void setVelocity( Vector3d v ) {
		//velocity = v;
		body.setLinearVel( brevis.Utils.Vector3dToDVector3( v ) );
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
	
	/*public BufferedImage getTexture() {
		return texture;
	}*/
	
	/*public void setTexture(BufferedImage newTexture) {
		texture = newTexture;

		int[] pixels = new int[texture.getWidth() * texture.getHeight()];
        pixels = texture.getRGB(0, 0, texture.getWidth(), texture.getHeight(), pixels, 0, texture.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(texture.getWidth() * texture.getHeight() * 4); //4 for RGBA, 3 for RGB

        for(int y = 0; y < texture.getHeight(); y++){
            for(int x = 0; x < texture.getWidth(); x++){
                int pixel = pixels[y * texture.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                //buffer.put((byte) (0xFF));    // Alpha component. Only for RGBA
                //buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

        // You now have a ByteBuffer filled with the color data of each pixel.
        // Now just create a texture ID and bind it. Then you can load it using 
        // whatever OpenGL method you want, for example:

        texId = GL11.glGenTextures(); //Generate texture ID
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId); //Bind texture ID

        //Setup wrap mode
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        //GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        //Send texel data to OpenGL
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, texture.getWidth(), texture.getHeight(), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
				        
	}*/
	
	//public void setTexture( String filename ) {
	public void setTexture( URL filename ) {
		
		try {
			// load texture from PNG file
			//texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(filename));
			//ResourceLoader.addResourceLocation( new ClasspathLocation() );// this should probably be a 1x thing
			//texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream( filename.getPath() ) );
			//texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream( filename.getFile() ) );
			texture = TextureLoader.getTexture("PNG", filename.openStream() );
		
			/*System.out.println("Texture loaded: "+texture);
			System.out.println(">> Image width: "+texture.getImageWidth());
			System.out.println(">> Image height: "+texture.getImageHeight());
			System.out.println(">> Texture width: "+texture.getTextureWidth());
			System.out.println(">> Texture height: "+texture.getTextureHeight());
			System.out.println(">> Texture ID: "+texture.getTextureID());*/
		} catch (IOException e) {
			System.out.println( "Error loading texture: " + filename );
			e.printStackTrace();
		}		
		
	}
	
	
	/*
	 * Updat the orientation of an object
	 */
	public void orient( Vector3d objVec, Vector3d targetVec ) {
		if( objVec.length() != 0 && targetVec.length() != 0 ) {
			Vector3d dir = new Vector3d();
			dir.cross( objVec, targetVec );
			//dir.cross( targetVec, objVec );
			//System.out.println( "orient cross " + dir );
			dir.set( ( objVec.y * targetVec.z - objVec.z * targetVec.y ), 
					 ( objVec.z * targetVec.x - objVec.x * targetVec.z ), 
					 ( objVec.x * targetVec.y - objVec.y * targetVec.x ) );
			dir.normalize();
			//dir.scale( 1.0 / dir.length() );
			double vdot = objVec.dot( targetVec );
			vdot = Math.max( Math.min( vdot / ( objVec.length() * targetVec.length() ), 
									   1.0), -1.0 );
			//double angle = ( Math.acos( vdot ) * ( Math.PI / 180.0 ) );
			double angle = ( Math.acos( vdot ) * ( 180.0 / Math.PI ) );
			if( dir.length() == 0 ) 
				rotation.set( objVec.x, objVec.y, objVec.z, 0.001 );
			else
				rotation.set( dir.x, dir.y, dir.z, angle );
			//System.out.println( "orient " + objVec + " " + targetVec + " " + dir + " " + vdot + " " + rotation );
			
		}
	}
	
	public void updateObjectKinematics( double dt ) {	
	//(defn update-object-kinematics
	//		  "Update the kinematics of an object by applying acceleration and velocity for an infinitesimal amount of time."
		//System.out.print( this );
		
		Vector3d f = (Vector3d) acceleration.clone();
		f.scale( getDoubleMass() );
		getBody().addForce( f.x, f.y, f.z );
		orient( new Vector3d(0,0,1), getVelocity() );
		//orient( new Vector3d(0,1,0), getVelocity() );
		//orient( new Vector3d(1,0,0), getForce() );
		//System.out.println( "Object " + uid + " force " + f );
	}
	
	public int getTextureId() {
		//return texId;
		if( texture != null )
			return texture.getTextureID();
		else
			return -1;
	}

	@Override
	public Iterator iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsKey(Object arg0) {
		// TODO Auto-generated method stub
		return myMap.containsKey(arg0);
	}

	@Override
	public IMapEntry entryAt(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPersistentCollection cons(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int count() {
		// TODO Auto-generated method stub
		return myMap.size();
	}

	@Override
	public IPersistentCollection empty() {
		// TODO Auto-generated method stub
		myMap.clear();
		return this;
	}

	@Override
	public boolean equiv(Object arg0) {
		// TODO Auto-generated method stub
		return myMap.equals(arg0);
	}

	@Override
	public ISeq seq() {
		// TODO Auto-generated method stub		
		return null;
		//return ISeq( myMap.keySet() );
	}

	@Override
	public Object valAt(Object arg0) {
		// TODO Auto-generated method stub
		return myMap.get(arg0);
	}

	@Override
	public Object valAt(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	
}


package brevis;

import clojure.lang.IMapEntry;
import clojure.lang.IPersistentCollection;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.OdeHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

// Shouldn't be any opengl stuff in here actually
//import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

//public class BrObject {
//public class BrObject implements clojure.lang.IRecord {
public class BrObject implements clojure.lang.IPersistentMap, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 5143539083266175610L;

	public Long uid;
	//public String type;
	public clojure.lang.Keyword type;
	public Vector3f acceleration;
	public Vector3f velocity;
	public Vector3f position;
	public double density = 1;
	public BrShape shape;
	public DMass mass;
	public Vector4f rotation;
	public Vector4f color;

	public Object data;

	public Long closestNeighbor;

	//public Matrix4d transform;

	public HashMap<Object,Object> myMap;

	public Vector<Long> nbrs;
	protected int texId = -1;

	public boolean drawable = true;
	public boolean hasShadow = true;

	public BrKDNode myKDnode  = null;

	// Physics
	public DBody body;
	public DGeom geom;

	public boolean enabledShadow() {
		return hasShadow;
	}

	public void setShadow( boolean newHasShadow ) {
		hasShadow = newHasShadow;
	}

	public String toString() {
		/*String s = "#BrObject{ :UID " + uid + ", :type " + type + ", :acceleration " + acceleration +
				", :velocity " + velocity + ", :position " + position + ", :density " + density +
				", :rotation " + rotation + ", : color " + color + ", :shape " + shape +
				"}";*/
		String s = "#BrObject{ :UID " + uid + ", :type " + type + ", :acceleration " + acceleration +
				", :velocity " + velocity + ", :position " + position + ", :density " + density +
				", :rotation " + rotation + ", : color " + color + ", :shape " + shape + ", [";
		/*Iterator itr = this.iterator();
		while( itr.hasNext() ) {
			Object o = itr.next();
			s += o + ", ";
		}*/
		s += "]}";
		return s;
	}

	public BrObject() {
		uid = (long)-1;
		//type = "Unassigned";
		type = clojure.lang.Keyword.intern( clojure.lang.Symbol.create( "Unassigned" ) );
		acceleration = new Vector3f( 0, 0, 0 );
		velocity = new Vector3f( 0, 0, 0 );
		position = new Vector3f( 0, 0, 0 );
		shape = null;//BrShape.createSphere( 1 ); too expensive
		color = new Vector4f( 1, 1, 1, 1 );
		rotation = new Vector4f( 1, 0, 0, 0 );
		data = null;
		myMap = new HashMap<Object,Object>();
	}

	public void setDrawable( boolean newDrawable ) {
		drawable = newDrawable;
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

	public double distanceTo( BrObject other ) {
		Vector3f delta = new Vector3f();
		delta.set( other.getPosition().sub(getPosition()));
		return delta.length();
	}

	public void setUID( Long UID ) {
		uid = UID;
	}

	/*public String getType() {
		return type;
	}*/

	public Object getType() {
		return type;
	}

	public Long getUID( ) {
		return uid;
	}

	public void setType( String newType ) {
		type = clojure.lang.Keyword.intern( clojure.lang.Symbol.create( newType ) );
		//type = newType;
	}

	public Vector<Long> getNeighbors() {
		return nbrs;
	}

	public Long getClosestNeighbor() {
		return closestNeighbor;
	}

	public void clearNeighbors() {
		nbrs.clear();
	}

	public void addNeighbor( Long UID ) {
		nbrs.add( UID );
	}

	public Vector3f getPosition() {
		//return position;
		return brevis.Utils.DVector3CToVector3f( body.getPosition() );
	}

	public Vector3f getVelocity() {
		return brevis.Utils.DVector3CToVector3f( body.getLinearVel() );
		//return velocity;
	}

	public Vector3f getForce() {
		return brevis.Utils.DVector3CToVector3f( body.getForce() );
		//return velocity;
	}

	public Vector3f getAcceleration() {
		return acceleration;
	}

	public void setAcceleration( Vector3f v ) {
		acceleration = v;
	}

	public void setVelocity( Vector3f v ) {
		//velocity = v;
		body.setLinearVel( brevis.Utils.Vector3fToDVector3( v ) );
	}

	public void setPosition( Vector3f v ) {
		//position = v;
		if( myKDnode != null ) {
			myKDnode.domain[0] = position.x;
			myKDnode.domain[1] = position.y;
			myKDnode.domain[2] = position.z;
		}
		body.setPosition( brevis.Utils.Vector3fToDVector3( v ) );
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

		//System.out.println( "makeReal " + shape.getDimension() + " " + density + " " + mass );

		body = OdeHelper.createBody( e.getWorld() );
		body.setMass( mass );
		HashMap<String,Object> bodymap = new HashMap<String,Object>();
		bodymap.put( "uid", uid );
		bodymap.put( "type", ((Keyword)type).getName() );// or toString
		body.setData( bodymap );

		geom = shape.createGeom( e.physics.getSpace() );
		geom.setBody( body );
		geom.setOffsetWorldPosition( position.x, position.y, position.z );

		/*if( shape.type != BrShapeType.MESH ) {
			shape.createMesh();
		}*/
		//shape.createVBOFromMesh();
	}

	public void recreatePhysicsGeom( Engine e ) {
		// need to remove old geom from body??
		geom = shape.createGeom( e.physics.getSpace() );
		geom.setBody( body );
		geom.setOffsetWorldPosition( position.x, position.y, position.z );
	}

	/*public void makeAbstract( Engine e ) {

	}*/

	public void setColor( Vector4f c ) {
		color = c;
	}

	public Vector4f getColor() {
		return color;
	}

	public void setDimension( Vector3f newDim, boolean withGraphics ) {
		shape.setDimension( newDim, withGraphics );
	}

	public Vector3f getDimension() {
		return shape.getDimension();
	}

	public Vector4f getRotation() {

		return rotation;
	}

	public void setRotation( Vector4f v ) {
		rotation = v;
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

	/**
	 * from https://bitbucket.org/kevglass/slick/src/9d7443ec33af80e3cd1d249d99087437d39d5f48/trunk/Slick/src/org/newdawn/slick/opengl/InternalTextureLoader.java?at=default
     * Get the closest greater power of 2 to the fold number
     *
     * @param fold The target number
     * @return The power of 2
     */
    public static int get2Fold(int fold) {
        int ret = 2;
        while (ret < fold) {
            ret *= 2;
        }
        return ret;
    }


	/*
	 * Update the orientation of an object
	 */
	public void orient( Vector3f objVec, Vector3f targetVec ) {
		if( objVec.length() != 0 && targetVec.length() != 0 ) {
			Vector3f dir = new Vector3f();
			dir.set(objVec.cross(targetVec));
			//dir.cross( targetVec, objVec );
			//System.out.println( "orient cross " + dir );
			dir.set( ( objVec.y * targetVec.z - objVec.z * targetVec.y ),
					 ( objVec.z * targetVec.x - objVec.x * targetVec.z ),
					 ( objVec.x * targetVec.y - objVec.y * targetVec.x ) );
			if( dir.length() != 0 )
				dir.normalize();
			//dir.scale( 1.0 / dir.length() );
			double vdot = targetVec.dot(objVec);
			vdot = Math.max( Math.min( vdot / ( objVec.length() * targetVec.length() ),
									   1.0), -1.0 );
			//double angle = ( Math.acos( vdot ) * ( Math.PI / 180.0 ) );
			double angle = ( Math.acos( vdot ) * ( 180.0 / Math.PI ) );
			if( dir.length() == 0 )
				rotation.set( objVec.x, objVec.y, objVec.z, (float)0.001 );
			else
				rotation.set( dir.x, dir.y, dir.z, (float)angle );
			//System.out.println( "orient " + objVec + " " + targetVec + " " + dir + " " + vdot + " " + rotation );

		}
	}

	public void updateObjectKinematics( double dt ) {
	//(defn update-object-kinematics
	//		  "Update the kinematics of an object by applying acceleration and velocity for an infinitesimal amount of time."
		//System.out.print( this );

		Vector3f f = new Vector3f( acceleration ).mul( (float) getDoubleMass() );
		getBody().addForce( f.x, f.y, f.z );
		orient( new Vector3f(0,0,1), getVelocity() );
		//orient( new Vector3f(0,1,0), getVelocity() );
		//orient( new Vector3f(1,0,0), getForce() );
		//System.out.println( "Object " + uid + " force " + f );
	}

	@Override
	public Iterator iterator() {
		// TODO Auto-generated method stub
		return myMap.keySet().iterator();
		//return null;
	}

	@Override
	public boolean containsKey(Object arg0) {
		// TODO Auto-generated method stub
		return myMap.containsKey(arg0);
	}

	@Override
	public IMapEntry entryAt(Object arg0) {
		// TODO Auto-generated method stub
		//return myMap.
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
		//List l = new List();
		//l.addAll( myMap.keySet() );
		//ISeq s = (ISeq) PersistentList.create( l );
		//return s;
		//s.addAll( myMap.keySet() );
		//return s;
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

	public void destroy( Engine e ) {
		shape.destroy();
		//System.out.println( "[A]Number of objects in collision space : " + e.physics.space.getNumGeoms() );
		e.physics.space.remove( geom );
		//System.out.println( "[B]Number of objects in collision space : " + e.physics.space.getNumGeoms() );
		//body.destroy();
		geom.destroy();
	}

	/* Serialization stuff */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		 out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}


}

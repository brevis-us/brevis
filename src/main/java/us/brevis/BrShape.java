
package us.brevis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;

import org.joml.Vector3f;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DTriMeshData;
import org.ode4j.ode.OdeHelper;

import us.brevis.graphics.BrMesh;

public class BrShape implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1469388870902925210L;

	public enum BrShapeType {
		BOX, SPHERE, CONE, CYLINDER, MESH,
		// Unit meshes for optimized rendering
		UNIT_CONE, UNIT_SPHERE, //FLOOR
		ICOSAHEDRON, PRISM
	};
	
	static public String objDir = "obj" + File.separator;
	
	public BrShapeType type;
	//public Vector3d dim;
	public Vector3f dim;
	public int vertBID = -1;
	public int colBID = -1;
	public int idxBID = -1;
	public int numIdx = 0;
	public BrMesh mesh = null;
	public Object data = null;
	
	public Vector3f center;
	
	// Make final?
	public static BrMesh unitCone = null;	
	public static BrMesh unitSphere = null;	
	
	private int objectlist;
	
	public void resize( Vector3f newDim ) {
		dim = newDim;
		// should reload shoul
	}
	
	public BrMesh getMesh() {
		return mesh;
	}
	
	public String toString() {
		String s = "#BrShape{ :type " + type + ", :dim" + dim +
				", :mesh " + mesh + "}";		 				
		return s;
	}
	
	void computeCenter() {
		if( type == BrShapeType.BOX ) {
			center = new Vector3f( ( dim.x / 2f) , ( dim.y / 2f ), ( dim.z / 2f ) );
		} else if( type == BrShapeType.SPHERE ) {
			//center = new Vector3f( dim.x, dim.x, dim.x );
			center = new Vector3f( 0, 0, 0 );
		} else {
			center = new Vector3f( 0, 0, 0 );
		}
	}
	
	BrShape( BrShapeType t, Vector3f d, boolean withGraphics ) {
		//type = BrShapeType.SPHERE;
		//dim = new Vector3d(1,1,1);
		type = t;
		dim = d;
		
		if( type == BrShapeType.UNIT_CONE ) {
			if( unitCone == null ) {
				initUnitCone( withGraphics );				
			}
			mesh = unitCone;
			//dim = new Vector3d( mesh.getXWidth(), mesh.getYHeight(), mesh.getZDepth() );
			dim = new Vector3f( 1, 1, 1 );
			//System.out.println( dim );
		} else if( type == BrShapeType.UNIT_SPHERE) {
			if( unitSphere== null ) {
				initUnitSphere( withGraphics );				
			}
			mesh = unitSphere;
			//dim = new Vector3d( mesh.getXWidth(), mesh.getYHeight(), mesh.getZDepth() );
			dim = new Vector3f( 1, 1, 1 );
			//System.out.println( dim );
		} else if( type == BrShapeType.SPHERE ) {
			//data = OdeHelper.createSphere()
		} else if( type == BrShapeType.CYLINDER ) {
			//data = new Cylinder();
		} else if( type == BrShapeType.CONE ) {
			//data = new Cylinder();
		} else if( type == BrShapeType.BOX ) {
		} else {
			createMesh( withGraphics );
			if( mesh != null ) {
				mesh.rescaleMesh( (float)dim.x, (float)dim.y, (float)dim.z, withGraphics );
			}
			dim = new Vector3f( 1, 1, 1 );
		}
				
		computeCenter();
	}
	
	BrShape( String filename, boolean isResource, boolean withGraphics ) {		
		type = BrShapeType.MESH;
		//createMesh( withGraphics );
		loadMesh( filename, isResource, withGraphics );
		if( mesh != null ) {
			mesh.rescaleMesh( (float)dim.x, (float)dim.y, (float)dim.z, withGraphics );
		}
		computeCenter();
		//loadMesh( filename, isResource, withGraphics );
	}
	
	BrShape( String filename, boolean isResource, boolean withGraphics, Vector3f d ) {		
		type = BrShapeType.MESH;
		dim = d;
		//createMesh( withGraphics );
		loadMesh( filename, isResource, withGraphics );
		if( mesh != null ) {
			mesh.rescaleMesh( (float)dim.x, (float)dim.y, (float)dim.z, withGraphics );
		}
		computeCenter();
		//loadMesh( filename, isResource, withGraphics );
	}
	
	BrShape( List<Vector3f> verts ) {
		type = BrShapeType.MESH;
		loadMesh( verts );
		dim = new Vector3f( 1, 1, 1 );
		computeCenter();
	}
	
	
	
	BrShape( BrMesh inMesh ) {
		type = BrShapeType.MESH;
		mesh = inMesh;
		dim = new Vector3f( 1, 1, 1 );
		computeCenter();
	}
	
	public void initUnitCone( boolean withGraphics ) {		
		String filename = objDir + "cone.obj";
	
		try {		
			BufferedReader br = new BufferedReader( new InputStreamReader( ClassLoader.getSystemResource( filename ).openStream() ) );
			unitCone = new BrMesh( br, true, withGraphics );
		} catch( Exception e ) {
			e.printStackTrace();
		}	
	}
	
	public void initUnitSphere( boolean withGraphics ) {		
		String filename = objDir + "sphere.obj";
	
		try {		
			BufferedReader br = new BufferedReader( new InputStreamReader( ClassLoader.getSystemResource( filename ).openStream() ) );
			unitSphere = new BrMesh( br, true, withGraphics );
		} catch( Exception e ) {
			e.printStackTrace();
		}	
	}
	
	public String getType() {
		if( type == BrShapeType.BOX ) {
			return "box";
		} else if( type == BrShapeType.SPHERE  ||  type == BrShapeType.UNIT_SPHERE ) {
			return "sphere";
		} else if( type == BrShapeType.CONE || type == BrShapeType.UNIT_CONE ) {
			return "cone";			
		} else if( type == BrShapeType.CYLINDER ) {
			return "cylinder";
		} else if( type == BrShapeType.ICOSAHEDRON ) {
			return "icosahedron";
		} else if( type == BrShapeType.PRISM ) {
			return "prism";
		} else if( type == BrShapeType.MESH ) {
			return "mesh";
		} else {
			return "unknown";
		}
	}
	
	/*
	 * Return a mass that is appropriate for this object and its dimensions
	 */
	public DMass createMass( double density ) {
		DMass m = OdeHelper.createMass();
		if( type == BrShapeType.BOX ) {
			m.setBox(density, dim.x, dim.y, dim.z );
		} else if( type == BrShapeType.SPHERE || type == BrShapeType.UNIT_SPHERE || type == BrShapeType.ICOSAHEDRON || type == BrShapeType.PRISM ) {
			m.setSphere( density, dim.x );
		} else if( type == BrShapeType.CONE || type == BrShapeType.UNIT_CONE ) {
			m.setSphere(density, dim.x);
		} else if( type == BrShapeType.CYLINDER ) {
			m.setSphere(density, dim.x);
		} else if( type == BrShapeType.MESH ) {
			m.setSphere(density, dim.x );
		}
		return m;
	}
	
	/*
	 * VBO code, currently nonfunctional
	 * 
	  public static int createVBOID() {
		  if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
		    IntBuffer buffer = BufferUtils.createIntBuffer(1);
		    ARBVertexBufferObject.glGenBuffersARB(buffer);
		    return buffer.get(0);
		  }
		  return 0;
		}*/
	
	/*
	public static int createVBOID() {
	    //IntBuffer buffer = BufferUtils.createIntBuffer(1);
	    //GL15.glGenBuffers(buffer);
	    //return buffer.get(0);
	    //Or alternatively you can simply use the convenience method:
	    return GL15.glGenBuffers(); //Which can only supply you with a single id.
	}


	public static void vertexBufferData(int id, FloatBuffer buffer) { //Not restricted to FloatBuffer
	    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id); //Bind buffer (also specifies type of buffer)
	    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW); //Send up the data and specify usage hint.
	}
	public static void indexBufferData(int id, IntBuffer buffer) { //Not restricted to IntBuffer
	    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, id);
	    GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}

	public void createVBOFromMesh( ) {
		vertBID = createVBOID();
		colBID = createVBOID();
		idxBID = createVBOID();
		numIdx = mesh.numIdx();
		
		vertexBufferData( vertBID, FloatBuffer.wrap( mesh.verts ) );
		vertexBufferData( colBID, FloatBuffer.wrap( mesh.col ) );
		indexBufferData( idxBID, IntBuffer.wrap( mesh .idx ) );
		
	}
	
	public void createMesh() {
		mesh = new BrMesh();
		
		if( type == BrShapeType.BOX ) {
			mesh.initBox( dim );			
		} else if( type == BrShapeType.SPHERE ) {
			mesh.initSphere( dim );
		} else if( type == BrShapeType.CONE ) {
			mesh.initCone( dim );
		} else if( type == BrShapeType.CYLINDER ) {
			mesh.initCylinder( dim );
		}
	}
	
	public static void bufferData(int id, FloatBuffer buffer) {
		  if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
		    ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, id);
		    ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, buffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
		  }
		}
*/
	
	/*public void createMesh() {		
		String filename  = "";
		if( type == BrShapeType.BOX ) {
			//mesh.initBox( dim );
			filename = "box.obj";
		} else if( type == BrShapeType.SPHERE ) {
			//mesh.initSphere( dim );
			filename = "sphere.obj";
		} else if( type == BrShapeType.CONE ) {
			//mesh.initCone( dim );
			filename = "cone.obj";
		} else if( type == BrShapeType.CYLINDER ) {
			//mesh.initCylinder( dim );
			filename = "cylinder.obj";
		}
		filename = objDir + filename;
		
		//System.out.println( "createMesh " + filename + " " + type );
		loadMesh( filename, true );
	}*/
	
	
	public void createMesh( boolean withGraphics ) {		
		String filename  = "";
		if( type == BrShapeType.BOX ) {
			//initBox( dim );
			filename = "box.obj";
		} else if( type == BrShapeType.SPHERE ) {
			//mesh.initSphere( dim );
			filename = "sphere.obj";
		} else if( type == BrShapeType.CONE ) {
			//mesh.initCone( dim );
			filename = "cone.obj";
		} else if( type == BrShapeType.CYLINDER ) {
			//mesh.initCylinder( dim );
			filename = "cylinder.obj";
		} else if( type == BrShapeType.ICOSAHEDRON ) {
			//mesh.initCylinder( dim );
			filename = "icosahedron.obj";
		} else if( type == BrShapeType.PRISM) {
			//mesh.initCylinder( dim );
			filename = "prism.obj";
		}
		//filename = objDir + filename;		
		
		//System.out.println( "createMesh " + filename + " " + type );
		loadMesh( filename, true, withGraphics );
	}
	
	public void createMesh( boolean withGraphics, boolean isResource ) {// this version is actually just for meshes		
		String filename  = "";
		if( type == BrShapeType.BOX ) {
			//initBox( dim );
			filename = "box.obj";
		} else if( type == BrShapeType.SPHERE ) {
			//mesh.initSphere( dim );
			filename = "sphere.obj";
		} else if( type == BrShapeType.CONE ) {
			//mesh.initCone( dim );
			filename = "cone.obj";
		} else if( type == BrShapeType.CYLINDER ) {
			//mesh.initCylinder( dim );
			filename = "cylinder.obj";
		} else if( type == BrShapeType.ICOSAHEDRON ) {
			//mesh.initCylinder( dim );
			filename = "icosahedron.obj";
		} else if( type == BrShapeType.PRISM) {
			//mesh.initCylinder( dim );
			filename = "prism.obj";
		}
		if( isResource )
			filename = objDir + filename;
		
		//System.out.println( "createMesh " + filename + " " + type );
		loadMesh( filename, isResource, withGraphics );
	}
	
	public void loadMesh( List<Vector3f> verts ) {
		try {
			mesh = new BrMesh( verts );			
					
			if( mesh.numpolygons() == 0 ) {
				System.out.println("Found 0 faces when loading vert series." );
			}
			
			// this is actually size
			//dim = new Vector3d( mesh.getXWidth(), mesh.getYHeight(), mesh.getZDepth() );
			
			// this is being used for scale
			dim = new Vector3f( 1, 1, 1 );
			
			//mesh.opengldrawtolist();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}	
	
	public void loadMesh( String filename, boolean isResource, boolean withGraphics ) {
		try {
			if( isResource )
				filename = objDir + filename;
			//System.out.println( "Loading object: " + filename );			
			
			if( isResource ) {			
			//FileReader fr = new FileReader(filename);		
				//BufferedReader br = new BufferedReader( new InputStreamReader( ClassLoader.getSystemResource( filename ).openStream() ) );
				BufferedReader br = new BufferedReader( new InputStreamReader( Thread.currentThread().getContextClassLoader().getResourceAsStream(filename) ) );
				
				//mesh = new BrMesh( br, false );
				mesh = new BrMesh( br, true, withGraphics );
			} else {				
				BufferedReader br = new BufferedReader( new FileReader( filename ) );
				//mesh = new BrMesh( br, false );
				mesh = new BrMesh( br, true, withGraphics );
			}
					
			if( mesh.numpolygons() == 0 ) {
				System.out.println("Found 0 faces when reading: " + filename );
			}
			
			// this is actually size
			//dim = new Vector3d( mesh.getXWidth(), mesh.getYHeight(), mesh.getZDepth() );
			
			// this is being used for scale
			dim = new Vector3f( 1, 1, 1 );
			
			//mesh.opengldrawtolist();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}		
	
	public DGeom createGeom( DSpace space ) {
		DGeom g;
		
		if( mesh != null ) {
			DTriMeshData new_tmdata = OdeHelper.createTriMeshData();
			//System.out.println( "createGeom " + type );
			new_tmdata.build( mesh.trimeshVertices( new float[]{ (float) dim.x, (float) dim.y, (float) dim.z } ), mesh.trimeshIndices() );
			
			g = OdeHelper.createTriMesh(space, new_tmdata, null, null, null);
			
			
			//g.getBody().
			
			return g;
		} 
		
		// Should be where primitive shapes are
		switch( type ) {
		case BOX:
			return OdeHelper.createBox( space, dim.x, dim.y, dim.z );		
		default:
		case SPHERE:
			return OdeHelper.createSphere( space, dim.x );			
		}		
	}
	
	public void setDimension( Vector3f newd, boolean withGraphics ) {
		dim = newd;
		if( mesh != null ) {
			mesh.rescaleMesh( (float)newd.x, (float)newd.y, (float)newd.z, withGraphics );
			//System.out.println( "rescaling " + newd );
		}
	}
	
	public Vector3f getDimension() {
		return dim;
	}
	
	public static BrShape createMeshFromBrMesh( BrMesh inMesh ) {
		//System.out.println( filename );
		return ( new BrShape( inMesh ) );
	}
	
	public static BrShape createMeshFromFile( String filename, boolean isResource, boolean withGraphics, Vector3f dim ) {
		//System.out.println( filename );
		//return ( new BrShape( filename, isResource, withGraphics ) );
		return ( new BrShape( filename, isResource, withGraphics, dim ) );
	}
	
	public static BrShape createMeshFromTriangles( List<Vector3f> verts ) {
		//System.out.println( filename );
		//return ( new BrShape( filename, isResource, withGraphics ) );
		return ( new BrShape( verts ) );
	}
	
	public static BrShape createSphere( double r, boolean withGraphics ) {
		return ( new BrShape( BrShapeType.SPHERE, new Vector3f( (float)r, (float)r, (float)r ), withGraphics ) );
		//return ( new BrShape( BrShapeType.UNIT_SPHERE, new Vector3d( r, r, r )));	
	}
	
	public static BrShape createIcosahedron( double r, boolean withGraphics ) {
		return ( new BrShape( BrShapeType.ICOSAHEDRON, new Vector3f( (float)r, (float)r, (float)r ), withGraphics ) );
		//return ( new BrShape( BrShapeType.UNIT_SPHERE, new Vector3d( r, r, r )));	
	}
	
	public static BrShape createBox( double x, double y, double z, boolean withGraphics ) {
		return ( new BrShape( BrShapeType.BOX, new Vector3f( (float)x, (float)y, (float)z ), withGraphics ) );
	}
	
	public static BrShape createCone( double length, double base, boolean withGraphics ) {
		//return ( new BrShape( BrShapeType.CONE, new Vector3d( length, base, 25 )));	// last element of vector is # of sides or stacks (depending on renderer)
		//return ( new BrShape( BrShapeType.UNIT_CONE, new Vector3f( (float)length, (float)base, (float)25 )));	// last element of vector is # of sides or stacks (depending on renderer)
		return ( new BrShape( BrShapeType.CONE, new Vector3f( (float)length, (float)base, (float)25 ), withGraphics ));	// last element of vector is # of sides or stacks (depending on renderer)
	}
	
	public static BrShape createCylinder( double length, double radius, boolean withGraphics ) {
		return ( new BrShape( BrShapeType.CYLINDER, new Vector3f( (float)length, (float)radius, (float)25 ), withGraphics ));	// last element of vector is # of sides or stacks (depending on renderer)
	}
	
	public static BrShape createCylinder( double length, double radius1, double radius2, boolean withGraphics ) {
		return ( new BrShape( BrShapeType.CYLINDER, new Vector3f( (float)length, (float)radius1, (float)radius2 ), withGraphics ));	// last element of vector is # of sides or stacks (depending on renderer)
	}

	public void destroy() {
		mesh.destroy();
		
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		 out.defaultWriteObject();
	}
		     
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}

}


package us.brevis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;

import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DTriMeshData;
import org.ode4j.ode.OdeHelper;

import sc.iview.vector.JOMLVector3;
import sc.iview.vector.Vector3;
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
	public Vector3 dim;
	public int vertBID = -1;
	public int colBID = -1;
	public int idxBID = -1;
	public int numIdx = 0;
	public BrMesh mesh = null;
	public Object data = null;
	
	public Vector3 center;
	
	// Make final?
	public static BrMesh unitCone = null;	
	public static BrMesh unitSphere = null;	
	
	private int objectlist;
	
	public void resize( Vector3 newDim ) {
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
			center = new JOMLVector3( ( dim.xf() / 2f) , ( dim.yf() / 2f ), ( dim.zf() / 2f ) );
		} else if( type == BrShapeType.SPHERE ) {
			center = new JOMLVector3( 0, 0, 0 );
		} else {
			center = new JOMLVector3( 0, 0, 0 );
		}
	}
	
	BrShape( BrShapeType t, Vector3 d, boolean withGraphics ) {
		type = t;
		dim = d;
		
		if( type == BrShapeType.UNIT_CONE ) {
			if( unitCone == null ) {
				initUnitCone( withGraphics );				
			}
			mesh = unitCone;
			dim = new JOMLVector3( 1, 1, 1 );
		} else if( type == BrShapeType.UNIT_SPHERE) {
			if( unitSphere== null ) {
				initUnitSphere( withGraphics );				
			}
			mesh = unitSphere;
			//dim = new Vector3d( mesh.getXWidth(), mesh.getYHeight(), mesh.getZDepth() );
			dim = new JOMLVector3( 1, 1, 1 );
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
				mesh.rescaleMesh( (float)dim.xf(), (float)dim.yf(), (float)dim.zf() );
			}
			dim = new JOMLVector3( 1, 1, 1 );
		}
				
		computeCenter();
	}
	
	BrShape( String filename, boolean isResource, boolean withGraphics ) {		
		type = BrShapeType.MESH;
		//createMesh( withGraphics );
		loadMesh( filename, isResource, withGraphics );
		if( mesh != null ) {
			mesh.rescaleMesh( (float)dim.xf(), (float)dim.yf(), (float)dim.zf() );
		}
		computeCenter();
		//loadMesh( filename, isResource, withGraphics );
	}
	
	BrShape( String filename, boolean isResource, boolean withGraphics, Vector3 d ) {
		type = BrShapeType.MESH;
		dim = d;
		//createMesh( withGraphics );
		loadMesh( filename, isResource, withGraphics );
		if( mesh != null ) {
			mesh.rescaleMesh( (float)dim.xf(), (float)dim.yf(), (float)dim.zf() );
		}
		computeCenter();
		//loadMesh( filename, isResource, withGraphics );
	}
	
	BrShape( List<Vector3> verts ) {
		type = BrShapeType.MESH;
		loadMesh( verts );
		dim = new JOMLVector3( 1, 1, 1 );
		computeCenter();
	}
	
	
	
	BrShape( BrMesh inMesh ) {
		type = BrShapeType.MESH;
		mesh = inMesh;
		dim = new JOMLVector3( 1, 1, 1 );
		computeCenter();
	}
	
	public void initUnitCone( boolean withGraphics ) {		
		String filename = objDir + "cone.obj";
	
		try {		
			BufferedReader br = new BufferedReader( new InputStreamReader( ClassLoader.getSystemResource( filename ).openStream() ) );
			unitCone = new BrMesh( br, true );
		} catch( Exception e ) {
			e.printStackTrace();
		}	
	}
	
	public void initUnitSphere( boolean withGraphics ) {		
		String filename = objDir + "sphere.obj";
	
		try {		
			BufferedReader br = new BufferedReader( new InputStreamReader( ClassLoader.getSystemResource( filename ).openStream() ) );
			unitSphere = new BrMesh( br, true );
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
			m.setBox(density, dim.xf(), dim.yf(), dim.zf() );
		} else if( type == BrShapeType.SPHERE || type == BrShapeType.UNIT_SPHERE || type == BrShapeType.ICOSAHEDRON || type == BrShapeType.PRISM ) {
			m.setSphere( density, dim.xf() );
		} else if( type == BrShapeType.CONE || type == BrShapeType.UNIT_CONE ) {
			m.setSphere(density, dim.xf());
		} else if( type == BrShapeType.CYLINDER ) {
			m.setSphere(density, dim.xf());
		} else if( type == BrShapeType.MESH ) {
			m.setSphere(density, dim.xf() );
		}
		return m;
	}
	
	public void createMesh( boolean withGraphics ) {		
		createMesh( withGraphics, false );
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
		
		loadMesh( filename, isResource, withGraphics );
	}
	
	public void loadMesh( List<Vector3> verts ) {
		try {
			mesh = new BrMesh( verts );			
					
			if( mesh.numpolygons() == 0 ) {
				System.out.println("Found 0 faces when loading vert series." );
			}
			
			// this is actually size
			//dim = new Vector3d( mesh.getXWidth(), mesh.getYHeight(), mesh.getZDepth() );
			
			// this is being used for scale
			dim = new JOMLVector3( 1, 1, 1 );
			
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
				BufferedReader br = new BufferedReader( new InputStreamReader( Thread.currentThread().getContextClassLoader().getResourceAsStream(filename) ) );
				
				mesh = new BrMesh( br, true );
			} else {				
				BufferedReader br = new BufferedReader( new FileReader( filename ) );
				mesh = new BrMesh( br, true );
			}
					
			if( mesh.numpolygons() == 0 ) {
				System.out.println("Found 0 faces when reading: " + filename );
			}
			
			// this is being used for scale
			dim = new JOMLVector3( 1, 1, 1 );
			
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
			new_tmdata.build( mesh.trimeshVertices( new float[]{dim.xf(), dim.yf(), dim.zf()} ), mesh.trimeshIndices() );
			
			g = OdeHelper.createTriMesh(space, new_tmdata, null, null, null);

			return g;
		} 
		
		// Should be where primitive shapes are
		switch( type ) {
		case BOX:
			return OdeHelper.createBox( space, dim.xf(), dim.yf(), dim.zf() );
		default:
		case SPHERE:
			return OdeHelper.createSphere( space, dim.xf() );
		}		
	}
	
	public void setDimension( Vector3 newd, boolean withGraphics ) {
		dim = newd;
		if( mesh != null ) {
			mesh.rescaleMesh(newd.xf(), newd.yf(), newd.zf() );
		}
	}
	
	public Vector3 getDimension() {
		return dim;
	}
	
	public static BrShape createMeshFromBrMesh( BrMesh inMesh ) {
		return ( new BrShape( inMesh ) );
	}
	
	public static BrShape createMeshFromFile( String filename, boolean isResource, boolean withGraphics, Vector3 dim ) {
		return ( new BrShape( filename, isResource, withGraphics, dim ) );
	}
	
	public static BrShape createMeshFromTriangles( List<Vector3> verts ) {
		//System.out.println( filename );
		//return ( new BrShape( filename, isResource, withGraphics ) );
		return ( new BrShape( verts ) );
	}
	
	public static BrShape createSphere( double r, boolean withGraphics ) {
		return ( new BrShape( BrShapeType.SPHERE, new JOMLVector3( (float)r, (float)r, (float)r ), withGraphics ) );
	}
	
	public static BrShape createIcosahedron( double r, boolean withGraphics ) {
		return ( new BrShape( BrShapeType.ICOSAHEDRON, new JOMLVector3( (float)r, (float)r, (float)r ), withGraphics ) );
	}
	
	public static BrShape createBox( double x, double y, double z, boolean withGraphics ) {
		return ( new BrShape( BrShapeType.BOX, new JOMLVector3( (float)x, (float)y, (float)z ), withGraphics ) );
	}
	
	public static BrShape createCone( double length, double base, boolean withGraphics ) {
		return ( new BrShape( BrShapeType.CONE, new JOMLVector3( (float)base, (float)length, (float)25 ), withGraphics ));	// last element of vector is # of sides or stacks (depending on renderer)
	}
	
	public static BrShape createCylinder( double length, double radius, boolean withGraphics ) {
		return ( new BrShape( BrShapeType.CYLINDER, new JOMLVector3( (float)length, (float)radius, (float)25 ), withGraphics ));	// last element of vector is # of sides or stacks (depending on renderer)
	}
	
	public static BrShape createCylinder( double length, double radius1, double radius2, boolean withGraphics ) {
		return ( new BrShape( BrShapeType.CYLINDER, new JOMLVector3( (float)length, (float)radius1, (float)radius2 ), withGraphics ));	// last element of vector is # of sides or stacks (depending on renderer)
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

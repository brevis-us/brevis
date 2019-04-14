

package brevis.graphics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

//import javax.vecmath.Point3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;


/**
 * derived from code by:
 * @author Jeremy Adams (elias4444)
 *
 * Use these lines if reading from a file
 * FileReader fr = new FileReader(ref);
 * BufferedReader br = new BufferedReader(fr);

 * Use these lines if reading from within a jar
 * InputStreamReader fr = new InputStreamReader(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(ref)));
 * BufferedReader br = new BufferedReader(fr);
 */

public class BrMesh implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7412159215339192242L;
	public ArrayList<float[]> vertexsets = new ArrayList<float[]>(); // Vertex Coordinates
	public ArrayList<float[]> vertexsetsnorms = new ArrayList<float[]>(); // Vertex Coordinates Normals
	public ArrayList<float[]> vertexsetstexs = new ArrayList<float[]>(); // Vertex Coordinates Textures
	public ArrayList<int[]> faces = new ArrayList<int[]>(); // Array of Faces (vertex sets)
	public ArrayList<int[]> facestexs = new ArrayList<int[]>(); // Array of of Faces textures
	public ArrayList<int[]> facesnorms = new ArrayList<int[]>(); // Array of Faces normals
	
	private int objectlist;
	private int numpolys = 0;
	
	//// Statisitcs for drawing ////
	public float toppoint = 0;		// y+
	public float bottompoint = 0;	// y-
	public float leftpoint = 0;		// x-
	public float rightpoint = 0;	// x+
	public float farpoint = 0;		// z-
	public float nearpoint = 0;		// z+	
	
	public boolean redraw = false;
	
	public BrMesh clone()  {
		/* Create a copy of this mesh */
		BrMesh m = new BrMesh();
		
		m.vertexsets = vertexsets;
		m.vertexsetsnorms = vertexsetsnorms;
		m.vertexsetstexs = vertexsetstexs;
		
		m.faces = faces;
		m.facestexs = facestexs;
		m.facesnorms = facesnorms;
		
		m.toppoint = toppoint;
		m.bottompoint = bottompoint;
		m.leftpoint = leftpoint;
		m.rightpoint = rightpoint;
		m.farpoint = farpoint;
		m.nearpoint = nearpoint;
		
		return m;
	}
	
	public String toString() {
		String s = "#BrMesh{ :numpolys " + numpolys + ", :toppoint " + toppoint +
				", :bottompoint " + bottompoint + ", :leftpoint " + leftpoint +
				", :rightpoint " + rightpoint + ", :farpoint " + farpoint +
				", :nearpoint " + nearpoint + 
				"}";		 				 				
		return s;
	}
	
	public BrMesh(BufferedReader ref, boolean centerit, boolean withGraphics ) {
		loadobject(ref);
		if (centerit) {
			centerit();
		}
		if( withGraphics ) {
			opengldrawtolist();
		}		
		numpolys = faces.size();
		// We don't actually want to cleanup
		//cleanup();
	}
	
	public BrMesh() {
		
	}

	public BrMesh(List<Vector3f> verts) {		
		cleanup();

		boolean firstpass = true;

		for( Vector3f v : verts ) {
			float[] coords = new float[3];
			coords[0] = v.x;
			coords[1] = v.y;
			coords[2] = v.z;
			//// check for farpoints ////
			if (firstpass) {
				rightpoint = coords[0];
				leftpoint = coords[0];
				toppoint = coords[1];
				bottompoint = coords[1];
				nearpoint = coords[2];
				farpoint = coords[2];
				firstpass = false;
			}
			if (coords[0] > rightpoint) {
				rightpoint = coords[0];
			}
			if (coords[0] < leftpoint) {
				leftpoint = coords[0];
			}
			if (coords[1] > toppoint) {
				toppoint = coords[1];
			}
			if (coords[1] < bottompoint) {
				bottompoint = coords[1];
			}
			if (coords[2] > nearpoint) {
				nearpoint = coords[2];
			}
			if (coords[2] < farpoint) {
				farpoint = coords[2];
			}
			/////////////////////////////
			vertexsets.add(coords);
		}		
		List<Vector3f> normals = new ArrayList<Vector3f>();
		Vector3f p1 = new Vector3f(), p2 = new Vector3f(), p3  = new Vector3f();
		Vector3f edge1 = new Vector3f(), edge2 = new Vector3f();
		Vector3f veccross = new Vector3f();
		for( int k = 0; k < verts.size(); k+=3 ) {
			p1.set( verts.get(k).x, verts.get(k).y, verts.get(k).z );
			p2.set( verts.get(k+1).x, verts.get(k+1).y, verts.get(k+1).z );
			//p3.set( verts.get(k+2).x, verts.get(k+2).y, verts.get(k+2).z );
			p3.set( verts.get(k+2).x, verts.get(k+2).y, verts.get(k+2).z );
			Vector3f.sub(p1, p2, edge1);
			Vector3f.sub(p1, p3, edge2);			
			Vector3f.cross( edge1, edge2, veccross );
			//Vector3f.cross( edge2, edge1, veccross );
			//normals.add( new Vector3f( veccross ) ); 
			//normals.add( new Vector3f( veccross ) ); 
			//normals.add( new Vector3f( veccross ) );
			vertexsetsnorms.add( new float[]{ veccross.x, veccross.y, veccross.z } );
			vertexsetsnorms.add( new float[]{ veccross.x, veccross.y, veccross.z } );
			vertexsetsnorms.add( new float[]{ veccross.x, veccross.y, veccross.z } );
		}
		for( int k = 0; k < vertexsets.size(); k+=3 ) {
			int[] v = new int[]{ k+1, k+2, k+3 };
			faces.add( v );
			//int[] vn = new int[]{ 0, 0, 0 };
			int[] vn = new int[]{ k+1, k+2, k+3 };
			facesnorms.add( vn );
			int[] vt = new int[]{ 0, 0, 0 };
			facestexs.add( vt );
		}
		centerit();	
		opengldrawtolist();
		numpolys = faces.size();
	}
	
	@SuppressWarnings("unused")
	private void cleanup() {
		vertexsets.clear();
		vertexsetsnorms.clear();
		vertexsetstexs.clear();
		faces.clear();
		facestexs.clear();
		facesnorms.clear();
	}
	
	private void loadobject(BufferedReader br) {
		int linecounter = 0;
		try {
			
			String newline;
			boolean firstpass = true;
			
			while (((newline = br.readLine()) != null)) {
				linecounter++;
				newline = newline.trim();
				if (newline.length() > 0) {
					if (newline.charAt(0) == 'v' && newline.charAt(1) == ' ') {
						float[] coords = new float[3];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1;i < coordstext.length;i++) {
							coords[i-1] = Float.valueOf(coordstext[i]).floatValue();
						}
						//// check for farpoints ////
						if (firstpass) {
							rightpoint = coords[0];
							leftpoint = coords[0];
							toppoint = coords[1];
							bottompoint = coords[1];
							nearpoint = coords[2];
							farpoint = coords[2];
							firstpass = false;
						}
						if (coords[0] > rightpoint) {
							rightpoint = coords[0];
						}
						if (coords[0] < leftpoint) {
							leftpoint = coords[0];
						}
						if (coords[1] > toppoint) {
							toppoint = coords[1];
						}
						if (coords[1] < bottompoint) {
							bottompoint = coords[1];
						}
						if (coords[2] > nearpoint) {
							nearpoint = coords[2];
						}
						if (coords[2] < farpoint) {
							farpoint = coords[2];
						}
						/////////////////////////////
						vertexsets.add(coords);
					}
					if (newline.charAt(0) == 'v' && newline.charAt(1) == 't') {
						float[] coords = new float[4];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1;i < coordstext.length;i++) {
							coords[i-1] = Float.valueOf(coordstext[i]).floatValue();
						}
						vertexsetstexs.add(coords);
					}
					if (newline.charAt(0) == 'v' && newline.charAt(1) == 'n') {
						float[] coords = new float[4];
						String[] coordstext = new String[4];
						coordstext = newline.split("\\s+");
						for (int i = 1;i < coordstext.length;i++) {
							coords[i-1] = Float.valueOf(coordstext[i]).floatValue();
						}
						vertexsetsnorms.add(coords);
					}
					if (newline.charAt(0) == 'f' && newline.charAt(1) == ' ') {
						String[] coordstext = newline.split("\\s+");
						int[] v = new int[coordstext.length - 1];
						int[] vt = new int[coordstext.length - 1];
						int[] vn = new int[coordstext.length - 1];
						
						for (int i = 1;i < coordstext.length;i++) {
							String fixstring = coordstext[i].replaceAll("//","/0/");
							String[] tempstring = fixstring.split("/");
							v[i-1] = Integer.valueOf(tempstring[0]).intValue();
							if (tempstring.length > 1) {
								vt[i-1] = Integer.valueOf(tempstring[1]).intValue();
							} else {
								vt[i-1] = 0;
							}
							if (tempstring.length > 2) {
								vn[i-1] = Integer.valueOf(tempstring[2]).intValue();
							} else {
								vn[i-1] = 0;
							}
						}
						faces.add(v);
						facestexs.add(vt);
						facesnorms.add(vn);
					}
				}
			}
			
		} catch (IOException e) {
			System.out.println("Failed to read file: " + br.toString());
			//System.exit(0);			
		} catch (NumberFormatException e) {
			System.out.println("Malformed OBJ (on line " + linecounter + "): " + br.toString() + "\r \r" + e.getMessage());
			//System.exit(0);
		}		
	}
	
	private void centerit() {
		float xshift = (rightpoint-leftpoint) /2f;
		float yshift = (toppoint - bottompoint) /2f;
		float zshift = (nearpoint - farpoint) /2f;
		
		for (int i=0; i < vertexsets.size(); i++) {
			float[] coords = new float[3];
			
			coords[0] = ((float[])(vertexsets.get(i)))[0] - leftpoint - xshift;
			coords[1] = ((float[])(vertexsets.get(i)))[1] - bottompoint - yshift;
			coords[2] = ((float[])(vertexsets.get(i)))[2] - farpoint - zshift;
			
			vertexsets.set(i,coords); // = coords;
		}
		
		// Should update bounding coordinates now
		
	}
	
	public float getXWidth() {
		float returnval = 0;
		returnval = rightpoint - leftpoint;
		return returnval;
	}
	
	public float getYHeight() {
		float returnval = 0;
		returnval = toppoint - bottompoint;
		return returnval;
	}
	
	public float getZDepth() {
		float returnval = 0;
		returnval = nearpoint - farpoint;
		return returnval;
	}
	
	public int numpolygons() {
		return numpolys;
	}
	
	public void opengldrawtolist() {
		
		this.objectlist = GL11.glGenLists(1);
		
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GL11.glNewList(objectlist,GL11.GL_COMPILE);
		for (int i=0;i<faces.size();i++) {
			int[] tempfaces = (int[])(faces.get(i));
			int[] tempfacesnorms = (int[])(facesnorms.get(i));
			int[] tempfacestexs = (int[])(facestexs.get(i));
			
			//// Quad Begin Header ////
			int polytype;
			if (tempfaces.length == 3) {
				polytype = GL11.GL_TRIANGLES;
			} else if (tempfaces.length == 4) {
				polytype = GL11.GL_QUADS;
			} else {
				polytype = GL11.GL_POLYGON;
			}
			GL11.glBegin(polytype);
			////////////////////////////
			
			for (int w=0;w<tempfaces.length;w++) {
				if (tempfacesnorms[w] != 0) {
					float normtempx = ((float[])vertexsetsnorms.get(tempfacesnorms[w] - 1))[0];
					float normtempy = ((float[])vertexsetsnorms.get(tempfacesnorms[w] - 1))[1];
					float normtempz = ((float[])vertexsetsnorms.get(tempfacesnorms[w] - 1))[2];
					//GL11.glNormal3f(normtempx, normtempy, normtempz);
					GL11.glNormal3f(normtempx, normtempy, normtempz);
				}
				
				if (tempfacestexs[w] != 0) {
					float textempx = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[0];
					float textempy = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[1];
					float textempz = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[2];
					//GL11.glTexCoord3f(textempx,1f-textempy,textempz);
					//System.out.println( "tx: " + textempx + " " + ( 1f-textempy ) + " " + textempz);
					GL11.glTexCoord2f(textempx,1f-textempy);
				}
				
				float tempx = ((float[])vertexsets.get(tempfaces[w] - 1))[0];
				float tempy = ((float[])vertexsets.get(tempfaces[w] - 1))[1];
				float tempz = ((float[])vertexsets.get(tempfaces[w] - 1))[2];
				GL11.glVertex3f(tempx,tempy,tempz);
				//System.out.println( "v: " + tempx + " " + tempy + " " + tempz );
			}
			
			
			//// Quad End Footer /////
			GL11.glEnd();
			///////////////////////////
			
			
		}
		GL11.glEndList();
	}
	
	public void opengldraw() {
		GL11.glCallList(objectlist);
	}
	
	public float[] trimeshVertices( ) {
		return trimeshVertices( new float[]{ 1.0f, 1.0f,1.0f } );
	}
	
	// These just go ahead and assume data is setup as triangles. 
	public float[] trimeshVertices( float[] scale ) {
		float[] Vertices = new float[vertexsets.size()*3];
		
		for( int k = 0; k < vertexsets.size(); k++ ) {
			final float[] v = vertexsets.get(k);
			Vertices[ k * 3 + 0 ] = scale[0] * v[0];
			Vertices[ k * 3 + 1 ] = scale[1] * v[1];
			Vertices[ k * 3 + 2 ] = scale[2] * v[2];
		}
		//System.out.println( "trimeshVertices " + Vertices.length );
		return Vertices;
	}
	
	public int[] trimeshIndices() {		
	    int[] Indices = new int[faces.size()*3];
	    
	    for( int k = 0; k < faces.size(); k++ ) {
			final int[] f = faces.get(k);
			// index system is rooted at 1
			Indices[ k * 3 + 0 ] = f[0] - 1;
			Indices[ k * 3 + 1 ] = f[1] - 1;
			Indices[ k * 3 + 2 ] = f[2] - 1;
		}
		
	    //System.out.println( "trimeshIndices " + Indices.length );
		return Indices;
	}
	
	public void rescaleMesh( float w, float h, float d, boolean withGraphics ) {		
		
		for( int k = 0; k < vertexsets.size(); k++ ) {
			float[] v = vertexsets.get(k);
			v[ 0 ] = w * v[0];
			v[ 1 ] = h * v[1];
			v[ 2 ] = d * v[2];
			vertexsets.set(k, v);
		}
		//System.out.println( "trimeshVertices " + Vertices.length );
		
		toppoint *= h;		// y+
		bottompoint *= h;	// y-
		leftpoint *= w;		// x-
		rightpoint *= w;	// x+
		farpoint *= d;		// z-
		nearpoint *= d;		// z+
		
		if( withGraphics )
			opengldrawtolist();
		// Regen display list
	}

	public void destroy() {
		//GL11.glDeleteLists(objectlist,1);
		vertexsets.clear();		
		vertexsetsnorms.clear();
		vertexsetstexs.clear();
		faces.clear();
		facestexs.clear();
		facesnorms.clear();
	}
	
	public int numVertices() {
		return vertexsets.size();
	}
	
	public float[] getVertex( int idx ) {
		return vertexsets.get( idx );
	}
	
	public void setVertex( int idx, float[] newV ) {
		vertexsets.set( idx , newV );
	}
	
	public float[] getVertexNorm( int idx ) {
		return vertexsetsnorms.get( idx );
	}
	
	public void setVertexNorm( int idx, float[] newV ) {
		vertexsetsnorms.set( idx,  newV );
	}
	
	public float[] getVertexTex( int idx ) {
		return vertexsetstexs.get( idx );
	}
	
	public void setVertexTex( int idx, float[] newV ) {
		vertexsetstexs.set( idx, newV );
	}
	
	public int numFaces() {
		return faces.size();
	}
	
	public int[] getFace( int idx ) {
		return faces.get(idx);
	}
	
	public void setFace( int idx, int[] newV ) {
		faces.set( idx, newV );
	}
	
	public int[] getFaceTex( int idx ) {
		return facestexs.get(idx);
	}
	
	public void setFaceTex( int idx, int[] newV ) {
		facestexs.set( idx, newV );
	}
	
	public int[] getFaceNorm( int idx ) {
		return facesnorms.get(idx);
	}
	
	public float[] getFaceNormal( int idx ) {
		float[] n = new float[3];
		float[] n1 = vertexsetsnorms.get( facesnorms.get(idx)[0] );
		float[] n2 = vertexsetsnorms.get( facesnorms.get(idx)[1] );
		float[] n3 = vertexsetsnorms.get( facesnorms.get(idx)[2] );
		
		n[0] = n1[0] + n2[0] + n3[0];
		n[1] = n1[1] + n2[1] + n3[1];
		n[2] = n1[2] + n2[2] + n3[2];
		
		return n;
	}
	
	public void setFaceNorm( int idx, int[] newV ) {
		facesnorms.set( idx, newV );
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		 out.defaultWriteObject();
	}
		     
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
	
	public void pairwiseVertexDistanceToFile( String filename, double cutoff ) throws IOException {
		FileWriter w = new FileWriter( filename );
		BufferedWriter bw = new BufferedWriter(w);
		
		for( int k = 0; k < vertexsets.size() - 1; k++ ) {
			float[] v1 = vertexsets.get(k);
			for( int h = k+1; h < vertexsets.size(); h++ ) {
				float[] v2 = vertexsets.get(h);
				double d = Math.sqrt( Math.pow( v1[0] - v2[0], 2 ) + Math.pow( v1[1] - v2[1], 2 ) + Math.pow( v1[2] - v2[2], 2 ) );
				if( d < cutoff )
					bw.write( k + "\t" + h + "\t" + d + "\n" );
			}
			if( ( k % ( (int) vertexsets.size() / 100 ) ) == 0 ) 
				System.out.println( k + " of " + vertexsets.size() );
			//bw.flush();
		}
		
		bw.close();
		w.close();
	}
	
	public double intersectRayMesh( float[] direction, float[] point, int sourceFace, double offsetR ) {
		double dist = Double.POSITIVE_INFINITY;
		/*direction[0] = -direction[0];
		direction[1] = -direction[1];
		direction[2] = -direction[2];*/
		
		Vector3f dir = new Vector3f( direction[0], direction[1], direction[2] );
		//Vector3f w0 = new Vector3f( point[0], point[1], point[2] );
		
		Vector3f w0 = new Vector3f( (float) (point[0] + offsetR * direction[0]),
									(float) (point[1] + offsetR * direction[1]),
									(float) (point[2] + offsetR * direction[2]) );
		
		for( int k = 0; k < faces.size(); k++ ) {
			if( k != sourceFace ) {
			//if( k % ( faces.size() / 100 ) == 0 )
			//	System.out.println( "." + k );
			//Point3f I = new Point3f();
	        Vector3f    u, v, n;
	        Vector3f    w;
	        float     r, a, b;
	        
	        float[] p1v = getVertex( faces.get(k)[0] - 1  );
	        float[] p2v = getVertex( faces.get(k)[1] - 1 );
	        float[] p3v = getVertex( faces.get(k)[2] - 1 );
	        Vector3f p1 = new Vector3f( p1v[0], p1v[1], p1v[2] );
	        u = new Vector3f( p2v[0], p2v[1], p2v[2] );
	        v = new Vector3f( p3v[0], p3v[1], p3v[2] );
	        
	        Vector3f.sub( u, p1, u );
	        Vector3f.sub( v, p1, v );
	        n = new Vector3f(); // cross product
	        Vector3f.cross(u, v, n);
	        
	        if (n.length() != 0) {

		        //w0.sub(T.getPointOne());
	        	
	        	Vector3f rw0 = Vector3f.sub( w0, p1, null );
		        //a = -(new Vector3f(n).dot(w0));
	        	a = -( Vector3f.dot(n,rw0) );
		        //b = new Vector3f(n).dot(dir);
	        	b = ( Vector3f.dot( n, dir ) );
		        
		        if ((float)Math.abs(b) > 0.000001 ) {// small number check
		        
			        r = a / b;
			        if (r >= 0.0) {
				        
			        	// intersection point
				        //I = new Point3f(R.getStart());
				        //I.x += r * dir.x;
				        //I.y += r * dir.y;
				        //I.z += r * dir.z;
			        	
		    	        float[] pc = new float[3];
		    	        pc[0] = ( p1v[0] + p2v[0] + p3v[0] ) / 3;
		    	        pc[1] = ( p1v[1] + p2v[1] + p3v[1] ) / 3;
		    	        pc[2] = ( p1v[2] + p2v[2] + p3v[2] ) / 3;
		    	        
		        		float d = (float) Math.sqrt( Math.pow( pc[0] - point[0], 2 ) +
		        							 Math.pow( pc[1] - point[1], 2 ) + 
		        							 Math.pow( pc[2] - point[2], 2 ) );
				        
			        	if( d < dist ) {
			        		//System.out.println( "Matching face for " + sourceFace + " is " + k );
			        		//System.out.println( "Source direction " + dir );
			        		//System.out.println( "source point " + w0 );
			        		//System.out.println( "p1 " + p1 );
			        		//System.out.println( "r " + r );


			        		dist = d;
			        		//System.out.println( "d " + d );
			        	}
			        }
		        }
	        }
			}
		}
		if( dist == Double.POSITIVE_INFINITY )
			return -1;
		else	
			return dist;
	}
	
	public int closestVertexIndex( float[] point ) {
		double dist = Double.POSITIVE_INFINITY;
		int closestVertex = -1;
				
		for( int k = 0; k < vertexsets.size(); k++ ) {
			float[] kVertex = vertexsets.get(k);
			float[] dVector = { ( point[0] - kVertex[0] ), ( point[1] - kVertex[1] ), ( point[2] - kVertex[2] ) };
			double kDist = Math.sqrt( Math.pow( (double)dVector[0], 2.0 ) + Math.pow( (double)dVector[1], 2.0 ) + Math.pow( (double)dVector[2], 2.0 ) );
			
			if( kDist < dist ) {
				dist = kDist;
				closestVertex = k;
			}
			
		}
		
		return closestVertex;
	}
	
	public int closestVertexIndex( float[] point, float maximumDistance ) {
		double dist = Double.POSITIVE_INFINITY;
		int closestVertex = -1;
				
		for( int k = 0; k < vertexsets.size(); k++ ) {
			float[] kVertex = vertexsets.get(k);
			float[] dVector = { ( point[0] - kVertex[0] ), ( point[1] - kVertex[1] ), ( point[2] - kVertex[2] ) };
			double kDist = Math.sqrt( Math.pow( (double)dVector[0], 2.0 ) + Math.pow( (double)dVector[1], 2.0 ) + Math.pow( (double)dVector[2], 2.0 ) );
			
			if( ( kDist < maximumDistance ) && ( kDist < dist ) ) {
				dist = kDist;
				closestVertex = k;
			}
			
		}
		
		return closestVertex;
	}
}
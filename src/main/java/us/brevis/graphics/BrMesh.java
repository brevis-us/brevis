

package us.brevis.graphics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import org.joml.Vector3f;
import sc.iview.vector.JOMLVector3;
import sc.iview.vector.Vector3;


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
	public ArrayList<float[]> vertexsets = new ArrayList<>(); // Vertex Coordinates
	public ArrayList<float[]> vertexsetsnorms = new ArrayList<>(); // Vertex Coordinates Normals
	public ArrayList<float[]> vertexsetstexs = new ArrayList<>(); // Vertex Coordinates Textures
	public ArrayList<int[]> faces = new ArrayList<>(); // Array of Faces (vertex sets)
	public ArrayList<int[]> facestexs = new ArrayList<>(); // Array of of Faces textures
	public ArrayList<int[]> facesnorms = new ArrayList<>(); // Array of Faces normals

	private int numpolys = 0;
	
	//// Statisitcs for drawing ////
	public float toppoint = 0;		// y+
	public float bottompoint = 0;	// y-
	public float leftpoint = 0;		// x-
	public float rightpoint = 0;	// x+
	public float farpoint = 0;		// z-
	public float nearpoint = 0;		// z+
	
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
	
	public BrMesh(BufferedReader ref, boolean centerit ) {
		loadobject(ref);
		if (centerit) {
			centerit();
		}
		numpolys = faces.size();
	}

	public BrMesh() {
	}

	public BrMesh(List<Vector3> verts) {
		cleanup();

		boolean firstpass = true;

		for( Vector3 v : verts ) {
			float[] coords = new float[3];
			coords[0] = v.xf();
			coords[1] = v.yf();
			coords[2] = v.zf();
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
		List<Vector3> normals = new ArrayList<>();
		Vector3 p1, p2, p3, edge1, edge2, veccross;
		for( int k = 0; k < verts.size(); k+=3 ) {
			p1 = verts.get(k).copy();
			p2 = verts.get(k+1).copy();
			p3 = verts.get(k+2).copy();

			edge1 = p1.minus(p2);
			edge2 = p1.minus(p3);

			veccross = edge1.cross(edge2);

			vertexsetsnorms.add( veccross.asFloatArray() );
			vertexsetsnorms.add( veccross.asFloatArray() );
			vertexsetsnorms.add( veccross.asFloatArray() );
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
	
	public void rescaleMesh( float w, float h, float d ) {
		for( int k = 0; k < vertexsets.size(); k++ ) {
			float[] v = vertexsets.get(k);
			v[ 0 ] = w * v[0];
			v[ 1 ] = h * v[1];
			v[ 2 ] = d * v[2];
			vertexsets.set(k, v);
		}
		
		toppoint *= h;		// y+
		bottompoint *= h;	// y-
		leftpoint *= w;		// x-
		rightpoint *= w;	// x+
		farpoint *= d;		// z-
		nearpoint *= d;		// z+
	}

	public void destroy() {
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
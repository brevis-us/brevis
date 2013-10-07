package brevis.graphics;

import javax.vecmath.Vector3d;

/*public class BrMesh {
	public float[] verts;
	public float[] col;
	public int[] idx;
	
	public int numVerts() {
		return verts.length;
	}
	
	public int numCol() {
		return col.length;
	}
	
	public int numIdx() {
		return idx.length;
	}


}*/

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

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

public class BrMesh {
	
	
	public ArrayList vertexsets = new ArrayList(); // Vertex Coordinates
	public ArrayList vertexsetsnorms = new ArrayList(); // Vertex Coordinates Normals
	public ArrayList vertexsetstexs = new ArrayList(); // Vertex Coordinates Textures
	public ArrayList faces = new ArrayList(); // Array of Faces (vertex sets)
	public ArrayList facestexs = new ArrayList(); // Array of of Faces textures
	public ArrayList facesnorms = new ArrayList(); // Array of Faces normals
	
	private int objectlist;
	private int numpolys = 0;
	
	//// Statisitcs for drawing ////
	public float toppoint = 0;		// y+
	public float bottompoint = 0;	// y-
	public float leftpoint = 0;		// x-
	public float rightpoint = 0;	// x+
	public float farpoint = 0;		// z-
	public float nearpoint = 0;		// z+
	
	public BrMesh(BufferedReader ref, boolean centerit) {
		loadobject(ref);
		if (centerit) {
			centerit();
		}
		opengldrawtolist();
		numpolys = faces.size();
		cleanup();
	}
	
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
						float[] coords = new float[4];
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
			float[] coords = new float[4];
			
			coords[0] = ((float[])(vertexsets.get(i)))[0] - leftpoint - xshift;
			coords[1] = ((float[])(vertexsets.get(i)))[1] - bottompoint - yshift;
			coords[2] = ((float[])(vertexsets.get(i)))[2] - farpoint - zshift;
			
			vertexsets.set(i,coords); // = coords;
		}
		
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
					GL11.glNormal3f(normtempx, normtempy, normtempz);
				}
				
				if (tempfacestexs[w] != 0) {
					float textempx = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[0];
					float textempy = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[1];
					float textempz = ((float[])vertexsetstexs.get(tempfacestexs[w] - 1))[2];
					GL11.glTexCoord3f(textempx,1f-textempy,textempz);
				}
				
				float tempx = ((float[])vertexsets.get(tempfaces[w] - 1))[0];
				float tempy = ((float[])vertexsets.get(tempfaces[w] - 1))[1];
				float tempz = ((float[])vertexsets.get(tempfaces[w] - 1))[2];
				GL11.glVertex3f(tempx,tempy,tempz);
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
	
}
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

package brevis.graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import brevis.BrObject;
import brevis.BrShape;

public class Basic3D {
    private static final float LIGHTX = 1.0f;
    private static final float LIGHTY = 0.4f;
    private static final float SHADOW_INTENSITY = 0.65f;
    
    //static float lightPos[] = { 0.0f, 5.0f,-4.0f, 1.0f};           // Light Position                                                                               
    static float lightPos[] = { 0.0f, 1.0f, 0.0f, 1.0f};           // Light Position                                                                               
    static float lightAmb[] = { 0.2f, 0.2f, 0.2f, 1.0f};           // Ambient Light Values                                                                         
    static float lightDif[] = { 0.6f, 0.6f, 0.6f, 1.0f};           // Diffuse Light Values                                                                         
    static float lightSpc[] = {-0.2f, -0.2f, -0.2f, 1.0f};         // Specular Light Values                                                                        
    static ByteBuffer byteBuffer;
    static ByteBuffer floatBuffer;
    static float matAmb[] = {0.4f, 0.4f, 0.4f, 1.0f};              // Material - Ambient Values                                                                    
    static float matDif[] = {0.2f, 0.6f, 0.9f, 1.0f};              // Material - Diffuse Values                                                                    
    static float matSpc[] = {0.0f, 0.0f, 0.0f, 1.0f};              // Material - Specular Values                                                                   
    static float matShn[] = {0.0f, 0.0f, 0.0f, 0.0f};                                // Material - Shininess                                                       
    
    static public int width = 640;
    static public int height = 480;        
    
    private static final FloatBuffer light_position =  BufferUtils.createFloatBuffer(4);
	private static final FloatBuffer light_ambient = BufferUtils.createFloatBuffer(4);
	private static final FloatBuffer light_diffuse = BufferUtils.createFloatBuffer(4);
	private static final FloatBuffer light_specular = BufferUtils.createFloatBuffer(4);
	static {
		//light_position.put(new float[] { LIGHTX, LIGHTY, 1.0f, 0.0f }).flip();
		light_position.put(new float[] { 50.0f, 200.0f, 50.0f, 0.0f }).flip();
		light_ambient.put(new float[]{ 0.5f, 0.5f, 0.5f, 1.0f }).flip();
		light_diffuse.put(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }).flip();
		light_specular.put(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }).flip();
	}
    
    static private float[] view_xyz = new float[3];	// position x,y,z
	static private float[] view_hpr = new float[3];	// heading, pitch, roll (degrees)
    
	static BrLight light1 = new BrLight();// should probably have a light array
	static BrSky sky;
	//static BrCamera displayCamera;// This is the BrCamera that gets the main GL context
	
	public static void lightMove( int lightNum, float[] position ) {
		//light_position.put( position ).flip();
		light1.setPosition( position );
	}	
	
	// a good bit from ode4j
    static public void initGL() {
        
    	view_xyz[0] = 2;
		view_xyz[1] = 0;
		view_xyz[2] = 1;
		view_hpr[0] = 180;
		view_hpr[1] = 0;
		view_hpr[2] = 0;
		
		float fov = 45;
		float near = 0.1f;
		float far = 3000;
		//displayCamera = new BrCamera( view_xyz[0], view_xyz[1], view_xyz[2], view_hpr[0], view_hpr[1], view_hpr[2], fov, width, height, near, far );
        
		//light1.setPosition( new float[]{ 1.0f, 0.4f, 1.0f, 0.0f } );
		light1.setPosition(new float[] { 50.0f, 200.0f, 50.0f, 0.0f }  );
		
        GL11.glShadeModel(GL11.GL_SMOOTH);                            // Enable Smooth Shading
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);               // Black Background
        GL11.glClearDepth(1.0f);                                 // Depth Buffer Setup
        GL11.glClearStencil(0);                                  // Stencil Buffer Setup
        GL11.glEnable(GL11.GL_DEPTH_TEST);                            // Enables Depth Testing
        GL11.glDepthFunc(GL11.GL_LEQUAL);                             // The Type Of Depth Testing To Do
        //GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);  // Really Nice Perspective Calculations
        
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_FASTEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_FASTEST);
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        
        //floatBuffer = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asFloatBuffer();
        //byteBuffer = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder());
        
        /*floatBuffer = ByteBuffer.allocateDirect(64);
        floatBuffer.order(ByteOrder.nativeOrder());
        byteBuffer = ByteBuffer.allocateDirect(16);
        byteBuffer.order(ByteOrder.nativeOrder());*/
        /*
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, (FloatBuffer)floatBuffer.put(lightPos).flip());        // Set Light1 Position
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, (FloatBuffer)floatBuffer.put(lightAmb).flip());         // Set Light1 Ambience
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, (FloatBuffer)floatBuffer.put(lightDif).flip());         // Set Light1 Diffuse
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, (FloatBuffer)floatBuffer.put(lightSpc).flip());        // Set Light1 Specular
        GL11.glEnable(GL11.GL_LIGHT1);                                // Enable Light1
        GL11.glEnable(GL11.GL_LIGHTING);                              // Enable Lighting

        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, (FloatBuffer)floatBuffer.put(matAmb).flip());         // Set Material Ambience
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, (FloatBuffer)floatBuffer.put(matDif).flip());         // Set Material Diffuse
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, (FloatBuffer)floatBuffer.put(matSpc).flip());        // Set Material Specular
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SHININESS, (FloatBuffer)floatBuffer.put(matShn).flip());       // Set Material Shininess
*/
        /*
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, (FloatBuffer)byteBuffer.asFloatBuffer().put(lightPos).flip());        // Set Light1 Position         
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, (FloatBuffer)byteBuffer.asFloatBuffer().put(lightAmb).flip());         // Set Light1 Ambience         
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, (FloatBuffer)byteBuffer.asFloatBuffer().put(lightDif).flip());         // Set Light1 Diffuse          
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, (FloatBuffer)byteBuffer.asFloatBuffer().put(lightSpc).flip());        // Set Light1 Specular         
        GL11.glEnable(GL11.GL_LIGHT1);                                // Enable Light1                                                                      
        GL11.glEnable(GL11.GL_LIGHTING);                              // Enable Lighting                                                                    

        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, (FloatBuffer)byteBuffer.asFloatBuffer().put(matAmb).flip());         // Set Material Ambience       
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, (FloatBuffer)byteBuffer.asFloatBuffer().put(matDif).flip());         // Set Material Diffuse        
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, (FloatBuffer)byteBuffer.asFloatBuffer().put(matSpc).flip());        // Set Material Specular       
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SHININESS, (FloatBuffer)byteBuffer.asFloatBuffer().put(matShn).flip());       // Set Material Shininess        
        */
        //GL11.glCullFace(GL11.GL_BACK);                                // Set Culling Face To Back Face
        //GL11.glEnable(GL11.GL_CULL_FACE);                             // Enable Culling
        
        light1.enable();
        sky = new BrSky();
        
        GL11.glClearColor(0.1f, 1.0f, 0.5f, 1.0f);               // Set Clear Color (Greenish Color)

/*        q = new Sphere();                               // Initialize Quadratic
        q.setNormals(GL11.GL_SMOOTH);                   // Enable Smooth Normal Generation
        q.setTextureFlag(false);                        // Disable Auto Texture Coords
*/
        GL11.glViewport(0,0,width,height);                           // Reset The Current Viewport

        GL11.glMatrixMode(GL11.GL_PROJECTION);                            // Select The Projection Matrix
        GL11.glLoadIdentity();                                       // Reset The Projection Matrix

        // Calculate The Aspect Ratio Of The Window
        /*GLU.gluPerspective(45.0f,
                (float) displayMode.getWidth() / (float) displayMode.getHeight(),
                0.05f,100.0f);*/
        
        GLU.gluPerspective(45.0f,
                (float) width / (float) height,
                0.05f, 100.0f);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);                             // Select The Modelview Matrix
        GL11.glLoadIdentity();                                       // Reset The Modelview Matrix    	
    }
	
    static public void initFrame( BrCamera displayCamera ) {
    	GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );
    	
    	//GL11.glEnable( GL11.GL_LIGHTING );
    	//GL11.glEnable( GL11.GL_LIGHT0 );
    	
    	//light1.enable();
    	
    	GL11.glDisable (GL11.GL_TEXTURE_2D);
		GL11.glDisable (GL11.GL_TEXTURE_GEN_S);
		GL11.glDisable (GL11.GL_TEXTURE_GEN_T);
		GL11.glShadeModel (GL11.GL_FLAT);
		GL11.glEnable (GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc (GL11.GL_LESS);
		//GL11.glEnable (GL11.GL_CULL_FACE);
		//GL11.glCullFace (GL11.GL_BACK);
		//GL11.glFrontFace (GL11.GL_CCW);

		// setup viewport
		//displayCamera.setupFrame();		
		
		/*GL11.glViewport (0,0,width,height);
		GL11.glMatrixMode (GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		final float vnear = 0.1f;
		final float vfar = 100.0f;
		final float k = 0.8f;     // view scale, 1 = +/- 45 degrees
		if (width >= height) {
			float k2 = (float)height/(float)width;
			GL11.glFrustum (-vnear*k,vnear*k,-vnear*k*k2,vnear*k*k2,vnear,vfar);
		}
		else {
			float k2 = (float)width/(float)height;
			GL11.glFrustum (-vnear*k*k2,vnear*k*k2,-vnear*k,vnear*k,vnear,vfar);
		}*/
		
		// setup lights. it makes a difference whether this is done in the
		// GL_PROJECTION matrix mode (lights are scene relative) or the
		// GL_MODELVIEW matrix mode (lights are camera relative, bad!).
//				static GLfloat light_ambient[] = { 0.5, 0.5, 0.5, 1.0 };
//				static GLfloat light_diffuse[] = { 1.0, 1.0, 1.0, 1.0 };
//				static GLfloat light_specular[] = { 1.0, 1.0, 1.0, 1.0 };

		/*GL11.glLight (GL11.GL_LIGHT0, GL11.GL_AMBIENT, light_ambient);
		GL11.glLight (GL11.GL_LIGHT0, GL11.GL_DIFFUSE, light_diffuse);
		GL11.glLight (GL11.GL_LIGHT0, GL11.GL_SPECULAR, light_specular);*/
		
		light1.enable();
		
		GL11.glColor3f (1.0f, 1.0f, 1.0f);

		// clear the window
		GL11.glClearColor (0.5f ,0.5f ,0.5f ,0);
		GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		//sky.draw( displayCamera.x, displayCamera.y, displayCamera.z );
		
		// snapshot camera position (in MS Windows it is changed by the GUI thread)
		//float[] view2_xyz=view_xyz.clone();
		//float[] view2_hpr=view_hpr.clone();
//				memcpy (view2_xyz,view_xyz);//,sizeof(float)*3);
//				memcpy (view2_hpr,view_hpr);//,sizeof(float)*3);

		// go to GL_MODELVIEW matrix mode and set the camera
		GL11.glMatrixMode (GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		displayCamera.setupFrame();		
		//setCamera (view2_xyz[0],view2_xyz[1],view2_xyz[2],
		//		view2_hpr[0],view2_hpr[1],view2_hpr[2]);

		// set the light position (for some reason we have to do this in model view.
//				static GLfloat light_position[] = { LIGHTX, LIGHTY, 1.0, 0.0 };
		//GL11.glLight (GL11.GL_LIGHT0, GL11.GL_POSITION, light_position);
		//GL11.glLight (GL11.GL_LIGHT0, GL11.GL_POSITION, light_position);
    	
		//old
		//sky.draw( displayCamera.x, displayCamera.y, displayCamera.z );
		
		sky.draw( displayCamera );
				
    	//GL11.glEnable(GL11.GL_TEXTURE_2D);
    	//GL11.glBindTexture(GL11.GL_TEXTURE_2D, sky.textures.get(0).getTextureID() );
		//drawBox( 500, 500, 500 );
		
    }
    
	static public void drawBox(float w, float h, float d) {
		GL11.glBegin(GL11.GL_QUADS);
			// Front
			GL11.glNormal3f(0f, 0f, 1f);
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-w, -h,  d);  // Bottom Left
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( w, -h,  d);  // Bottom Right
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( w,  h,  d);  // Top Right
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-w,  h,  d);  // Top Left
		    // Back 
			GL11.glNormal3f(0f, 0f, -1f);
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-w, -h, -d);  // Bottom Right
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-w,  h, -d);  // Top Right
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( w,  h, -d);  // Top Left
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( w, -h, -d);  // Bottom Left 
		    // Top 
			GL11.glNormal3f(0f, -1f, 0f);
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-w,  h, -d);  // Top Left
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-w,  h,  d);  // Bottom Left 
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( w,  h,  d);  // Bottom Right 
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( w,  h, -d);  // Top Right 
		    // Bottom 
			GL11.glNormal3f(0f, 1f, 0f);
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-w, -h, -d);  // Top Right
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( w, -h, -d);  // Top Left 
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( w, -h,  d);  // Bottom Left 
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-w, -h,  d);  // Bottom Right
		    // Right 
			GL11.glNormal3f(-1f, 0f, 0f);
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( w, -h, -d);  // Bottom Right
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( w,  h, -d);  // Top Right 
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( w,  h,  d);  // Top Left 
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( w, -h,  d);  // Bottom Left 
		    // Left 
			GL11.glNormal3f(1f, 0f, 0f);
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-w, -h, -d);  // Bottom Left
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-w, -h,  d);  // Bottom Right 
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-w,  h,  d);  // Top Right
		    GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-w,  h, -d);  // Top Left 
		
		    GL11.glEnd();
	
		

		//System.out.println( "I wish I was printing a box of " + w + "," + h + "," + d + " dimensions ");
	}
	
	static public void drawSphere(float r, int stack, int string) {

		Sphere s = new Sphere();
	
		s.draw(r, stack, string);

	}

	static public void drawCylinder(float baseRadius, float topRadius, float height, int slices, int stacks) {

		Cylinder c = new Cylinder();
	
		c.draw(baseRadius, topRadius, height, slices, stacks);

	}
	
	// nehe lesson 27
    private static void vMatMult(float[] minv, double[] lp) {
        double res[] = new double[4];                                     // Hold Calculated Results
        res[0]=minv[ 0]*lp[0]+minv[ 4]*lp[1]+minv[ 8]*lp[2]+minv[12]*lp[3];
        res[1]=minv[ 1]*lp[0]+minv[ 5]*lp[1]+minv[ 9]*lp[2]+minv[13]*lp[3];
        res[2]=minv[ 2]*lp[0]+minv[ 6]*lp[1]+minv[10]*lp[2]+minv[14]*lp[3];
        res[3]=minv[ 3]*lp[0]+minv[ 7]*lp[1]+minv[11]*lp[2]+minv[15]*lp[3];
        lp[0]=res[0];                                        // Results Are Stored Back In v[]
        lp[1]=res[1];
        lp[2]=res[2];
        lp[3]=res[3];                                        // Homogenous Coordinate
    }	    

    // ode4j-sdk drawstuff
    private static float[] color = {0,0,0,0};       // current r,g,b,alpha color                                                                                                                                                          
    //private static DS_TEXTURE_NUMBER tnum = DS_TEXTURE_NUMBER.DS_NONE;                      // current texture number                                                                                                                     
    
    public static void setCamera (float x, float y, float z, float h, float p, float r)
    {
            //GL11.glMatrixMode (GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            //GL11.glRotatef (90, 0,0,1);
            //GL11.glRotatef (90, 0,1,0);
            /*GL11.glRotatef (r, 1,0,0);
            GL11.glRotatef (p, 0,1,0);
            GL11.glRotatef (-h, 0,0,1);
            GL11.glTranslatef (-x,-y,-z);*/
            GL11.glRotatef (r, 1,0,0);
            GL11.glRotatef (p, 0,1,0);
            GL11.glRotatef (-h, 0,0,1);
            GL11.glTranslatef (-x,-y,-z);
    }   
    
    // ow the static pain
    private static FloatBuffer light_ambient2 = BufferUtils.createFloatBuffer(4);
    private static FloatBuffer light_diffuse2 = BufferUtils.createFloatBuffer(4);
    private static FloatBuffer light_specular2 = BufferUtils.createFloatBuffer(4);
    public static void setColor (float r, float g, float b, float alpha)
    {
            //GLfloat light_ambient[4],light_diffuse[4],light_specular[4];                                                                                                                                                                
            light_ambient2.put( new float[]{ r*0.3f, g*0.3f, b*0.3f, alpha }).flip();
            light_diffuse2.put( new float[]{ r*0.7f, g*0.7f, b*0.7f, alpha }).flip();
            light_specular2.put( new float[]{ r*0.2f, g*0.2f, b*0.2f, alpha }).flip();
            GL11.glMaterial (GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT, light_ambient2);
            GL11.glMaterial (GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE, light_diffuse2);
            GL11.glMaterial (GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR, light_specular2);
            GL11.glMaterialf (GL11.GL_FRONT_AND_BACK, GL11.GL_SHININESS, 5.0f);
    }
    
    private FloatBuffer matrixF = BufferUtils.createFloatBuffer(16);

//  static void setTransform (final float pos[3], final float R[12])                                                                                                                                                                      
    public void setTransform (final float[] pos, final float[] R)
    {
            //GLfloat                                                                                                                                                                                                                     
            float[] matrix=new float[16];
            matrix[0]=R[0];
            matrix[1]=R[4];
            matrix[2]=R[8];
            matrix[3]=0;
            matrix[4]=R[1];
            matrix[5]=R[5];
            matrix[6]=R[9];
            matrix[7]=0;
            matrix[8]=R[2];
            matrix[9]=R[6];
            matrix[10]=R[10];
            matrix[11]=0;
            matrix[12]=pos[0];
            matrix[13]=pos[1];
            matrix[14]=pos[2];
            matrix[15]=1;
            matrixF.put(matrix);
            matrixF.flip();
            GL11.glPushMatrix();
            GL11.glMultMatrix (matrixF);
    }
    
    private FloatBuffer matrixSST = BufferUtils.createFloatBuffer(16);
    private void setShadowTransform( float lightX, float lightY )
    {
            //GLfloat                                                                                                                                                                                                                     
            float[] matrix=new float[16];
            for (int i=0; i<16; i++) matrix[i] = 0;
            matrix[0]=1;
            matrix[5]=1;
            matrix[8]=-lightX;
            matrix[9]=-lightY;
            matrix[15]=1;
            matrixSST.put( matrix );
//          for (int i=0; i < 16; i++) matrixSST.put(i, 0);                                                                                                                                                                               
//          matrixSST.put(0, 1);                                                                                                                                                                                                          
//          matrixSST.put(5, 1);                                                                                                                                                                                                          
//          matrixSST.put(8, -LIGHTX);                                                                                                                                                                                                    
//          matrixSST.put(9, -LIGHTY);                                                                                                                                                                                                    
//          matrixSST.put(15, 1);                                                                                                                                                                                                         
            matrixSST.flip();
            GL11.glPushMatrix();
            GL11.glMultMatrix (matrixSST);
    }    
    
    // http://lwjgl.org/forum/index.php?topic=2407.0;wap2
    static public void castShadow( BrMesh o, double lp[] ){
        int i;
        float side;

        //set visual parameter
        /*for (i=0;i<o.numpolygons();i++){
            // chech to see if light is in front or behind the plane (face plane)
            side =  o.planes[i].planeEq.a*lp[0]+
                    o.planes[i].planeEq.b*lp[1]+
                    o.planes[i].planeEq.c*lp[2]+
                    o.planes[i].planeEq.d;
            if (side >0) o.planes[i].visible = true;
                    else o.planes[i].visible = false;
        }*/

        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT
                | GL11.GL_ENABLE_BIT | GL11.GL_POLYGON_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glColorMask(false, false, false, false);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xffffffff);

        // first pass, stencil operation increases stencil value
        GL11.glFrontFace(GL11.GL_CCW);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
        //System.out.println( "castShadow " + o );
        doShadowPass(o, lp);

        // second pass, stencil operation decreases stencil value
        GL11.glFrontFace(GL11.GL_CW);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_DECR);
        doShadowPass(o, lp);

        GL11.glFrontFace(GL11.GL_CCW);
        GL11.glColorMask(true, true, true, true);

        //draw a shadowing rectangle covering the entire screen
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.4f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glStencilFunc(GL11.GL_NOTEQUAL, 0, 0xffffffff);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glVertex3f(-0.1f, 0.1f,-0.10f);
        GL11.glVertex3f(-0.1f,-0.1f,-0.10f);
        GL11.glVertex3f( 0.1f, 0.1f,-0.10f);
        GL11.glVertex3f( 0.1f,-0.1f,-0.10f);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }    
    
    static private void doShadowPass( BrMesh o, double[] lp ){
        int i, j, k, jj;
        Vector3f v3 = new Vector3f();
        Vector3f v4 = new Vector3f();
        float[] p1, p2, p3;
        int[] f1;
        
        //System.out.println( o.numpolygons() + " " + o.faces.size() + " " + o.vertexsets.size() );
        
        for (i=0; i<o.numpolygons();i++){
        	//System.out.println( i + " " + o.faces.get(i) );
        	int[] face = (int[])( o.faces.get(i) );        
            // could check to see if a face is visible before proceeding
        	
        	//GL11.glBegin( GL11.GL_POLYGON );
        	
            for ( j=0; j<face.length; j++ ){                
            	k = face[j];
                if ( k != 0 ){
                    // here we have an edge, we must draw a polygon
                	float[] point = (float[])( o.vertexsets.get(k-1) );
                	GL11.glVertex3f( point[0], point[1], point[2] );
                	
                	f1 = (int[])(o.faces.get(i));//.p[j];
                    jj = (j+1)%3;
                    
                    p1 = o.vertexsets.get(f1[j] - 1);
                    p2 = o.vertexsets.get(f1[jj] - 1);

                    //calculate the length of the vector
                    v3.x = (float) ((p1[0] - lp[0])*100);
                    v3.y = (float) ((p1[1] - lp[1])*100);
                    v3.z = (float) ((p1[2] - lp[2])*100);

                    v4.x = (float) ((p2[0] - lp[0])*100);
                    v4.y = (float) ((p2[1] - lp[1])*100);
                    v4.z = (float) ((p2[2] - lp[2])*100);

                    //draw the polygon
                    GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
                        GL11.glVertex3f(p1[0],
                                    	p1[1],
                                    	p1[2]);
                        GL11.glVertex3f((float) (p1[0] + v3.x),
                                    	(float) (p1[1] + v3.y),
                                    	(float) (p1[2] + v3.z));

                        GL11.glVertex3f(p2[0],p2[1],p2[2]);
                        GL11.glVertex3f((float) (p2[0] + v4.x),
                        				(float) (p2[1] + v4.y),
                        				(float) (p2[2] + v4.z));

                    GL11.glEnd();
                }
            }
            
            //GL11.glEnd();
        }
        
    }   
    
	// some from nehe lesson 27    
	//static public void drawShape( BrObject obj, double xrot, double yrot, double zrot, double xoff, double yoff, double zoff, double[] lp, Vector3d dim ) {
    //static public void drawShape( BrObject obj, double[] lp, Vector3f dim ) {
    static public void drawShape( BrObject obj, Vector3f dim ) {
		//float Minv[] = new float[16];
        //float wlp[] = new float[4];        
		
		/*ByteBuffer byteBuffer;
		ByteBuffer floatBuffer;
		floatBuffer = ByteBuffer.allocateDirect(64);
        floatBuffer.order(ByteOrder.nativeOrder());
        byteBuffer = ByteBuffer.allocateDirect(16);
        byteBuffer.order(ByteOrder.nativeOrder());*/
		
        // Clear Color Buffer, Depth Buffer, Stencil Buffer
        //GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

        // calculate light's position relative to local coordinate system
        // dunno if this is the best way to do it, but it actually works
        // if u find another aproach, let me know ;)

        // we build the inversed matrix by doing all the actions in reverse order
        // and with reverse parameters (notice -xrot, -yrot, -ObjPos[], etc.)
        //GL11.glLoadIdentity();                                   // Reset Matrix
        //GL11.glTranslatef((float) pos.x, (float) pos.y, (float) pos.z);      // Position The Object
        //GL11.glRotatef((float) rot.w, rot.x, rot.y, rot.z);
        //GL11.glRotatef((float) -zrot, 0.0f, 0.0f, 1.0f);                 // Rotate By -yrot On Y Axis
        //GL11.glRotatef((float) -yrot, 0.0f, 1.0f, 0.0f);                 // Rotate By -yrot On Y Axis
        //GL11.glRotatef((float) -xrot, 1.0f, 0.0f, 0.0f);                 // Rotate By -xrot On X Axis
        //GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,(FloatBuffer)floatBuffer.asFloatBuffer().put(Minv).flip());              // Retrieve ModelView Matrix (Stores In Minv)
        /*lp[0] = lightPos[0];                                // Store Light Position X In lp[0]
        lp[1] = lightPos[1];                                // Store Light Position Y In lp[1]
        lp[2] = lightPos[2];                                // Store Light Position Z In lp[2]
        lp[3] = lightPos[3];                                // Store Light Direction In lp[3]*/
        
        //vMatMult(Minv, lp);                                 // We Store Rotated Light Vector In 'lp' Array
        
        //Vector3f vObjPos = obj.getPosition();        
    	Vector3f vObjPos = new Vector3f();
    	Vector3f.add( obj.getPosition(), obj.getShape().center, vObjPos );
    	
        //float[] objPos = { (float) vObjPos.x, (float) vObjPos.y, (float) vObjPos.z, 0 };
        //float[] objPos = { (float) vObjPos.x, (float) vObjPos.y, (float) vObjPos.z, 0 };
        
        /*GL11.glTranslatef(-objPos[0], -objPos[1], -objPos[2]);   // Move Negative On All Axis Based On ObjPos[] Values (X, Y, Z)
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,(FloatBuffer)floatBuffer.asFloatBuffer().put(Minv).flip());              // Retrieve ModelView Matrix From Minv
        wlp[0] = 0.0f;                                      // World Local Coord X To 0
        wlp[1] = 0.0f;                                      // World Local Coord Y To 0
        wlp[2] = 0.0f;                                      // World Local Coord Z To 0
        wlp[3] = 1.0f;
        vMatMult(Minv, wlp);                                // We Store The Position Of The World Origin Relative To The
                                                            // Local Coord. System In 'wlp' Array
        lp[0] += wlp[0];                                    // Adding These Two Gives Us The
        lp[1] += wlp[1];                                    // Position Of The Light Relative To
        lp[2] += wlp[2];                                    // The Local Coordinate System

        GL11.glColor4f(0.7f, 0.4f, 0.0f, 1.0f);                  // Set Color To An Orange
        GL11.glLoadIdentity();                                   // Reset Modelview Matrix
        //GL11.glTranslatef(0.0f, 0.0f, -20.0f);                   // Zoom Into The Screen 20 Units
        drawGLRoom();                                       // Draw The Room*/
        
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        
        GL11.glPushMatrix();
        //GL11.glColor4d( obj.color.x, obj.color.y, obj.color.z, obj.color.w );
        setColor( (float)obj.color.x, (float)obj.color.y, (float)obj.color.z, (float)obj.color.w );
                
        GL11.glTranslatef( vObjPos.x, vObjPos.y, vObjPos.z );      // Position The Object
        //GL11.glTranslatef(objPos[0], objPos[1], objPos[2]);      // Position The Object
                        
        Vector4f rot = obj.getRotation();
        GL11.glRotatef( (float)rot.w, (float)rot.x, (float)rot.y, (float)rot.z);
        if( ! ( obj.getShape().type == BrShape.BrShapeType.CONE ||
        		obj.getShape().type == BrShape.BrShapeType.UNIT_CONE ||
        		obj.getShape().type == BrShape.BrShapeType.SPHERE ||
        		obj.getShape().type == BrShape.BrShapeType.UNIT_SPHERE ||
        		obj.getShape().type == BrShape.BrShapeType.CYLINDER ||
        		obj.getShape().type == BrShape.BrShapeType.MESH ||
        		obj.getShape().type == BrShape.BrShapeType.ICOSAHEDRON ) ) {         	 
        	GL11.glScaled( dim.x, dim.y, dim.z );
        	//System.out.println( "drawShape " + dim );
        }
                
        if( obj.getTextureId() != -1 ) {
        	GL11.glEnable(GL11.GL_TEXTURE_2D);
        	GL11.glBindTexture(GL11.GL_TEXTURE_2D, obj.getTextureId() );
        } else {
        	GL11.glDisable(GL11.GL_TEXTURE_2D);
        }        
        
        // Render primitives directly with vertex commands       
        if( obj.getShape().mesh == null || ( obj.getShape().getType() == "box" ) ) {
        //if( obj.getShape().mesh == null ) {
        	//System.out.println( "NO MESH " + obj.type );
	        if( obj.getShape().getType() == "box" )
	        	drawBox( 1, 1, 1 );
	        else if( obj.getShape().getType() == "cone" )
	        	drawCylinder( (float)dim.x, (float)0.01, (float)dim.y, (int)dim.z, 25 );
	        else if( obj.getShape().getType() == "cylinder" )
	        	drawCylinder( (float)dim.x, (float)dim.y, (float)dim.y, (int)dim.z, 25 );
	        else
	        	drawSphere( (float)dim.x, (int)dim.y, 20);
        } else {
        	//System.out.println( "Rendering from mesh " + dim );
        	//GL11.glScaled( dim.x, dim.y, dim.z );
        	obj.getShape().mesh.opengldraw();
    	}        
        
        GL11.glPopMatrix();
                                 
        /*if( obj.getShape().mesh != null && obj.enabledShadow() ) {
        	//System.out.println( "drawShape " + obj.type + " " + obj.getShape() );
        	castShadow( obj.getShape().mesh, lp);                               // Procedure For Casting The Shadow Based On The Silhouette
        	//System.out.println( "castShadow" );
        } */
                
	}   
    
    public static void screenshot( String filename ) throws LWJGLException {
	    GL11.glReadBuffer(GL11.GL_FRONT);	
	    /*if( Display.wasResized() ) {
	    	//GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
	    	Display.setDisplayMode(new DisplayMode(Display.getWidth(),Display.getHeight()));
	    }*/
	    //int width = Display.getDisplayMode().getWidth();
	    int width = Display.getWidth();
	    //int height= Display.getDisplayMode().getHeight();
	    int height = Display.getHeight();
	    int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
	    ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
	    GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );
	    
	    File file = new File( filename ); // The file to save to.
	    String format = "PNG"; // Example: "PNG" or "JPG"
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	      
	    for(int x = 0; x < width; x++) {
	    	for(int y = 0; y < height; y++) {
	    		int i = (x + (width * y)) * bpp;
	    		int r = buffer.get(i) & 0xFF;
	    		int g = buffer.get(i + 1) & 0xFF;
	    		int b = buffer.get(i + 2) & 0xFF;
	    		image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
	    	}
	    }
	      
	    try {
	    	ImageIO.write(image, format, file);
	    } catch (IOException e) { e.printStackTrace(); }
}

    
    
}

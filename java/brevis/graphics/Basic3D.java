

package brevis.graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;

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
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import static org.lwjgl.opengl.ARBFramebufferObject.*;
import static org.lwjgl.opengl.ARBShadowAmbient.GL_TEXTURE_COMPARE_FAIL_VALUE_ARB;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.util.glu.GLU.*;
import brevis.BrObject;
import brevis.BrShape;
import brevis.Engine;

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
	
	// Shadow stuff
	private static int shadowMapWidth;
	private static int shadowMapHeight;
	private static int shadowFrameBuffer;
	private static int shadowRenderBuffer;
	
	public static void lightMove( int lightNum, float[] position ) {
		//light_position.put( position ).flip();
		light1.setPosition( position );
	}	
	
	private static void setUpFrameBufferObject() {
		final int MAX_RENDERBUFFER_SIZE = glGetInteger(GL_MAX_RENDERBUFFER_SIZE);
		final int MAX_TEXTURE_SIZE = glGetInteger(GL_MAX_TEXTURE_SIZE);
		/**
		* Cap the maximum shadow map size at 1024x1024 pixels or at the maximum render buffer size. If you have a good
		* graphics card, feel free to increase this value. The program will lag
		* if I record and run the program at the same time with higher values.
		*/
		if (MAX_TEXTURE_SIZE > 1024) {
		if (MAX_RENDERBUFFER_SIZE < MAX_TEXTURE_SIZE) {
		shadowMapWidth = shadowMapHeight = MAX_RENDERBUFFER_SIZE;
		} else {
		shadowMapWidth = shadowMapHeight = 1024;
		}
		} else {
		shadowMapWidth = shadowMapHeight = MAX_TEXTURE_SIZE;
		}
		// Generate and bind a frame buffer.
		shadowFrameBuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, shadowFrameBuffer);
		// Generate and bind a render buffer.
		shadowRenderBuffer = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, shadowRenderBuffer);
		// Set the internal storage format of the render buffer to a depth component of 32 bits (4 bytes).
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32, shadowMapWidth, shadowMapHeight);
		// Attach the render buffer to the frame buffer as a depth attachment. This means that, if the frame buffer is
		// bound, any depth texture values will be copied to the render buffer object.
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, shadowRenderBuffer);
		// OpenGL shall make no amendment to the colour or multisample buffer.
		glDrawBuffer(GL_NONE);
		// Disable the colour buffer for pixel read operations (such as glReadPixels or glCopyTexImage2D).
		glReadBuffer(GL_NONE);
		// Check for frame buffer errors.
		int FBOStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
		if (FBOStatus != GL_FRAMEBUFFER_COMPLETE) {
		System.err.println("Framebuffer error: " + gluErrorString(glGetError()));
		}
		// Bind the default frame buffer, which is used for ordinary drawing.
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
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
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);               // Black Background
        GL11.glClearDepth(1.0f);                                 // Depth Buffer Setup
        GL11.glClearStencil(0);                                  // Stencil Buffer Setup
        GL11.glEnable(GL11.GL_DEPTH_TEST);                            // Enables Depth Testing
        GL11.glDepthFunc(GL11.GL_LEQUAL);                             // The Type Of Depth Testing To Do
        //GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);  // Really Nice Perspective Calculations
        
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_FASTEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_FASTEST);
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
   
        
        light1.enable();
        sky = new BrSky();
        
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);            
        GL11.glViewport(0,0,width,height);                           // Reset The Current Viewport

        GL11.glMatrixMode(GL11.GL_PROJECTION);                            // Select The Projection Matrix
        GL11.glLoadIdentity();                                       // Reset The Projection Matrix
        
        GLU.gluPerspective(45.0f,
                (float) width / (float) height,
                0.05f, 100.0f);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);                             // Select The Modelview Matrix
        GL11.glLoadIdentity();                                       // Reset The Modelview Matrix
        
        setUpFrameBufferObject();

    }
    
	
    static public void initFrame( BrCamera displayCamera ) {
    	GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );
    	
    	//GL11.glEnable( GL11.GL_LIGHTING );
    	//GL11.glEnable( GL11.GL_LIGHT0 );
    	
    	//light1.enable();
    	
    	//glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    	//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);
    	GL11.glDisable(GL_TEXTURE_GEN_R);
    	GL11.glDisable(GL_TEXTURE_GEN_Q);    	
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
		
		light1.enable();
		
		GL11.glColor3f (1.0f, 1.0f, 1.0f);

		// clear the window
		GL11.glClearColor (0.5f ,0.5f ,0.5f ,0);
		GL11.glClear (GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// go to GL_MODELVIEW matrix mode and set the camera
		GL11.glMatrixMode (GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		displayCamera.setupFrame();		
		
		sky.draw( displayCamera );
		
    }
    
    // Le ew
    static FloatBuffer lightProjection = BufferUtils.createFloatBuffer(16);
	static FloatBuffer lightModelView = BufferUtils.createFloatBuffer(16);
	static Matrix4f lightProjectionTemp = new Matrix4f();
	static Matrix4f lightModelViewTemp = new Matrix4f();
	private static final Matrix4f depthModelViewProjection = new Matrix4f();
	private static final FloatBuffer shadowTextureBuffer = BufferUtils.createFloatBuffer(16);
	//GLuint shadowMapTexture;
    
    static public void initShadows( BrCamera cam ) {
    	
    	cam.perspectiveMatrix();
    	// Store the shadow frustum in 'lightProjection'.
    	glGetFloat(GL_PROJECTION_MATRIX, lightProjection);
    	glMatrixMode(GL_MODELVIEW);
    	// Store the current model-view matrix.
    	glPushMatrix();
    	glLoadIdentity();
    	// Have the 'shadow camera' look toward [0, 0, 0] and be location at the light's position.
    	float[] lp = light1.getPosition();
    	gluLookAt(lp[0], lp[1], lp[2], 0, 0, 0, 0, 1, 0);
    	glGetFloat(GL_MODELVIEW_MATRIX, lightModelView);
    	// Set the view port to the shadow map dimensions so no part of the shadow is cut off.
    	glViewport(0, 0, shadowMapWidth, shadowMapHeight);
    	// Bind the extra frame buffer in which to store the shadow map in the form a depth texture.
    	glBindFramebuffer(GL_FRAMEBUFFER, shadowFrameBuffer);
    	// Clear only the depth buffer bit. Clearing the colour buffer is unnecessary, because it is disabled (we
    	// only need depth components).
    	glClear(GL_DEPTH_BUFFER_BIT);
    	
    	glPushAttrib(GL_ALL_ATTRIB_BITS);
    	
    	glShadeModel(GL_FLAT);
    	// Enabling all these lighting states is unnecessary for reasons listed above.
    	glDisable(GL_LIGHTING);
    	glDisable(GL_COLOR_MATERIAL);
    	glDisable(GL_NORMALIZE);
    	// Disable the writing of the red, green, blue, and alpha colour components,
    	// because we only need the depth component.
    	glColorMask(false, false, false, false);
    	// An offset is given to every depth value of every polygon fragment to prevent a visual quirk called
    	// 'shadow
    	// acne'.
    	glEnable(GL_POLYGON_OFFSET_FILL);
    	glCullFace(GL_FRONT);
    	//glColorMask(0, 0, 0, 0);
    }
    
    static public void finishShadows() {
    	//glBindTexture(GL_TEXTURE_2D, shadowMapTexture);
    	glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 0, 0, shadowMapWidth, shadowMapHeight, 0);
    	// Restore the previous model-view matrix.
    	glPopMatrix();
    	glMatrixMode(GL_PROJECTION);
    	// Restore the previous projection matrix.
    	glPopMatrix();
    	glMatrixMode(GL_MODELVIEW);
    	glBindFramebuffer(GL_FRAMEBUFFER, 0);
    	// Restore the previous attribute state.
    	glPopAttrib();
    	// Restore the view port.
    	glViewport(0, 0, Display.getWidth(), Display.getHeight());
    	lightProjectionTemp.load(lightProjection);
    	lightModelViewTemp.load(lightModelView);
    	lightProjection.flip();
    	lightModelView.flip();
    	depthModelViewProjection.setIdentity();
    	// [-1,1] -> [-0.5,0.5] -> [0,1]
    	depthModelViewProjection.translate(new Vector3f(0.5F, 0.5F, 0.5F));
    	depthModelViewProjection.scale(new Vector3f(0.5F, 0.5F, 0.5F));
    	// Multiply the texture matrix by the projection and model-view matrices of the light.
    	Matrix4f.mul(depthModelViewProjection, lightProjectionTemp, depthModelViewProjection);
    	Matrix4f.mul(depthModelViewProjection, lightModelViewTemp, depthModelViewProjection);
    	// Transpose the texture matrix.
    	Matrix4f.transpose(depthModelViewProjection, depthModelViewProjection);
    }
    
    public static void generateTextureCoordinates() {
    	glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
    	// Compare the texture coordinate 'r' (the distance from the light to the surface of the object) to the
    	// value in the depth buffer.
    	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);
    	// Enable 's' texture coordinate generation.
    	glEnable(GL_TEXTURE_GEN_S);
    	// Enable 't' texture coordinate generation.
    	glEnable(GL_TEXTURE_GEN_T);
    	// Enable 'r' texture coordinate generation.
    	glEnable(GL_TEXTURE_GEN_R);
    	// Enable 'q' texture coordinate generation.
    	glEnable(GL_TEXTURE_GEN_Q);
    	shadowTextureBuffer.clear();
    	shadowTextureBuffer.put(0, depthModelViewProjection.m00);
    	shadowTextureBuffer.put(1, depthModelViewProjection.m01);
    	shadowTextureBuffer.put(2, depthModelViewProjection.m02);
    	shadowTextureBuffer.put(3, depthModelViewProjection.m03);
    	glTexGen(GL_S, GL_EYE_PLANE, shadowTextureBuffer);
    	shadowTextureBuffer.put(0, depthModelViewProjection.m10);
    	shadowTextureBuffer.put(1, depthModelViewProjection.m11);
    	shadowTextureBuffer.put(2, depthModelViewProjection.m12);
    	shadowTextureBuffer.put(3, depthModelViewProjection.m13);
    	glTexGen(GL_T, GL_EYE_PLANE, shadowTextureBuffer);
    	shadowTextureBuffer.put(0, depthModelViewProjection.m20);
    	shadowTextureBuffer.put(1, depthModelViewProjection.m21);
    	shadowTextureBuffer.put(2, depthModelViewProjection.m22);
    	shadowTextureBuffer.put(3, depthModelViewProjection.m23);
    	glTexGen(GL_R, GL_EYE_PLANE, shadowTextureBuffer);
    	shadowTextureBuffer.put(0, depthModelViewProjection.m30);
    	shadowTextureBuffer.put(1, depthModelViewProjection.m31);
    	shadowTextureBuffer.put(2, depthModelViewProjection.m32);
    	shadowTextureBuffer.put(3, depthModelViewProjection.m33);
    	glTexGen(GL_Q, GL_EYE_PLANE, shadowTextureBuffer);
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

	/*static public void drawCylinder(float baseRadius, float topRadius, float height, int slices, int stacks) {

		Cylinder c = new Cylinder();
	
		c.draw(baseRadius, topRadius, height, slices, stacks);
		
		//System.out.println(baseRadius + " " + topRadius + " " + height + " " + slices + " " + stacks);

	}*/
	
	static public void drawCylinder(float baseRadius, float topRadius, float height, int slices, int stacks, Cylinder data) {

		//Cylinder c = new Cylinder();
	
		data.draw(baseRadius, topRadius, height, slices, stacks);
		
		//System.out.println(baseRadius + " " + topRadius + " " + height + " " + slices + " " + stacks);

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
    	Vector3f vObjPos = new Vector3f();
    	Vector3f.add( obj.getPosition(), obj.getShape().center, vObjPos );
        
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
        	 setColor( (float)obj.color.x, (float)obj.color.y, (float)obj.color.z, (float)obj.color.w );
        }        
        
        int numSlices = 25;
        int numStacks = 25;
        
        // Render primitives directly with vertex commands       
        if( ( obj.getShape().mesh == null ) ||
        		( obj.getShape().getType() == "box" ) || 
        		( obj.getShape().getType() == "cone" ) || 
        		( obj.getShape().getType() == "cylinder" ) ) {
        //if( obj.getShape().mesh == null ) {
        	//System.out.println( "NO MESH " + obj.type );
        	obj.getShape().opengldraw();
	        /*if( obj.getShape().getType() == "box" )
	        	drawBox( 1, 1, 1 );
	        	
	        else if( obj.getShape().getType() == "cone" )
	        	drawCylinder( (float)dim.y, (float)0.0001, (float)dim.x, numSlices, numStacks, (Cylinder)obj.getShape().data );
	        else if( obj.getShape().getType() == "cylinder" )
	        	drawCylinder( (float)dim.y, (float)dim.z, (float)dim.x, numSlices, numStacks, (Cylinder)obj.getShape().data );
	        else
	        	drawSphere( (float)dim.x, 25, 20);*/
	        	//( (Sphere)obj.getShape().data ).draw( (float)dim.x, 25, 20);
        } else {
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
    
    static public void drawLine(Vector3f source, Vector3f destination, Vector4f color) {
        
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glColor3f( color.x, color.y, color.z );
        GL11.glVertex3f( source.x, source.y, source.z );
        GL11.glVertex3f( destination.x, destination.y, destination.z );
        GL11.glEnd();
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
    
    public static BufferedImage screenshotImage( ) throws LWJGLException {
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
	      
	    return image;
    }   
    
    private static void drawGround() {
        glPushAttrib(GL_LIGHTING_BIT);
        {
            glDisable(GL_LIGHTING);
            glBegin(GL_QUADS);
            glColor3f(0.3F, 0.6F, 0.3F);
            glVertex3f(-120, -19, -120);
            glVertex3f(-120, -19, +120);
            glVertex3f(+120, -19, +120);
            glVertex3f(+120, -19, -120);
            glEnd();
        }
        glPopAttrib();
    }
    
    /** Generate the shadow map. */
    private static void drawShadowMap( Engine e ) {
        /**
         * The model-view matrix of the light.
         */
        FloatBuffer lightModelView = BufferUtils.createFloatBuffer(16);
        /**
         * The projection matrix of the light.
         */
        FloatBuffer lightProjection = BufferUtils.createFloatBuffer(16);
        Matrix4f lightProjectionTemp = new Matrix4f();
        Matrix4f lightModelViewTemp = new Matrix4f();
        /**
         * The radius that encompasses all the objects that cast shadows in the scene. There should
         * be no object farther away than 50 units from [0, 0, 0] in any direction.
         * If an object exceeds the radius, the object may cast shadows wrongly.
         */
        float sceneBoundingRadius = 50;
        /**
         * The distance from the light to the scene, assuming that the scene is located
         * at [0, 0, 0]. Using the Pythagorean theorem, the distance is calculated by taking the square-root of the
         * sum of each of the components of the light position squared.
         */
        float [] lightpos = light1.getPosition();
        float lightToSceneDistance = (float) Math.sqrt(lightpos[0] * lightpos[0] +
                lightpos[1] * lightpos[1] +
                lightpos[2] * lightpos[2] );
        /**
         * The distance to the object that is nearest to the camera. This excludes objects that do not cast shadows.
         * This will be used as the zNear parameter in gluPerspective.
         */
        float nearPlane = lightToSceneDistance - sceneBoundingRadius;
        if (nearPlane < 0) {
            System.err.println("Camera is too close to scene. A valid shadow map cannot be generated.");
        }
        /**
         * The field-of-view of the shadow frustum in degrees. Formula taken from the OpenGL SuperBible.
         */
        float fieldOfView = (float) Math.toDegrees(2.0F * Math.atan(sceneBoundingRadius / lightToSceneDistance));
        glMatrixMode(GL_PROJECTION);
        // Store the current projection matrix.
        glPushMatrix();
        glLoadIdentity();
        // Generate the 'shadow frustum', a perspective projection matrix that shows all the objects in the scene.
        gluPerspective(fieldOfView, 1, nearPlane, nearPlane + sceneBoundingRadius * 2);
        // Store the shadow frustum in 'lightProjection'.
        glGetFloat(GL_PROJECTION_MATRIX, lightProjection);
        glMatrixMode(GL_MODELVIEW);
        // Store the current model-view matrix.
        glPushMatrix();
        glLoadIdentity();
        // Have the 'shadow camera' look toward [0, 0, 0] and be location at the light's position.
        //float[] lightpos = light1.getPosition();
        gluLookAt( lightpos[0], lightpos[1], lightpos[2], 0, 0, 0, 0, 1, 0);
        glGetFloat(GL_MODELVIEW_MATRIX, lightModelView);
        // Set the view port to the shadow map dimensions so no part of the shadow is cut off.
        glViewport(0, 0, shadowMapWidth, shadowMapHeight);
        // Bind the extra frame buffer in which to store the shadow map in the form a depth texture.
        glBindFramebuffer(GL_FRAMEBUFFER, shadowFrameBuffer);
        // Clear only the depth buffer bit. Clearing the colour buffer is unnecessary, because it is disabled (we
        // only need depth components).
        glClear(GL_DEPTH_BUFFER_BIT);
        // Store the current attribute state.
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        {
            // Disable smooth shading, because the shading in a shadow map is irrelevant. It only matters where the
            // shape
            // vertices are positioned, and not what colour they have.
            glShadeModel(GL_FLAT);
            // Enabling all these lighting states is unnecessary for reasons listed above.
            glDisable(GL_LIGHTING);
            glDisable(GL_COLOR_MATERIAL);
            glDisable(GL_NORMALIZE);
            // Disable the writing of the red, green, blue, and alpha colour components,
            // because we only need the depth component.
            glColorMask(false, false, false, false);
            // An offset is given to every depth value of every polygon fragment to prevent a visual quirk called
            // 'shadow
            // acne'.
            glEnable(GL_POLYGON_OFFSET_FILL);
            // Draw the objects that cast shadows.
            drawScene( e );
            /**
             * Copy the pixels of the shadow map to the frame buffer object depth attachment.
             *  int target -> GL_TEXTURE_2D
             *  int level  -> 0, has to do with mip-mapping, which is not applicable to shadow maps
             *  int internalformat -> GL_DEPTH_COMPONENT
             *  int x, y -> 0, 0
             *  int width, height -> shadowMapWidth, shadowMapHeight
             *  int border -> 0
             */
            glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 0, 0, shadowMapWidth, shadowMapHeight, 0);
            // Restore the previous model-view matrix.
            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            // Restore the previous projection matrix.
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }// Restore the previous attribute state.
        glPopAttrib();
        // Restore the view port.
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        lightProjectionTemp.load(lightProjection);
        lightModelViewTemp.load(lightModelView);
        lightProjection.flip();
        lightModelView.flip();
        depthModelViewProjection.setIdentity();
        // [-1,1] -> [-0.5,0.5] -> [0,1]
        depthModelViewProjection.translate(new Vector3f(0.5F, 0.5F, 0.5F));
        depthModelViewProjection.scale(new Vector3f(0.5F, 0.5F, 0.5F));
        // Multiply the texture matrix by the projection and model-view matrices of the light.
        Matrix4f.mul(depthModelViewProjection, lightProjectionTemp, depthModelViewProjection);
        Matrix4f.mul(depthModelViewProjection, lightModelViewTemp, depthModelViewProjection);
        // Transpose the texture matrix.
        Matrix4f.transpose(depthModelViewProjection, depthModelViewProjection);
    }
    
    private static void drawScene( Engine e ) {
        glPushMatrix();
        Collection<BrObject> objects = e.getObjects();
        for( BrObject obj : objects ) {
        	drawShape( obj, obj.getShape().getDimension() );        	        	
        }        
        glPopMatrix();
    }
    
    public static void displayEngine( Engine e, BrCamera displayCamera ) {

    	
    	
    	glLoadIdentity();
        // Apply the camera position and orientation to the model-view matrix.
    	initFrame( displayCamera );
        
        // Clear the screen.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // Store the current attribute state.
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        {
            generateTextureCoordinates();
            
            drawGround();
            drawScene( e );
            drawShadowMap( e );
        }
        // Restore the previous attribute state.
        glPopAttrib();
    	
    }
}

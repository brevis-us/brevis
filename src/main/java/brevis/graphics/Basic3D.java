

package brevis.graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import brevis.BrObject;
import brevis.BrShape;
import brevis.Engine;
import org.lwjgl.BufferUtils;

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
    
    /*private static final FloatBuffer light_position =  BufferUtils.createFloatBuffer(4);
	private static final FloatBuffer light_ambient = BufferUtils.createFloatBuffer(4);
	private static final FloatBuffer light_diffuse = BufferUtils.createFloatBuffer(4);
	private static final FloatBuffer light_specular = BufferUtils.createFloatBuffer(4);
	static {
		//light_position.put(new float[] { LIGHTX, LIGHTY, 1.0f, 0.0f }).flip();
		light_position.put(new float[] { 50.0f, 200.0f, 50.0f, 0.0f }).flip();
		light_ambient.put(new float[]{ 0.5f, 0.5f, 0.5f, 1.0f }).flip();
		light_diffuse.put(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }).flip();
		light_specular.put(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }).flip();
	}*/
    
    static private float[] view_xyz = new float[3];	// position x,y,z
	static private float[] view_hpr = new float[3];	// heading, pitch, roll (degrees)
    
	//static BrLight light1 = new BrLight();// should probably have a light array
	
	static ArrayList<BrLight> lights = new ArrayList<BrLight>();
	static { 
		lights.add( new BrLight( 0 ) );
	}

	//static BrCamera displayCamera;// This is the BrCamera that gets the main GL context
	
	// Shadow stuff
	private static int shadowMapWidth;
	private static int shadowMapHeight;
	private static int shadowFrameBuffer;
	private static int shadowRenderBuffer;
	
	public static void addLight( ) {
		lights.add( new BrLight( lights.size() ) );
	}
	
	public static void lightMove( int lightNum, float[] position ) {
		//light_position.put( position ).flip();
		//light1.setPosition( position );
		lights.get(lightNum).setPosition( position );
	}	
	
	public static float[] lightPosition( int lightNum ) {
		return lights.get(lightNum).getPosition();
	}
	
	public static void lightDiffuse( int lightNum, float[] color) {
		lights.get(lightNum).setDiffuse( color );
	}
	
	public static void lightSpecular( int lightNum, float[] color) {
		lights.get(lightNum).setSpecular( color );
	}
	
	public static void lightAmbient( int lightNum, float[] color) {
		lights.get(lightNum).setAmbient( color );
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
    
	private FloatBuffer matrixF = BufferUtils.createFloatBuffer(16);


}

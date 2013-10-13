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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;
import javax.vecmath.Vector4f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.Sphere;

import brevis.BrObject;
import brevis.BrShape;

public class Basic3D {
    private static final float LIGHTX = 1.0f;
    private static final float LIGHTY = 0.4f;
    private static final float SHADOW_INTENSITY = 0.65f;
	
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
    static private void castShadow( BrMesh o, double lp[] ){
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
        Vector3d v3 = new Vector3d();
        Vector3d v4 = new Vector3d();
        float[] p1, p2, p3;
        
        System.out.println( o.faces.size() + " " + o.vertexsets.size() );
        
        for (i=0; i<o.numpolygons();i++){
        	System.out.println( i + " " + o.faces.get(i) );
        	int[] face = (int[])( o.faces.get(i) );        
            // could check to see if a face is visible before proceeding
        	
        	GL11.glBegin( GL11.GL_POLYGON );
        	
            for ( j=0; j<face.length; j++ ){                
            	k = face[j];
                if ( k != 0 ){
                    // here we have an edge, we must draw a polygon
                	float[] point = (float[])( o.vertexsets.get(k) );
                	GL11.glVertex3f( point[0], point[1], point[2] );
                }
            }
            
            GL11.glEnd();
        }
        
    }    
    
	// some from nehe lesson 27    
	//static public void drawShape( BrObject obj, double xrot, double yrot, double zrot, double xoff, double yoff, double zoff, double[] lp, Vector3d dim ) {
    static public void drawShape( BrObject obj, double[] lp, Vector3d dim ) {
		float Minv[] = new float[16];
        float wlp[] = new float[4];        
		
		ByteBuffer byteBuffer;
		ByteBuffer floatBuffer;
		floatBuffer = ByteBuffer.allocateDirect(64);
        floatBuffer.order(ByteOrder.nativeOrder());
        byteBuffer = ByteBuffer.allocateDirect(16);
        byteBuffer.order(ByteOrder.nativeOrder());
		
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
        
        vMatMult(Minv, lp);                                 // We Store Rotated Light Vector In 'lp' Array
        
        Vector3d vObjPos = obj.getPosition();        
        float[] objPos = { (float) vObjPos.x, (float) vObjPos.y, (float) vObjPos.z, 0 };
        
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
        
        GL11.glPushMatrix();
        //GL11.glColor4d( obj.color.x, obj.color.y, obj.color.z, obj.color.w );
        setColor( (float)obj.color.x, (float)obj.color.y, (float)obj.color.z, (float)obj.color.w );
        GL11.glTranslatef(objPos[0], objPos[1], objPos[2]);      // Position The Object
                
        if( ! ( obj.getShape().type == BrShape.BrShapeType.CONE ||
        		obj.getShape().type == BrShape.BrShapeType.UNIT_CONE ||
        		obj.getShape().type == BrShape.BrShapeType.SPHERE ||
        		obj.getShape().type == BrShape.BrShapeType.CYLINDER ||
        		obj.getShape().type == BrShape.BrShapeType.MESH ) ) {         	 
        	GL11.glScaled( dim.x, dim.y, dim.z );
        	//System.out.println( "drawShape " + dim );
        }
        Vector4d rot = obj.getRotation();
        GL11.glRotatef( (float)rot.w, (float)rot.x, (float)rot.y, (float)rot.z);
                
        if( obj.getTextureId() != -1 ) {
        	GL11.glEnable(GL11.GL_TEXTURE_2D);
        	GL11.glBindTexture(GL11.GL_TEXTURE_2D, obj.getTextureId() );
        } else {
        	GL11.glDisable(GL11.GL_TEXTURE_2D);
        }        
        
        // Render primitives directly with vertex commands       
        if( obj.getShape().mesh == null ) {
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
        	//System.out.println( "Rendering from mesh" );
        	obj.getShape().mesh.opengldraw();
    	}
        
        GL11.glPopMatrix();

        if( obj.getShape().mesh != null ) {
        	castShadow( obj.getShape().mesh, lp);                               // Procedure For Casting The Shadow Based On The Silhouette
        	System.out.println( "castShadow" );
        }
                            
		
	}
    
    
}

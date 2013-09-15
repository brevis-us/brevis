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

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.Sphere;

import brevis.BrObject;

public class Basic3D {
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

	static public void drawCone(float baseRadius, float topRadius, float height, int slices, int stacks) {

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
	
	// some from nehe lesson 27    
	static public void drawShape( BrObject obj, double xrot, double yrot, double zrot, double xoff, double yoff, double zoff, double[] lp, Vector3d dim ) {
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
        GL11.glLoadIdentity();                                   // Reset Matrix
        GL11.glTranslatef((float) xoff, (float) yoff, (float) zoff);      // Position The Object
        GL11.glRotatef((float) -zrot, 0.0f, 0.0f, 1.0f);                 // Rotate By -yrot On Y Axis
        GL11.glRotatef((float) -yrot, 0.0f, 1.0f, 0.0f);                 // Rotate By -yrot On Y Axis
        GL11.glRotatef((float) -xrot, 1.0f, 0.0f, 0.0f);                 // Rotate By -xrot On X Axis
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
        
        GL11.glTranslatef(objPos[0], objPos[1], objPos[2]);      // Position The Object
        Vector4d rot = obj.getRotation();
        GL11.glRotatef( (float)rot.w, (float)rot.x, (float)rot.y, (float)rot.z);
        //GL11.glRotatef((float) xrot, 1.0f, 0.0f, 0.0f);                  // Spin It On The X Axis By xrot
        //GL11.glRotatef((float) yrot, 0.0f, 1.0f, 0.0f);                  // Spin It On The Y Axis By yrot               
        
        GL11.glScaled( dim.x, dim.y, dim.z );
        
        if( obj.getShape().getType() == "box" )
        	drawBox( 1, 1, 1 );
        else if( obj.getShape().getType() == "cone" )
        	drawCone( (float)0.8, (float)0.01, (float)1.2, 25, 25 );
        else
        	drawSphere( 2, 20, 20);

        //castShadow(obj, lp);                               // Procedure For Casting The Shadow Based On The Silhouette
                            
        /*GL11.glColor4f(0.7f, 0.4f, 0.0f, 1.0f);                  // Set Color To Purplish Blue
        GL11.glDisable(GL11.GL_LIGHTING);                             // Disable Lighting
        GL11.glDepthMask(false);                              // Disable Depth Mask
        GL11.glTranslatef(lp[0], lp[1], lp[2]);                  // Translate To Light's Position
                                                            // Notice We're Still In Local Coordinate System
        q.draw(0.2f, 16, 8);                          // Draw A Little Yellow Sphere (Represents Light)
        GL11.glEnable(GL11.GL_LIGHTING);                              // Enable Lighting
        GL11.glDepthMask(true);                               // Enable Depth Mask*/

        //GL11.glFlush();                                          // Flush The OpenGL Pipeline		
		
	}
}

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
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.Sphere;

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
}

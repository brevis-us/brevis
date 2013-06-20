package brevis.graphics;
import org.lwjgl.opengl.GL11;


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
}

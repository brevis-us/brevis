package brevis.graphics;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

//import edu.fhooe.mtd360.watershader.render.Renderer;

// from http://code.google.com/p/lwjgl-water-shader/
public class BrSky {

	private Vector<Texture> textures;
	
	public BrSky() {
		
		textures = new Vector<Texture>();
		
		//load textures
		try {
			//textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("resources/img/skybox/front.jpg"),GL11.GL_LINEAR));			
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/front.jpg"),GL11.GL_LINEAR));						
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/left.jpg"),GL11.GL_LINEAR));
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/back.jpg"),GL11.GL_LINEAR));
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/right.jpg"),GL11.GL_LINEAR));
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/up.jpg"),GL11.GL_LINEAR));
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/down.jpg"),GL11.GL_LINEAR));
		} catch (IOException e) {
			throw new RuntimeException("skybox loading error");
		}
		
	}
		
	static float SkyboxUnit = 500f;
	
	//public void draw() {
	public void draw( float x, float y, float z ) {
		
		GL11.glPushMatrix();
		
		GL11.glLoadIdentity();
		//GL11.glTranslatef(Renderer.camPosX, Renderer.camPosY, -Renderer.camPosZ);
		//GL11.glTranslatef( x, y, z );
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);		
	    GL11.glDisable(GL11.GL_DEPTH_TEST);
	    GL11.glDisable(GL11.GL_LIGHTING);
	    GL11.glDisable(GL11.GL_BLEND);
		//GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	   
		// Just in case we set all vertices to white.
	    GL11.glColor4f(1,1,1,0.5f);

//			gluBuild2DMipmaps( GL_TEXTURE_2D, 3, width, height,
//	                GL_RGB, GL_UNSIGNED_BYTE, data );
	    
	    // Render the front quad
	    //clampToEdge();
	    textures.get(0).bind();
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glTexCoord2f(1f, 1f); GL11.glVertex3f(  SkyboxUnit, -1f * SkyboxUnit, -1f * SkyboxUnit );
		    GL11.glTexCoord2f(0f, 1f); GL11.glVertex3f( -1f * SkyboxUnit, -1f * SkyboxUnit, -1f * SkyboxUnit );
		    GL11.glTexCoord2f(0f, 0f); GL11.glVertex3f( -1f * SkyboxUnit,  SkyboxUnit, -1f * SkyboxUnit );
		    GL11.glTexCoord2f(1f, 0f); GL11.glVertex3f(  SkyboxUnit,  SkyboxUnit, -1f * SkyboxUnit );
	    GL11.glEnd();

	    // Render the left quad
	    //clampToEdge();
	    textures.get(1).bind();
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glTexCoord2f(1f, 1f); GL11.glVertex3f(  SkyboxUnit, -1f * SkyboxUnit,  SkyboxUnit );
		    GL11.glTexCoord2f(0f, 1f); GL11.glVertex3f(  SkyboxUnit, -1f * SkyboxUnit, -1f * SkyboxUnit );
		    GL11.glTexCoord2f(0f, 0f); GL11.glVertex3f(  SkyboxUnit,  SkyboxUnit, -1f * SkyboxUnit );
		    GL11.glTexCoord2f(1f, 0f); GL11.glVertex3f(  SkyboxUnit,  SkyboxUnit,  SkyboxUnit );
	    GL11.glEnd();
	    
	    // Render the back quad
	    //clampToEdge();
	    textures.get(2).bind();
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glTexCoord2f(1f, 1f); GL11.glVertex3f( -1f * SkyboxUnit, -1f * SkyboxUnit,  SkyboxUnit );
		    GL11.glTexCoord2f(0f, 1f); GL11.glVertex3f(  SkyboxUnit, -1f * SkyboxUnit,  SkyboxUnit );
		    GL11.glTexCoord2f(0f, 0f); GL11.glVertex3f(  SkyboxUnit,  SkyboxUnit,  SkyboxUnit );
		    GL11.glTexCoord2f(1f, 0f); GL11.glVertex3f( -1f * SkyboxUnit,  SkyboxUnit,  SkyboxUnit );
	 
	    GL11.glEnd();
	    
	    // Render the right quad
	    //clampToEdge();
	    textures.get(3).bind();
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glTexCoord2f(1f, 1f); GL11.glVertex3f( -1f * SkyboxUnit, -1f * SkyboxUnit, -1f * SkyboxUnit );
		    GL11.glTexCoord2f(0f, 1f); GL11.glVertex3f( -1f * SkyboxUnit, -1f * SkyboxUnit,  SkyboxUnit );
		    GL11.glTexCoord2f(0f, 0f); GL11.glVertex3f( -1f * SkyboxUnit,  SkyboxUnit,  SkyboxUnit );
		    GL11.glTexCoord2f(1f, 0f); GL11.glVertex3f( -1f * SkyboxUnit,  SkyboxUnit, -1f * SkyboxUnit );
	    GL11.glEnd();
	    
	    // Render the top quad
	    //clampToEdge();
	    textures.get(4).bind();
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glTexCoord2f(1f, 1f); GL11.glVertex3f( -1f * SkyboxUnit,  SkyboxUnit, -1f * SkyboxUnit );
		    GL11.glTexCoord2f(0f, 1f); GL11.glVertex3f( -1f * SkyboxUnit,  SkyboxUnit,  SkyboxUnit );
		    GL11.glTexCoord2f(0f, 0f); GL11.glVertex3f(  SkyboxUnit,  SkyboxUnit,  SkyboxUnit );
		    GL11.glTexCoord2f(1f, 0f); GL11.glVertex3f(  SkyboxUnit,  SkyboxUnit, -1f * SkyboxUnit );
	    GL11.glEnd();
	    
	    // Render the bottom quad
	    //clampToEdge();
	    textures.get(5).bind();
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glTexCoord2f(1f, 1f); GL11.glVertex3f( -1f * SkyboxUnit, -1f * SkyboxUnit, -1f * SkyboxUnit );
		    GL11.glTexCoord2f(1f, 0f); GL11.glVertex3f( -1f * SkyboxUnit, -1f * SkyboxUnit,  SkyboxUnit );
		    GL11.glTexCoord2f(0f, 0f); GL11.glVertex3f(  SkyboxUnit, -1f * SkyboxUnit,  SkyboxUnit );
		    GL11.glTexCoord2f(0f, 1f); GL11.glVertex3f(  SkyboxUnit, -1f * SkyboxUnit, -1f * SkyboxUnit );
	    GL11.glEnd();
	 
	    // Restore enable bits and matrix
	    GL11.glPopAttrib();
	    GL11.glPopMatrix();
	}

	//clamp textures, that edges get dont create a line in between
	private void clampToEdge() {
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
	}

}


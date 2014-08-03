/*This file is part of brevis.                                                                                                                                                 
                                                                                                                                                                                     
    brevis is free software: you can redistribute it and/or modify                                                                                                           
    it under the terms of the GNU General Public License as published by                                                                                                             
    the Free Software Foundation, either version 3 of the License, or                                                                                                                
    (at your option) any later version.                                                                                                                                              
                                                                                                                                                                                     
    brevis is distributed in the hope that it will be useful,                                                                                                                
    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                                                                                   
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                                                                                    
    GNU General Public License for more details.                                                                                                                                     
                                                                                                                                                                                     
    You should have received a copy of the GNU General Public License                                                                                                                
    along with brevis.  If not, see <http://www.gnu.org/licenses/>.                                                                                                          
                                                                                                                                                                                     
Copyright 2012, 2013 Kyle Harrington*/

package brevis.graphics;

import java.io.File;
import java.io.IOException;
import java.util.List;
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

	public Vector<Texture> textures;
	
	public BrSky() {
		
		textures = new Vector<Texture>();
		
		defaultSkybox();

	}
	
	public void defaultSkybox() {
		textures.clear();
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
	
	public void changeSkybox( List<String> filenames ) {
		textures.clear();
		//load textures
		try {
			for( String filename : filenames ) {
				textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream( filename ),GL11.GL_LINEAR));						
			}
		} catch (IOException e) {
			throw new RuntimeException("skybox loading error");
		}
		if( textures.size() < 6 ) {
			System.out.println( "Insufficient number of skybox textures loaded." );
		}
	}
		
	static float SkyboxUnit = 1500f;
	
	//public void draw() {
	//public void draw( float x, float y, float z ) {
	public void draw( BrCamera cam ) {
		
		GL11.glPushMatrix();
		
		GL11.glLoadIdentity();
		cam.setupFrame();
		//GL11.glTranslatef(Renderer.camPosX, Renderer.camPosY, -Renderer.camPosZ);
		//GL11.glTranslatef( x, y, z );
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);		
	    GL11.glDisable(GL11.GL_DEPTH_TEST);
	    GL11.glDisable(GL11.GL_LIGHTING);
	    GL11.glDisable(GL11.GL_BLEND);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	   
		// Just in case we set all vertices to white.
	    GL11.glColor4f(1,1,1,1);

//			gluBuild2DMipmaps( GL_TEXTURE_2D, 3, width, height,
//	                GL_RGB, GL_UNSIGNED_BYTE, data );
	    
	    // Render the front quad
	    //clampToEdge();
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(0).getTextureID());
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glNormal3f(0f, 0f, 1f);
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-SkyboxUnit, -SkyboxUnit,  SkyboxUnit);  // Bottom Left
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( SkyboxUnit, -SkyboxUnit,  SkyboxUnit);  // Bottom Right
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( SkyboxUnit,  SkyboxUnit,  SkyboxUnit);  // Top Right
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-SkyboxUnit,  SkyboxUnit,  SkyboxUnit);  // Top Left	    
	    GL11.glEnd();

	    // Render the left quad
	    //clampToEdge();
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(1).getTextureID());
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glNormal3f(1f, 0f, 0f);
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-SkyboxUnit, -SkyboxUnit, -SkyboxUnit);  // Bottom Left
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-SkyboxUnit, -SkyboxUnit,  SkyboxUnit);  // Bottom Right 
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-SkyboxUnit,  SkyboxUnit,  SkyboxUnit);  // Top Right
		    GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-SkyboxUnit,  SkyboxUnit, -SkyboxUnit);  // Top Left;
	    GL11.glEnd();
	    
	    // Render the back quad
	    //clampToEdge();
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(2).getTextureID());
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glNormal3f(0f, 0f, -1f);
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-SkyboxUnit, -SkyboxUnit, -SkyboxUnit);  // Bottom Right
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-SkyboxUnit,  SkyboxUnit, -SkyboxUnit);  // Top Right
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( SkyboxUnit,  SkyboxUnit, -SkyboxUnit);  // Top Left
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( SkyboxUnit, -SkyboxUnit, -SkyboxUnit);  // Bottom Left 
	    GL11.glEnd();
	    
	    // Render the right quad
	    //clampToEdge();	    
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(3).getTextureID());
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glNormal3f(-1f, 0f, 0f);
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( SkyboxUnit, -SkyboxUnit, -SkyboxUnit);  // Bottom Right
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( SkyboxUnit,  SkyboxUnit, -SkyboxUnit);  // Top Right 
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( SkyboxUnit,  SkyboxUnit,  SkyboxUnit);  // Top Left 
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( SkyboxUnit, -SkyboxUnit,  SkyboxUnit);  // Bottom Left 
	    GL11.glEnd();
	    
	    // Render the top quad
	    //clampToEdge();
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(4).getTextureID());
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glNormal3f(0f, -1f, 0f);
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-SkyboxUnit,  SkyboxUnit, -SkyboxUnit);  // Top Left
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-SkyboxUnit,  SkyboxUnit,  SkyboxUnit);  // Bottom Left 
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( SkyboxUnit,  SkyboxUnit,  SkyboxUnit);  // Bottom Right 
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( SkyboxUnit,  SkyboxUnit, -SkyboxUnit);  // Top Right
	    GL11.glEnd();
	    
	    // Render the bottom quad
	    //clampToEdge();
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(5).getTextureID());
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glNormal3f(0f, 1f, 0f);
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-SkyboxUnit, -SkyboxUnit, -SkyboxUnit);  // Top Right
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( SkyboxUnit, -SkyboxUnit, -SkyboxUnit);  // Top Left 
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( SkyboxUnit, -SkyboxUnit,  SkyboxUnit);  // Bottom Left 
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-SkyboxUnit, -SkyboxUnit,  SkyboxUnit);  // Bottom Right
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


package brevis.graphics;

import java.io.IOException;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class BrTexture {
	
	public HashMap<String,Object> properties;
	public Texture texture;
	
	public BrTexture() {
		properties = new HashMap<String,Object>();
	}
	
	public void loadImage( String filename ) throws IOException {
		properties.put( "filename", filename );
		texture = TextureLoader.getTexture( "JPG", 
											ResourceLoader.getResourceAsStream( filename ), 
											GL11.GL_LINEAR );
	}
}



package brevis.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.glu.GLU;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;

//import edu.fhooe.mtd360.watershader.render.Renderer;

// some from http://code.google.com/p/lwjgl-water-shader/
public class BrSky {

	public Vector<Texture> textures;
	public Vector<ImagePlus> textureImps;
	public Vector<Integer> textureIDs;
	
	public BrSky() {
		
		textures = new Vector<Texture>();
		textureImps = new Vector<ImagePlus>();
		textureIDs = new Vector<Integer>();
		
		defaultSkybox();

	}
	
	private ByteBuffer convertImageData(BufferedImage bufferedImage) {		
	    ByteBuffer imageBuffer;
	    WritableRaster raster;
	    BufferedImage texImage;

	    // Color model is 4 byte R G B A, where A is alpha/transparency
	    ColorModel glAlphaColorModel = new ComponentColorModel(ColorSpace
	            .getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 },
	            true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

	    // Raster is 1D interleaved byte array (4 byte strides)
	    raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
	            bufferedImage.getWidth(), bufferedImage.getHeight(), 4, null);
	    texImage = new BufferedImage(glAlphaColorModel, raster, true,
	            new Hashtable());

	    // Allocate a direct ByteBuffer for the image
	    ByteBuffer byteBuffer;
	    DataBuffer dataBuffer = texImage.getRaster().getDataBuffer();

	    if (dataBuffer instanceof DataBufferByte) {
	        byte[] pixelData = ((DataBufferByte) dataBuffer).getData();
	        //byteBuffer = ByteBuffer.wrap(pixelData);// Original
	        byteBuffer = ByteBuffer.allocateDirect(pixelData.length * 4);
	        byteBuffer.put(pixelData);
	    }
	    else if (dataBuffer instanceof DataBufferUShort) {
	        short[] pixelData = ((DataBufferUShort) dataBuffer).getData();
	        byteBuffer = ByteBuffer.allocate(pixelData.length * 2);
	        byteBuffer.asShortBuffer().put(ShortBuffer.wrap(pixelData));
	    }
	    else if (dataBuffer instanceof DataBufferShort) {
	        short[] pixelData = ((DataBufferShort) dataBuffer).getData();
	        byteBuffer = ByteBuffer.allocate(pixelData.length * 2);
	        byteBuffer.asShortBuffer().put(ShortBuffer.wrap(pixelData));
	    }
	    else if (dataBuffer instanceof DataBufferInt) {
	        int[] pixelData = ((DataBufferInt) dataBuffer).getData();
	        byteBuffer = ByteBuffer.allocate(pixelData.length * 4);
	        byteBuffer.asIntBuffer().put(IntBuffer.wrap(pixelData));
	    }
	    else {
	        throw new IllegalArgumentException("Not implemented for data buffer type: " + dataBuffer.getClass());
	    }
	    
	    // We moved our pointer to the end of the image data, flip and we're at the start again.
	    byteBuffer.flip();

	    // ByteBuffers can be used within GL textures
	    return byteBuffer;
	}
	
	public void defaultSkybox() {
		textures.clear();
		//load textures
		try {
			//textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("resources/img/skybox/front.jpg"),GL11.GL_LINEAR));

			/*textureImps.add( IJ.openImage("img/skybox/front.jpg") );
			textureImps.add( IJ.openImage("img/skybox/left.jpg") );
			textureImps.add( IJ.openImage("img/skybox/back.jpg") );
			textureImps.add( IJ.openImage("img/skybox/right.jpg") );
			textureImps.add( IJ.openImage("img/skybox/up.jpg") );			
			textureImps.add( IJ.openImage("img/skybox/down.jpg") );			
			textureImps.add( IJ.openImage("img/skybox/down.jpg") );*/
			
			//textureImps.add( new ImagePlus( "tmp", new ImageStack( 1024, 1024, 1 ) ) ); 
			
			/* Make blank images
			textureImps.add( new ImagePlus( "tmp", new ColorProcessor( 1024, 1024 ) ) ); 
			textureImps.add( new ImagePlus( "tmp", new ColorProcessor( 1024, 1024 ) ) ); 
			textureImps.add( new ImagePlus( "tmp", new ColorProcessor( 1024, 1024 ) ) ); 
			textureImps.add( new ImagePlus( "tmp", new ColorProcessor( 1024, 1024 ) ) ); 
			textureImps.add( new ImagePlus( "tmp", new ColorProcessor( 1024, 1024 ) ) ); 
			textureImps.add( new ImagePlus( "tmp", new ColorProcessor( 1024, 1024 ) ) ); 
			*/
			
			/* Load images */
			textureImps.add( new ImagePlus( "tmp", new ColorProcessor( 1024, 1024 ) ) ); 
			textureImps.add( new ImagePlus( "tmp", new ColorProcessor( 1024, 1024 ) ) ); 
			textureImps.add( new ImagePlus( "tmp", new ColorProcessor( 1024, 1024 ) ) ); 
			textureImps.add( new ImagePlus( "tmp", new ColorProcessor( 1024, 1024 ) ) ); 
			textureImps.add( new ImagePlus( "tmp", new ColorProcessor( 1024, 1024 ) ) ); 
			textureImps.add( new ImagePlus( "tmp", new ColorProcessor( 1024, 1024 ) ) ); 
			
			//for( ImagePlus imp : textureImps ) {
			for( int k = 0; k < textureImps.size(); k++ ) {
				ByteBuffer bb = convertImageData( textureImps.get(k).getBufferedImage() );
				//bb.flip();
				int textureID = GL11.glGenTextures();

				textureIDs.add(textureID);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, textureImps.get(k).getWidth(), textureImps.get(k).getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, bb);
				//GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_GREEN, textureImps.get(k).getWidth(), textureImps.get(k).getHeight(), 0, GL11.GL_GREEN, GL11.GL_UNSIGNED_BYTE, bb);
				//textures.add( AWTTextureIO.newTexture(imp.getBufferedImage(), false) );;
			}
			
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/front.jpg"),GL11.GL_LINEAR));
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/left.jpg"),GL11.GL_LINEAR));
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/back.jpg"),GL11.GL_LINEAR));
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/right.jpg"),GL11.GL_LINEAR));
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/up.jpg"),GL11.GL_LINEAR));			
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/down.jpg"),GL11.GL_LINEAR));			
			textures.add(TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("img/skybox/down.jpg"),GL11.GL_LINEAR));			
		} catch (IOException e) {
			throw new RuntimeException("skybox loading error");
		}
	}
	
	public void loadTextures() {
		if( textureImps.size() >= 6 ) {
			
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
		
	static public float SkyboxUnit = 1500f;
	
	//public void draw() {
	//public void draw( float x, float y, float z ) {
	public void draw( BrCamera cam ) {
		
		GL11.glPushMatrix();
		
		GL11.glDisable (GL11.GL_CULL_FACE);		
		
		GL11.glLoadIdentity();
		cam.setupFrame();
		//GL11.glTranslatef(Renderer.camPosX, Renderer.camPosY, -Renderer.camPosZ);
		//GL11.glTranslatef( x, y, z );
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);		
	    GL11.glDisable(GL11.GL_DEPTH_TEST);
	    GL11.glDepthMask( false );
	    GL11.glDisable(GL11.GL_LIGHTING);
	    GL11.glDisable(GL11.GL_BLEND);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	   
		// Just in case we set all vertices to white.
	    GL11.glColor4f(1,1,1,1);

//			gluBuild2DMipmaps( GL_TEXTURE_2D, 3, width, height,
//	                GL_RGB, GL_UNSIGNED_BYTE, data );
	    
	    //System.out.println( "sky texture info " + textures.get(0).getWidth() + "," + textures.get(0).getHeight() + "\t" + textures.get(0).getTextureWidth() + "," + textures.get(0).getTextureHeight() + "\t" + textures.get(0).getImageWidth() + "," + textures.get(0).getImageHeight() );
	    
	    // Render the front quad
	    clampToEdge();
	    //GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.get(0).getTextureID());
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIDs.get(0));
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glNormal3f(0f, 0f, -1f);
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-SkyboxUnit, -SkyboxUnit,  SkyboxUnit);  // Bottom Left
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( SkyboxUnit, -SkyboxUnit,  SkyboxUnit);  // Bottom Right
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( SkyboxUnit,  SkyboxUnit,  SkyboxUnit);  // Top Right
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-SkyboxUnit,  SkyboxUnit,  SkyboxUnit);  // Top Left	    
	    GL11.glEnd();

	    // Render the left quad
	    clampToEdge();
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIDs.get(1));
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glNormal3f(-1f, 0f, 0f);
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-SkyboxUnit, -SkyboxUnit, -SkyboxUnit);  // Bottom Left
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-SkyboxUnit, -SkyboxUnit,  SkyboxUnit);  // Bottom Right 
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-SkyboxUnit,  SkyboxUnit,  SkyboxUnit);  // Top Right
		    GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-SkyboxUnit,  SkyboxUnit, -SkyboxUnit);  // Top Left;
	    GL11.glEnd();
	    
	    // Render the back quad
	    clampToEdge();
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIDs.get(2));
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glNormal3f(0f, 0f, 1f);
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-SkyboxUnit, -SkyboxUnit, -SkyboxUnit);  // Bottom Right
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-SkyboxUnit,  SkyboxUnit, -SkyboxUnit);  // Top Right
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( SkyboxUnit,  SkyboxUnit, -SkyboxUnit);  // Top Left
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( SkyboxUnit, -SkyboxUnit, -SkyboxUnit);  // Bottom Left 
	    GL11.glEnd();
	    
	    // Render the right quad
	    clampToEdge();	    
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIDs.get(3));
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glNormal3f(1f, 0f, 0f);
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( SkyboxUnit, -SkyboxUnit, -SkyboxUnit);  // Bottom Right
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( SkyboxUnit,  SkyboxUnit, -SkyboxUnit);  // Top Right 
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( SkyboxUnit,  SkyboxUnit,  SkyboxUnit);  // Top Left 
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( SkyboxUnit, -SkyboxUnit,  SkyboxUnit);  // Bottom Left 
	    GL11.glEnd();
	    
	    // Render the top quad
	    clampToEdge();
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIDs.get(4));
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glNormal3f(0f, 1f, 0f);
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-SkyboxUnit,  SkyboxUnit, -SkyboxUnit);  // Top Left
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-SkyboxUnit,  SkyboxUnit,  SkyboxUnit);  // Bottom Left 
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( SkyboxUnit,  SkyboxUnit,  SkyboxUnit);  // Bottom Right 
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( SkyboxUnit,  SkyboxUnit, -SkyboxUnit);  // Top Right
	    GL11.glEnd();
	    
	    // Render the bottom quad
	    clampToEdge();
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureIDs.get(5));
	    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glNormal3f(0f, -1f, 0f);
			GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-SkyboxUnit, -SkyboxUnit, -SkyboxUnit);  // Top Right
			GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( SkyboxUnit, -SkyboxUnit, -SkyboxUnit);  // Top Left 
			GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( SkyboxUnit, -SkyboxUnit,  SkyboxUnit);  // Bottom Left 
			GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-SkyboxUnit, -SkyboxUnit,  SkyboxUnit);  // Bottom Right
	    GL11.glEnd();
	 
	    // Restore enable bits and matrix
	    GL11.glPopAttrib();
	    GL11.glPopMatrix();
	    
	    //GL11.glDepthMask( false );
	    //GL11.glEnable( GL11.GL_DEPTH_TEST );
	    //GL11.glEnable (GL11.GL_CULL_FACE);
		//GL11.glCullFace (GL11.GL_BACK);
	}

	//clamp textures, so edges get dont create a line in between
	private void clampToEdge() {
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
	}

}


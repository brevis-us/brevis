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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 * A BrLight controls a single point light source in the simulation. Generally
 * this is only used for rendering, but some simulations may use it in models.
 * 
 * @author kyle
 *
 */
public class BrLight {
	private static final float LIGHTX = 1.0f;
    private static final float LIGHTY = 0.4f;
    
    //static ByteBuffer byteBuffer;
    //static ByteBuffer floatBuffer;
    
    static float lightPos[] = { 50.0f, 200.0f, 50.0f, 0.0f};           // Light Position                                                                               
    static float lightAmb[] = { 0.75f, 0.75f, 0.75f, 1.0f};           // Ambient Light Values                                                                         
    static float lightDif[] = { 0.6f, 0.6f, 0.6f, 1.0f};           // Diffuse Light Values                                                                         
    static float lightSpc[] = {-0.2f, -0.2f, -0.2f, 1.0f};         // Specular Light Values    
	
	private final FloatBuffer light_position =  BufferUtils.createFloatBuffer(4);
	private final FloatBuffer light_ambient = BufferUtils.createFloatBuffer(4);
	private final FloatBuffer light_diffuse = BufferUtils.createFloatBuffer(4);
	private final FloatBuffer light_specular = BufferUtils.createFloatBuffer(4);
	/*static {
		light_position.put(new float[] { LIGHTX, LIGHTY, 1.0f, 0.0f }).flip();
		light_ambient.put(new float[]{ 0.5f, 0.5f, 0.5f, 1.0f }).flip();
		light_diffuse.put(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }).flip();
		light_specular.put(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }).flip();
	}*/	
	
	public BrLight() {
		/*floatBuffer = ByteBuffer.allocateDirect(64);
        floatBuffer.order(ByteOrder.nativeOrder());
        byteBuffer = ByteBuffer.allocateDirect(16);
        byteBuffer.order(ByteOrder.nativeOrder());*/
		light_position.put( lightPos ).flip();
		light_ambient.put( lightAmb ).flip();
		light_diffuse.put( lightDif ).flip();
		light_specular.put( lightSpc ).flip();
	}

	public void enable() {
	    GL11.glEnable(GL11.GL_LIGHT1);                                // Enable Light1                                                                      
	    GL11.glEnable(GL11.GL_LIGHTING);                              // Enable Lighting
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, light_position );        // Set Light1 Position         
	    GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, light_ambient );         // Set Light1 Ambience         
	    GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, light_diffuse );         // Set Light1 Diffuse          
	    GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, light_specular );        // Set Light1 Specular         
	}
	
	public void setPosition() {
		GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, light_position );        // Set Light1 Position         
	}
	
	/**
	 * Set the ambient color of this light
	 * @param newAmb
	 */
	public void setAmbient(float[] newAmb) {
		lightAmb = newAmb;
		light_ambient.put( lightAmb ).flip();				
	}
	
	/**
	 * Set the position of this light
	 * @param newPos
	 */
	public void setPosition(float[] newPos) {
		lightPos = newPos;
		light_position.put( lightPos ).flip();
	}
	
	/**
	 * Set the diffuse color of this light
	 * @param newDif
	 */
	public void setDiffuse(float[] newDif) {
		lightDif = newDif;
		light_diffuse.put( lightDif ).flip();
	}
	
	/**
	 * Set the specular color of this light
	 * @param newSpc
	 */
	public void setSpecular(float[] newSpc) {
		lightSpc = newSpc;
		light_specular.put( lightSpc ).flip();
	}
	
	/**
	 * Get the ambient color of this light
	 * @return 
	 */
	public float[] getAmbient() {
		return lightAmb;
	}
	
	/**
	 * Get the position of this light
	 * @return
	 */
	public float[] getPosition() {
		return lightPos;
	}
	
	/**
	 * Get the diffuse color of this light
	 * @return
	 */
	public float[] getDiffuse() {
		return lightDif;
	}
	
	/**
	 * Get the specular color of this light
	 * @return
	 */
	public float[] getSpecular() {
		return lightSpc;
	}
	
}

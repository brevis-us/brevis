package brevis.graphics;

import javax.vecmath.Vector3f;

import static java.lang.Math.*;

import org.lwjgl.opengl.GL11;
//import org.lwjgl.util.vector.Matrix4f;
//import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

import static org.lwjgl.opengl.ARBDepthClamp.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL11.*;

// was based on http://www.lloydgoodall.com/tutorials/first-person-camera-control-with-lwjgl/
// now based on https://gist.github.com/DziNeIT/4206709
// plus some code from: https://github.com/tzaeschke/ode4j

public class BrCamera {
	// Position x y z
	public float x = 0;
	public float y = 0;
	public float z = 0;
	// Rotation pitch yaw roll
	public float pitch = 0;
	public float yaw = 0;
	public float roll = 0;
	// Field of View
	private float fov = 90;
	// Aspect Ratio
	private float aspectRatio = 1;
	// nearClippingPlane = How close to the camera and behind isn't rendered
	private final float nearClippingPlane;
	// farClippingPlane = Render distance from the camera
	private final float farClippingPlane;
	public float width;
	public float height;
	
	public String toString() {
		return "{x " + x + ", y " + y +  ", z " + z +
				", roll " + roll + ", pitch " + pitch + ", yaw " + yaw + 
				", fov " + fov + ", width " + width + ", height " + height + 
				", aspectRatio " + aspectRatio + ", nearClippingPlane " + nearClippingPlane + ", farClippingPlane " + farClippingPlane + "}";
	}
	
	private static final double DEG_TO_RAD = Math.PI/180.0; 

	public BrCamera(float x, float y, float z, 
			float pitch, float yaw, float roll, 
			float fov, float width, float height, float zNear, float zFar) {
		super();

		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
		this.roll = roll;
		this.fov = fov;
		this.aspectRatio = ( width/height > 1 ? width/height : height/width );
		this.width = width;
		this.height = height;
		this.nearClippingPlane = zNear;
		this.farClippingPlane = zFar;
	}
	
	public void setDimensions( float width, float height ) {
		this.width = width;
		this.height = height;
		this.aspectRatio = ( width/height > 1 ? width/height : height/width );
		GL11.glViewport(0,0,(int)width,(int)height);                           // Reset The Current Viewport
		
		GLU.gluPerspective(fov,
                (float) width / (float) height,
                nearClippingPlane, farClippingPlane);
	}

	public void processMouse( float dx, float dy, float mouseSpeed) {
		processMouse(dx, dy, mouseSpeed, 90, -90);
	}

	/**
	 * Processes mouse movements
	 *
	 * @param mouseSpeed Speed of movement based on DX
	 * @param maxLookUp Maximum angle that can be looked up
	 * @param maxLookDown Minimum angle that can be looked down
	 */
	/*public void processMouse( float dx, float dy, float mouseSpeed, float maxLookUp, float maxLookDown) {
		float mouseDX = dx * mouseSpeed * 0.16f;
		float mouseDY = dy * mouseSpeed * 0.16f;
		if (yaw + mouseDX >= 360) {
			yaw = yaw + mouseDX - 360;
		//} else if (yaw + mouseDX < 0) {
		//	yaw = 360 - yaw + mouseDX;
		} else if ( yaw + mouseDX < - 360 ) {
			yaw = 360 - yaw + mouseDX;
		} else {
			yaw += mouseDX;
		}
		if (pitch - mouseDY >= maxLookDown && pitch - mouseDY <= maxLookUp) {
			pitch += -mouseDY;
		} else if (pitch - mouseDY < maxLookDown) {
			pitch = maxLookDown;
		} else if (pitch - mouseDY > maxLookUp) {
			pitch = maxLookUp;
		}
	}*/
	
	public void processMouse( float dx, float dy, float mouseSpeed, float maxLookUp, float maxLookDown) {
		//float side = 0.01f * dx;
		//float s = (float) Math.sin (yaw*DEG_TO_RAD);
		//float c = (float) Math.cos (yaw*DEG_TO_RAD);

		//roll += dx * 0.5f;
		yaw += dx * 0.5f;
		pitch += dy * 0.5f;

		while (roll > 180) roll -= 360;
		while (roll < -180) roll += 360;
		while (pitch > 180) pitch -= 360;
		while (pitch < -180) pitch += 360;
		while (yaw > 180) yaw -= 360;
		while (yaw < -180) yaw += 360;
		
	}
	
	public void rotateFromLook( float dr, float dp, float dw ) {
		roll += dr;
		pitch += dp;
		yaw += dw;
		
		while (roll > 180) roll -= 360;
		while (roll < -180) roll += 360;
		while (pitch > 180) pitch -= 360;
		while (pitch < -180) pitch += 360;
		while (yaw > 180) yaw -= 360;
		while (yaw < -180) yaw += 360;
	}

	public void processKeyboard(float delta,  boolean up, boolean down, boolean left, boolean right, boolean rise, boolean sink) {
		processKeyboard(delta, 1,   up,  down,  left,  right,  rise,  sink);
	}

	/**
	 * Processes Keyboard presses using given delta and player movement speed
	 *
	 * @param delta Delta time since last call
	 * @param speed Speed of camera movement
	 */
	public void processKeyboard(float delta, float speed, boolean up, boolean down, boolean left, boolean right, boolean rise, boolean sink) {
		if (delta <= 0) {
			throw new IllegalArgumentException("delta is 0 or is smaller than 0");
		}

		/*boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W);
		boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S);
		boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A);
		boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D);
		boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
		boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);*/
		
		boolean keyUp = up;
		boolean keyDown = down;
		boolean keyLeft = left;
		boolean keyRight = right;
		boolean flyUp = rise;
		boolean flyDown = sink;

		if (keyUp && keyRight && !keyLeft && !keyDown) {
			moveFromLook(speed * delta * 0.003f, 0, -speed * delta * 0.003f);
		}
		if (keyUp && keyLeft && !keyRight && !keyDown) {
			moveFromLook(-speed * delta * 0.003f, 0, -speed * delta * 0.003f);
		}
		if (keyUp && !keyLeft && !keyRight && !keyDown) {
			moveFromLook(0, 0, -speed * delta * 0.003f);
		}
		if (keyDown && keyLeft && !keyRight && !keyUp) {
			moveFromLook(-speed * delta * 0.003f, 0, speed * delta * 0.003f);
		}
		if (keyDown && keyRight && !keyLeft && !keyUp) {
			moveFromLook(speed * delta * 0.003f, 0, speed * delta * 0.003f);
		}
		if (keyDown && !keyUp && !keyLeft && !keyRight) {
			moveFromLook(0, 0, speed * delta * 0.003f);
		}
		if (keyLeft && !keyRight && !keyUp && !keyDown) {
			moveFromLook(-speed * delta * 0.003f, 0, 0);
		}
		if (keyRight && !keyLeft && !keyUp && !keyDown) {
			moveFromLook(speed * delta * 0.003f, 0, 0);
		}
		if (flyUp && !flyDown) {
			y += speed * delta * 0.003f;
		}
		if (flyDown && !flyUp) {
			y -= speed * delta * 0.003f;
		}
	}

	/**
	 * Moves camera based on Mouse movements
	 *
	 * @param dx Mouse x movement
	 * @param dy Mouse y movement
	 * @param dz Mouse z movement
	 */
	public void moveFromLook(float dx, float dy, float dz) {
		this.z += dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw));
		this.x -= dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw));
		this.y += dy * (float) sin(toRadians(pitch - 90)) + dz * sin(toRadians(pitch));
	}

	/**
	 * Applies optimal states
	 */
	public void optimiseStates() {
		if (GLContext.getCapabilities().GL_ARB_depth_clamp) {
			glEnable(GL_DEPTH_CLAMP);
		}
	}

	/**
	 * Applies the orthographic matrix (GL11.glOrtho)
	 */
	public void orthographicMatrix() {
		glPushAttrib(GL_TRANSFORM_BIT);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(-aspectRatio, aspectRatio, -1, 1, 0, farClippingPlane);
		glPopAttrib();
	}

	/**
	 * Applies the perspective matrix (GLU.gluPerspective)
	 */
	public void perspectiveMatrix() {
		glPushAttrib(GL_TRANSFORM_BIT);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		GLU.gluPerspective(fov, aspectRatio, nearClippingPlane, farClippingPlane);
		glPopAttrib();
	}
	
	public void setupFrame() {
		GL11.glViewport(0,0,(int)width,(int)height);                           // Reset The Current Viewport		
		//GLU.gluPerspective(fov, (float) width / (float) height, nearClippingPlane, farClippingPlane);
		
		GL11.glMatrixMode( GL11.GL_PROJECTION );
		GL11.glLoadIdentity();
		final float vnear = nearClippingPlane;
		final float vfar = farClippingPlane;
		//final float k = aspectRatio;     // view scale, 1 = +/- 45 degrees
		final float k = 0.8f;     // view scale, 1 = +/- 45 degrees
		if (width >= height) {
			float k2 = (float)height/(float)width;
			GL11.glFrustum (-vnear*k,vnear*k,-vnear*k*k2,vnear*k*k2,vnear,vfar);
		}
		else {
			float k2 = (float)width/(float)height;
			GL11.glFrustum (-vnear*k*k2,vnear*k*k2,-vnear*k,vnear*k,vnear,vfar);
		}
		
		GL11.glMatrixMode( GL11.GL_MODELVIEW );
		translate();
		//GLU.gluPerspective(fov, (float) width / (float) height, nearClippingPlane, farClippingPlane);
	}

	/**
	 * Translates camera position to OpenGL position
	 */
	public void translate() {
		//glPushAttrib(GL_TRANSFORM_BIT);
		glMatrixMode(GL_MODELVIEW);
		//glRotatef (90, 0,0,1);
		//glRotatef (90, 0,1,0);		
		glRotatef(roll, 1, 0, 0);
		glRotatef(pitch, 0, 1, 0);
		glRotatef(yaw, 0, 0, 1);
		glTranslatef(-x, -y, -z);
		//glPopAttrib();
	}
}

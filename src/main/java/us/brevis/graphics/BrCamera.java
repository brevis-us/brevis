package us.brevis.graphics;

import org.joml.Vector4f;
import sc.iview.vector.JOMLVector3;
import sc.iview.vector.Vector3;

import static java.lang.Math.*;

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
	public Vector4f rotation;
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
	
	public int colorTextureID = -1;
	public int framebufferID = -1;
	public int depthRenderBufferID = -1;
	
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
		this.rotation = new Vector4f( 1, 0, 0, 0 );
	}
	
	public void setDimensions( float width, float height ) {
		this.width = width;
		this.height = height;
		this.aspectRatio = ( width/height > 1 ? width/height : height/width );
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
		// Orig
		/*this.z += dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw));
		this.x -= dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw));
		this.y += dy * (float) sin(toRadians(pitch - 90)) + dz * sin(toRadians(pitch));*/
		
		this.z += dx * (float) cos(toRadians(pitch - 90)) + dz * cos(toRadians(pitch));
		this.x -= dx * (float) sin(toRadians(pitch - 90)) + dz * sin(toRadians(pitch));
		this.y += dy * (float) sin(toRadians(roll - 90)) + dz * sin(toRadians(roll));
	}
	


	public void setPosition( Vector3 v ) {
		x = v.xf();
		y = v.yf();
		z = v.zf();
	}
	
	public Vector3 getPosition() {
		return ( new JOMLVector3( x, y, z ) );
	}
	
	public Vector4f getRotation() {
		return rotation;
	}
	
	public void setRotation( Vector4f v ) {
		rotation = v;
		roll = v.x;
		pitch = v.y;
		yaw = v.z;
	}

}

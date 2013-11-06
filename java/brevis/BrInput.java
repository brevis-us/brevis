package brevis;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class BrInput {
	public static class InputType {
		public boolean mouseType = false;
		public boolean keyboardType = false;
		public String id = "";
		
		/**
		 * Test if this input is active
		 * @return true if this input is activated
		 */
		public boolean test() {
			return false;
		}
	}
	
	public static class InputHandler {
		public void trigger( Engine engine ) {
			System.out.println( "InputHandler triggered." );
		}
	}
	
	protected HashMap<InputType,InputHandler> inputHandlers;	
	
	public BrInput() throws LWJGLException {
		inputHandlers = new HashMap<InputType,InputHandler>();
		
		Mouse.create();
		Keyboard.create();
	}
	
	/**
	 * Check input state and call activated input handlers
	 */
	public void pollInput( Engine engine ) {
		for( Map.Entry<InputType,InputHandler> entry : inputHandlers.entrySet() ) {
			if( entry.getKey().test() ) {
				entry.getValue().trigger( engine );
			}
		}
	}
}

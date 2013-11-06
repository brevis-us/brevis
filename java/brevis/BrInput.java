package brevis;

import java.util.ArrayList;
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
	
	static public InputType makeInputType( String inputClass, ArrayList<String> parms ) {
		InputType it = new InputType();
		if( inputClass.contains("mouse") ) it.mouseType = true;
		if( inputClass.contains("keyboard") ) it.keyboardType = true;
		it.id = parms.get(0);
		return it;
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
	
	public void addInputHandler( InputType it, InputHandler ih ) {
		inputHandlers.put( it, ih );
	}
}

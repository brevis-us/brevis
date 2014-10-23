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
		public int mouseButton = -1;
		public int keyIndex = -1;
		
		public void setKey( String keyName ) {
			id = keyName;
			keyIndex = Keyboard.getKeyIndex( keyName );
			if( keyName.contains( "LSHIFT" ) ) keyIndex = 42;
			keyboardType = true;
		}
		
		public void setButton( String buttonName ) {
			id = buttonName;
			mouseType = true;
			if( buttonName.contains("LEFT") ) {
				mouseButton = 0;
			} else if( buttonName.contains("MIDDLE") ) {
				mouseButton = 1;
			} else if( buttonName.contains("RIGHT") ) {
				mouseButton = 2;
			}
		}
		
		/**
		 * Test if this input is active
		 * @return true if this input is activated
		 */
		public boolean test() {
			boolean ret = false;
			//Mouse.poll();
			//Keyboard.poll();
			if( mouseType ) {				
				return Mouse.isButtonDown( mouseButton );
			} else if( keyboardType ) {
				return Keyboard.isKeyDown( keyIndex );
			}
			return ret;
		}
	}
	
	static public InputType makeInputType( String inputClass, ArrayList<String> parms ) {
		InputType it = new InputType();
		if( inputClass.contains("mouse") ) {
			it.mouseType = true;
			it.setButton( parms.get(0) );
		} else if( inputClass.contains("key") ) {
			it.keyboardType = true;
			it.setKey( parms.get(0) );
		}
		return it;
	}
	
	static public int getMouseDX() {
		return Mouse.getDX();
	}
	
	static public int getMouseDY() {
		return Mouse.getDY();
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
	 * This version can miss keys, and trigger multiple times
	 */
	public void pollInput( Engine engine ) {
		//Keyboard.poll();
		//Mouse.poll();
		for( Map.Entry<InputType,InputHandler> entry : inputHandlers.entrySet() ) {
			if( entry.getKey().test() ) {
				entry.getValue().trigger( engine );
				
			}
		}
	}
	
	public static boolean isRepeatEvent() {
		return Keyboard.isRepeatEvent();
	}
	
	public void addInputHandler( InputType it, InputHandler ih ) {
		inputHandlers.put( it, ih );
	}
}

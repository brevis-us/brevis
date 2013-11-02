/*
 * brevis is free software: you can redistribute it and/or modify                                                                                                           
    it under the terms of the GNU General Public License as published by                                                                                                             
    the Free Software Foundation, either version 3 of the License, or                                                                                                                
    (at your option) any later version.                                                                                                                                              
                                                                                                                                                                                     
    brevis is distributed in the hope that it will be useful,                                                                                                                
    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                                                                                   
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                                                                                    
    GNU General Public License for more details.                                                                                                                                     
                                                                                                                                                                                     
    You should have received a copy of the GNU General Public License                                                                                                                
    along with brevis.  If not, see <http://www.gnu.org/licenses/>.                                                                                                          
                                                                                                                                                                                     
	Copyright 2012, 2013 Kyle Harrington
 */

package brevis;

import javax.vecmath.Vector3d;

import org.lwjgl.util.vector.Vector3f;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;

public class Utils {
	/*public static Vector3d DVector3CToVector3d( DVector3C odev ) {
		return new Vector3d( odev.get0(), odev.get1(), odev.get2() );
	}*/
	public static Vector3f DVector3CToVector3f( DVector3C odev ) {
		return new Vector3f( (float)odev.get0(), (float)odev.get1(), (float)odev.get2() );
	}
	
	/*public static DVector3 Vector3dToDVector3( Vector3d javav ) {
		return new DVector3( javav.x, javav.y, javav.z );
	}*/
	
	public static DVector3 Vector3fToDVector3( Vector3f javav ) {
		return new DVector3( javav.x, javav.y, javav.z );
	}
}

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

import org.ode4j.math.DVector3;
import org.ode4j.ode.DJointGroup;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

public class BrPhysics {
	public DWorld world;
	public DSpace space;
	public DJointGroup contactGroup;
	public double time = 0;
	
	BrPhysics() {
		world = OdeHelper.createWorld();
		world.setGravity( new DVector3(0, 0, 0) );
		
		space = OdeHelper.createHashSpace();
		
		contactGroup = OdeHelper.createJointGroup();
		
		time = 0;
	}
	
	public DWorld getWorld() {
		return world;
	}
	
	public double getTime() {
		return time;
	}
	
	public DSpace getSpace() {
		return space;
	}
}

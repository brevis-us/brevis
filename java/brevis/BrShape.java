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

import org.ode4j.ode.DGeom;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.OdeHelper;

public class BrShape {
	public enum BrShapeType {
		BOX, SPHERE, CONE, CYLINDER, MESH
	};
	
	public BrShapeType type;
	public Vector3d dim;
	
	BrShape( BrShapeType t, Vector3d d ) {
		//type = BrShapeType.SPHERE;
		//dim = new Vector3d(1,1,1);
		type = t;
		dim = d;
	}
	
	public void draw() {
		
	}
	
	public String getType() {
		if( type == BrShapeType.BOX ) {
			return "box";
		} else if( type == BrShapeType.SPHERE ) {
			return "sphere";
		} else if( type == BrShapeType.CONE ) {
			return "cone";			
		} else if( type == BrShapeType.CYLINDER ) {
			return "cylinder";
		} else if( type == BrShapeType.MESH ) {
			return "mesh";
		} else {
			return "unknown";
		}
	}
	
	/*
	 * Return a mass that is appropriate for this object and its dimensions
	 */
	public DMass createMass( double density ) {
		DMass m = OdeHelper.createMass();
		if( type == BrShapeType.BOX ) {
			m.setBox(density, dim.x, dim.y, dim.z );
		} else if( type == BrShapeType.SPHERE ) {
			m.setSphere( density, dim.x );
		} else if( type == BrShapeType.CONE ) {
			m.setSphere(density, dim.x);
		} else if( type == BrShapeType.CYLINDER ) {
			m.setSphere(density, dim.x);
		}
		return m;
	}
	
	public DGeom createGeom( DSpace space ) {
		switch( type ) {
		case BOX:
			return OdeHelper.createBox( space, dim.x, dim.y, dim.z );			
		default:
		case SPHERE:
			return OdeHelper.createSphere( space, dim.x );			
		}		
	}
	
	public void setDimension( Vector3d newd ) {
		dim = newd;
	}
	
	public Vector3d getDimension() {
		return dim;
	}
	
	public static BrShape createSphere( double r ) {
		return ( new BrShape( BrShapeType.SPHERE, new Vector3d( r, 25, r ) ) );
	}
	
	public static BrShape createBox( double x, double y, double z ) {
		return ( new BrShape( BrShapeType.BOX, new Vector3d( x, y, z ) ) );
	}
	
	public static BrShape createCone( double length, double base ) {
		return ( new BrShape( BrShapeType.CONE, new Vector3d( length, base, 25 )));	// last element of vector is # of sides or stacks (depending on renderer)
	}
	
	public static BrShape createCylinder( double length, double radius ) {
		return ( new BrShape( BrShapeType.CYLINDER, new Vector3d( length, radius, 25 )));	// last element of vector is # of sides or stacks (depending on renderer)
	}
}

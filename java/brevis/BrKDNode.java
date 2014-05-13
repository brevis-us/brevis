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

package brevis;

import java.util.Arrays;

public class BrKDNode {

	//public final Vector3d loc;
	public final double[] domain;
	
	public final long UID;
	
	/*public BrKDNode( Vector3d newLoc ) {
		this.loc = newLoc; 
	}*/
	
	public BrKDNode( double[] newDomain, long newUID ) {
		this.domain = newDomain;
		this.UID = newUID;
	}
	
	/*public final boolean collocated( final BrKDNode other ) {
		return loc.equals( other );
	}*/
	
	public final boolean collocated( final BrKDNode other ) {
		return Arrays.equals( other.domain, domain );
	}
	
}

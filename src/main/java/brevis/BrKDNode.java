
package brevis;

import java.util.Arrays;

public class BrKDNode {

	//public final Vector3d loc;
	
	//public final double[] domain;
	public double[] domain;
	
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

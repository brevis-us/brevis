
package us.brevis;

import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import org.joml.Vector3f;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DHingeJoint;
import org.ode4j.ode.DJoint;
import org.ode4j.ode.DJointGroup;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

public class BrPhysics implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1347085003075216668L;
	public DWorld world;
	public DSpace space;
	public DJointGroup contactGroup;
	public double time = 0;
	
	public Vector<DJoint> joints = new Vector<DJoint>();
	
	public class BrJoint {
		public DJoint joint;
		public String type;
		public BrJoint( DJoint j, String t ) {
			joint = j;
			type = t;
		}
		public DJoint getJoint() {
			return joint;
		}
		public void setJoint(DJoint j) {
			joint = j;
		}
		public String getType() {
			return type;
		}
		public void setType( String s ) {
			type = s;
		}
		/*
		 * Set velocity and other joint params
		 */
	}
	
	BrPhysics() {
		world = OdeHelper.createWorld();
		world.setGravity( new DVector3(0, 0, 0) );
		
		space = OdeHelper.createHashSpace();
		
		contactGroup = OdeHelper.createJointGroup();
		
		time = 0;
	}
	
	public BrJoint jointHinge(BrObject objA, BrObject objB, Vector3f locationOnA, Vector3f axis ) {
		DHingeJoint joint = OdeHelper.createHingeJoint(world);
		joint.attach( objA.getBody(), objB.getBody() );
		joint.setAnchor( locationOnA.x, locationOnA.y, locationOnA.z );
		joint.setAxis( (double)axis.x, (double)axis.y, (double)axis.z );
		joint.setParamHiStop( Math.PI/ 2 );
        joint.setParamLoStop( Math.PI / 2 ); 
		joint.enable();
		joints.add( joint );
		
		BrJoint brj = new BrJoint( joint, "hinge2" );
		
		return brj;
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
	
	public DJointGroup getContactGroup() {
		return contactGroup;
	}
	
	/* Empty the current contact group. Probably want to do this before each simulation step. */
	public void clearContactGroup() {
		contactGroup.clear();
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		 out.defaultWriteObject();
	}
		     
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
}

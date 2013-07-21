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
}

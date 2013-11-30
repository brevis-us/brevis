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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.vecmath.Vector3d;

import org.lwjgl.util.vector.Vector3f;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import clojure.lang.PersistentVector;
import duyn.algorithm.nearestneighbours.FastKdTree;
import duyn.algorithm.nearestneighbours.PrioNode;

public class Engine {	
	
	/*
	 * Callback classes. 
	 */
	
	// Compute the update after DT amount of time for object with UID
	public static class UpdateHandler {
		public BrObject update( Engine engine, Long uid, Double dt ) {
			BrObject obj = engine.objects.get( uid );
			return obj;
		}
	}
	
	public static class GlobalUpdateHandler {
		public Long priority = (long) 0;
		public void update( Engine engine ) {			
		}
		public Long getPriority () {
			return priority;
		}
		public void setPriority( Long priority2 ) {
			priority = priority2;
		}
	}
	
	// All collisions are pairwise now.
	// Compute the collision after DT amount of time for object with UID colliding with UID, other	
	/*public static class CollisionHandler {
		public BrObject collide( BrObject subj, BrObject othr, Double dt) {			
			return subj;
		}
	}*/
	
	public static class CollisionHandler {
		public clojure.lang.PersistentVector collide( Engine engine, BrObject subj, BrObject othr, Double dt) {
			clojure.lang.PersistentVector v = clojure.lang.PersistentVector.create( subj, othr );						
			return v;
		}
	}
	
	/* 
	 * Variables:
	 */
	
	// updateHandlers
	protected HashMap<String,UpdateHandler> updateHandlers;
	protected HashMap<String,Boolean> updateKinematics;
	protected PriorityQueue<GlobalUpdateHandler> globalUpdateHandlers; 
	// dt
	public double dt = 1.0;
	// neighborhoodRadius
	public double neighborhoodRadius = 25.0;
	// physics
	public BrPhysics physics;
	
	protected long simulationStart = -1;
	protected long numSteps = 0;
	
	public double startWallTime = 0;
	
	// objects
	//protected HashMap<Long,BrObject> objects;	
	protected ConcurrentHashMap<Long,BrObject> objects;	
	// addedObjects
	protected HashMap<Long,BrObject> addedObjects;
	// deletedObjects	
	protected HashSet<Long> deletedObjects;
	
	// collisionHandlers
	protected HashMap< SimpleEntry<String,String>, CollisionHandler > collisionHandlers;
	// collisions
	protected HashSet< SimpleEntry<Long,Long> > collisions;
	// DEPRECATED: temporary variable for boostrapped version
	public static HashSet< SimpleEntry<Long,Long> > globalCollisions;
	
	// physicsEnabled
	public boolean physicsEnabled = true;
	// collisionsEnabled
	public boolean collisionsEnabled = true;
	// neighborhoodsEnabled
	public boolean neighborhoodsEnabled = true;
	
	// enableParallel
	public boolean brevisParallel = true;
	
	public BrKDTree<BrKDNode> spaceTree = null;
	
	/* Methods: */	
	
	class GUHComparator implements Comparator<GlobalUpdateHandler> {

		@Override
		public int compare(GlobalUpdateHandler gh1, GlobalUpdateHandler gh2) {
			return (int) ( gh1.priority - gh2.priority );
		}
		
	}
	
	public Engine() {
		updateHandlers = new HashMap<String,UpdateHandler>();		
		updateKinematics = new HashMap<String,Boolean>();		
		physics = new BrPhysics();
		//objects = new HashMap<Long,BrObject>();
		objects = new ConcurrentHashMap<Long,BrObject>();
		addedObjects = new HashMap<Long,BrObject>();
		deletedObjects = new HashSet<Long>();
		
		collisionHandlers = new HashMap< SimpleEntry<String,String>, CollisionHandler >();
		collisions = new HashSet< SimpleEntry<Long,Long> >();
		globalCollisions = new HashSet< SimpleEntry<Long,Long> >();
		
		globalUpdateHandlers = 
				new PriorityQueue<GlobalUpdateHandler>(1, (Comparator<GlobalUpdateHandler>) new GUHComparator() );
		
		simulationStart = System.nanoTime();
		
		spaceTree = new BrKDTree<BrKDNode>();
	}
	
	public static class BrevisCollision implements DGeom.DNearCallback {

		@SuppressWarnings("unchecked")
		@Override
		public void call(Object data, DGeom o1, DGeom o2) {
			HashMap<String,Object> o1map = (HashMap<String,Object>)o1.getBody().getData();
			HashMap<String,Object> o2map = (HashMap<String,Object>)o2.getBody().getData();
			Long uid1 = (Long)o1map.get("uid");
			Long uid2 = (Long)o2map.get("uid");
			SimpleEntry<Long,Long> p1 = new SimpleEntry<Long,Long>( uid1, uid2 );
			SimpleEntry<Long,Long> p2 = new SimpleEntry<Long,Long>( uid2, uid1 );
			//System.out.println( "collision callback " + p1 + " " + p2  + " " + o1map + " " + o2map );
			Engine.globalCollisions.add( p1 );
			Engine.globalCollisions.add( p2 );
			
			/*if (physicsEnabled) {
				// Also do a physics collision?
			}*/
		}
		
	}	
	
	/* updatePhysics
	 * Move according to physics
	 */
	public void updatePhysics( double dt ) {
		physics.contactGroup.empty();
		OdeHelper.spaceCollide( physics.space, null, new BrevisCollision() );		
		physics.world.quickStep( dt );						
		
		physics.time += dt;		
	}
	
	/* synchronizeObjects
	 * Apply all insertions/deletions
	 */
	public void synchronizeObjects() {
		//System.out.println( "synchronizeObjects del: " + deletedObjects.size() + " add: " + addedObjects.size() );
		// Remove deleted objects
		for( Long uid : deletedObjects ) {
			objects.remove( uid );
		}
		deletedObjects = new HashSet<Long>();
		
		// Add newly created objects
		objects.putAll( addedObjects );
		addedObjects = new HashMap<Long,BrObject>();
	}
	
	/* updateObjects
	 * Call individual update functions
	 */
	public void updateObjects( double dt ) {
		//HashMap<Long,BrObject> updatedObjects = new HashMap<Long,BrObject>();
		ConcurrentHashMap<Long,BrObject> updatedObjects = new ConcurrentHashMap<Long,BrObject>();
		
		// Call the 0 update handler once (eliminating this implementation)
		/*if( updateHandlers.containsKey( 0 ) ) {
			UpdateHandler global_uh = updateHandlers.get( 0 );
			BrObject placeholder = global_uh.update( this, null, dt );
		}*/		
		
		//System.out.println( "updateObjects " + objects.keySet() );
		for( Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
			BrObject obj = entry.getValue();
			UpdateHandler uh = updateHandlers.get( obj.type );
			
			BrObject newObj = obj;
			if( uh != null ) {
				//System.out.println( "--" + getTime() + " updating object " + entry.getKey() );
				newObj = uh.update( this, entry.getKey(), dt );				
			} 
			//else System.out.println( "--" + getTime() + " not updating object " + entry.getKey() + " " + entry.getValue().type );
			
			Boolean kh = updateKinematics.get( obj.type );
			//System.out.println( obj.type + " " + kh );
			if( kh != null && kh ) {
				newObj.updateObjectKinematics( dt );
			}
			
			updatedObjects.put( entry.getKey(), newObj );
		}
		objects = updatedObjects;
	}
	
	/* globalUpdateObjects
	 * Call individual update functions
	 */
	public void globalUpdateObjects( boolean preIndividual ) {
		
		for( GlobalUpdateHandler gh : globalUpdateHandlers ) {
			//System.out.println( "guh " + gh );
			if( preIndividual && gh.getPriority() < 0 ) {
//				System.out.println( "globalUpdate " + gh );
				gh.update( this );
				synchronizeObjects();
			} else if ( !preIndividual && gh.getPriority() >= 0 ) {
				//System.out.println( "globalUpdate " + gh );
				gh.update( this );
				synchronizeObjects();
			}
		}
	}
	
	/* handleCollisions
	 * Respond to all computed collisions
	 */
	public void handleCollisions( double dt ) {
		collisions = globalCollisions;
		
		//HashMap<Long,BrObject> updatedObjects = new HashMap<Long,BrObject>();
		ConcurrentHashMap<Long,BrObject> updatedObjects = new ConcurrentHashMap<Long,BrObject>();		
		
		// An object may be involved in multiple computations, therefore we first duplicate all objects and use
		// these.
		for( Entry<Long, BrObject> entry : objects.entrySet() ) {
			updatedObjects.put( entry.getKey(), entry.getValue() );
		}
		
		//System.out.println( "handleCollisions " + collisions );
		for( SimpleEntry<Long,Long> entry: collisions ) {
			BrObject subj = updatedObjects.get( entry.getKey() );
			BrObject othr = updatedObjects.get( entry.getValue() );
			if( subj != null && othr != null ) {
				SimpleEntry<String,String> typeEntry = new SimpleEntry<String,String>(subj.type,othr.type);
				CollisionHandler ch = collisionHandlers.get( typeEntry );
				
				//System.out.println( "collision " + subj + " " + othr + " " + ch + " " + typeEntry + " " + collisionHandlers );				
				if( ch != null ) {
					PersistentVector pair = ch.collide( this, subj, othr, dt );
					BrObject newSubj = (BrObject) pair.get(0);
					updatedObjects.put( entry.getKey() , newSubj );
				}
			}
		}
		objects = updatedObjects;
		collisions.clear();
	}
	
	
	/* updateNeighborhoods
	 * Update the neighborhoods of all objects
	 * LAZY PAIRWISE IMPLEMENTATION
	 */
	/*public void updateNeighborhoods() {
		//HashMap<Long,BrObject> updatedObjects = new HashMap<Long,BrObject>();
		ConcurrentHashMap<Long,BrObject> updatedObjects = new ConcurrentHashMap<Long,BrObject>();			
		
		for( Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
			BrObject obj = entry.getValue();
			Vector<Long> nbrs = new Vector<Long>();
			for( Map.Entry<Long,BrObject> otherEntry : objects.entrySet() ) {
				BrObject otherObj = otherEntry.getValue();
				//System.out.println( "Distance to " + obj.distanceTo( otherObj ) + " radius " + neighborhoodRadius );
				if( obj.distanceTo( otherObj ) < neighborhoodRadius && obj != otherObj ) {
					nbrs.add( otherObj.uid );
					//System.out.println( "Adding neighbor " + obj.getType() );
				}
			}
			//System.out.println( "Neighbors of " + obj + " " + nbrs.size() );
			obj.nbrs = nbrs;
			updatedObjects.put( entry.getKey(), obj );
		}
		objects = updatedObjects;
	}*/
	
	/* updateNeighborhoods
	 * Update the neighborhoods of all objects
	 * KD tree implementation
	 */
	public void updateNeighborhoods() {
		//HashMap<Long,BrObject> updatedObjects = new HashMap<Long,BrObject>();
		ConcurrentHashMap<Long,BrObject> updatedObjects = new ConcurrentHashMap<Long,BrObject>();
		
		//spaceTree = new BrKDTree<BrKDNode>();//lazy
		spaceTree.clear();
		
		for( Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
			BrObject obj = entry.getValue();
			Vector3f pos = obj.getPosition();
			double[] arryloc = { pos.x, pos.y, pos.z };
			BrKDNode n = new BrKDNode( arryloc, entry.getKey() );
			spaceTree.add( n );
		}		
		
		int nResults = 10;
		
		for( Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
			BrObject obj = entry.getValue();
			Vector<Long> nbrs = new Vector<Long>();
			
			Vector3f pos = obj.getPosition();
			double[] arryloc = { pos.x, pos.y, pos.z };
			
			//Iterable<PrioNode<BrKDNode>> itNbrs = spaceTree.search( arryloc, nResults);
			//Iterable<PrioNode<BrKDNode>> itNbrs = spaceTree.searchByDistance( arryloc, neighborhoodRadius );
			ArrayList<BrKDNode> searchNbrs = spaceTree.searchByDistance( arryloc, neighborhoodRadius );
			Iterator<BrKDNode> itr = searchNbrs.iterator();
			
			while( itr.hasNext() ) {
				BrKDNode nbr = itr.next();
				nbrs.add( nbr.UID );
			}
			
			//System.out.println( "Neighbors of " + obj + " " + nbrs.size() );
			obj.nbrs = nbrs;
			updatedObjects.put( entry.getKey(), obj );
		}
		objects = updatedObjects;
	}
	
	/* 
	 * Return all objects along a line with start point, start, and direction vector, direction
	 * within distance, radius 
	 * NOTE: currently only centers of objects are considered, so radius should account for the largest dimension of the largest object to be considered
	 */
	public ArrayList<BrObject> objectsAlongLine( double[] start, double[] direction, double radius ) {
		ArrayList<BrObject> objs = new ArrayList<BrObject>();
		
		spaceTree.clear();
		
		for( Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
			BrObject obj = entry.getValue();
			Vector3f pos = obj.getPosition();
			double[] arryloc = { pos.x, pos.y, pos.z };
			BrKDNode n = new BrKDNode( arryloc, entry.getKey() );
			spaceTree.add( n );
		}				
		
		for( Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
			BrObject obj = entry.getValue();
			Vector<Long> nbrs = new Vector<Long>();
			
			Vector3f pos = obj.getPosition();
			double[] arryloc = { pos.x, pos.y, pos.z };
			
			//Iterable<PrioNode<BrKDNode>> itNbrs = spaceTree.search( arryloc, nResults);
			//Iterable<PrioNode<BrKDNode>> itNbrs = spaceTree.searchByDistance( arryloc, neighborhoodRadius );
			ArrayList<BrKDNode> searchNbrs = spaceTree.searchAlongLine( start, direction, radius);
			Iterator<BrKDNode> itr = searchNbrs.iterator();
			
			while( itr.hasNext() ) {
				BrKDNode nbr = itr.next();
				nbrs.add( nbr.UID );
			}
			
			//System.out.println( "Neighbors of " + obj + " " + nbrs.size() );
			obj.nbrs = nbrs;
			objs.add( obj );
		}
		return objs;
	}
	
	/* initWorld
	 * Initialization functions
	 */
	public void initWorld( ) {
		physics.time = 0;		
		startWallTime = System.nanoTime();
		objects.clear();
		addedObjects.clear();
		deletedObjects.clear();
		collisions.clear();
		globalCollisions.clear();
		spaceTree.clear();
		synchronizeObjects();		
	}
	
	/* updateWorld
	 * 	Run all of the enabled update subroutines including object updates, collisions, etc.
	 */
	public void updateWorld( double dt ) {		
		
		if( physicsEnabled ) {
			updatePhysics( dt );
			synchronizeObjects();
		}
		
		// Global update handlers < 0 run before individual object updates
		//System.out.println( " pre globalupdate ");
		globalUpdateObjects( true );
		synchronizeObjects();
		
		//System.out.println( " normal update " + globalUpdateHandlers.size() );
		updateObjects( dt );
		synchronizeObjects();
		
		//System.out.println( " post globalupdate ");
		// Global update handlers >= 0 run after individual object updates
		globalUpdateObjects( false );
		synchronizeObjects();
		
		
		if( collisionsEnabled ) {
			handleCollisions( dt );
			synchronizeObjects();
		}
		
		if( neighborhoodsEnabled ) {
			updateNeighborhoods();
			synchronizeObjects();
		}
		
		numSteps++;
	}
	
	public double getCurrentSimulationRate( ) {
		//System.out.println( "(" + simulationStart + " - " + System.nanoTime() + " ) / " + numSteps );
		return numSteps / ( (double) ( System.nanoTime() - simulationStart ) ) * 1000000.0;
	}
	
	public boolean getCollisionsEnabled() {
		return collisionsEnabled;		
	}
	
	public void setCollisionsEnabled( boolean newCE ) {
		collisionsEnabled = newCE;
	}
	
	public DWorld getWorld() {
		return physics.getWorld();
	}
	
	public double getWallTime() {
		return (System.nanoTime() - startWallTime) / 1000000000.0;
	}
	
	public double getTime() {
		return physics.getTime();
	}
	
	/* addObject
	 * Add an object to the simulation
	 */
	synchronized public void addObject( Long UID, BrObject obj ) {
		addedObjects.put( UID, obj );
		//System.out.println( "addObject " + UID + " " + obj );
	}
	
	public void deleteObject( Long UID ) {
		deletedObjects.add( UID );
	}
	
	public void addUpdateHandler( String type, UpdateHandler uh ) {
		System.out.println( "Adding update handler for type: " + type );
		updateHandlers.put( type,  uh );
	}
	
	public void addGlobalUpdateHandler( Long priority, GlobalUpdateHandler gh ) {
		System.out.println( "Adding global update handler " + gh + " with priority " + priority );
		gh.setPriority( priority );
		globalUpdateHandlers.add( gh );
	}
	
	public void enableUpdateKinematics( String type ) {
		//System.out.println( type );
		updateKinematics.put( type, true );
	}
	
	public void addCollisionHandler( String typea, String typeb, CollisionHandler ch ) {
		SimpleEntry<String,String> typeEntry = new SimpleEntry<String,String>( typea, typeb );
		collisionHandlers.put( typeEntry,  ch );
	}
	
	public Vector<Long> allObjectUIDs() {
		Vector<Long> v = new Vector<Long>();
		v.addAll( objects.keySet() );
		return v;
	}
	
	public Collection<BrObject> allObjects() {
		return objects.values();
	}
	
	public BrObject getObject( Long UID ) {
		return objects.get( UID );
	}
	
	public void setNeighborhoodRadius( Double r ) {
		neighborhoodRadius = r;
	}
	
	public double getNeighborhoodRadius() {
		return neighborhoodRadius;
	}
	
	public Collection<BrObject> getObjects() {
		return objects.values();
	}
	
	public BrPhysics getPhysics() {
		return physics;
	}	
}

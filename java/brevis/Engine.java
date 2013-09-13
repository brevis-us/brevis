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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.ode4j.ode.DGeom;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;

import clojure.lang.PersistentVector;

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
	// dt
	public double dt = 1.0;
	// neighborhoodRadius
	public double neighborhoodRadius = 25.0;
	// physics
	public BrPhysics physics;
	
	// objects
	protected HashMap<Long,BrObject> objects;	
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
	
	/* Methods: */		
	
	public Engine() {
		updateHandlers = new HashMap<String,UpdateHandler>();		
		updateKinematics = new HashMap<String,Boolean>();		
		physics = new BrPhysics();
		objects = new HashMap<Long,BrObject>();
		addedObjects = new HashMap<Long,BrObject>();
		deletedObjects = new HashSet<Long>();
		
		collisionHandlers = new HashMap< SimpleEntry<String,String>, CollisionHandler >();
		collisions = new HashSet< SimpleEntry<Long,Long> >();
		globalCollisions = new HashSet< SimpleEntry<Long,Long> >();
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
		HashMap<Long,BrObject> updatedObjects = new HashMap<Long,BrObject>();
		
		// Call the 0 update handler once
		if( updateHandlers.containsKey( 0 ) ) {
			UpdateHandler global_uh = updateHandlers.get( 0 );
			BrObject placeholder = global_uh.update( this, null, dt );
		}		
		
		//System.out.println( "updateObjects " + objects.keySet() );
		for( Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
			BrObject obj = entry.getValue();
			UpdateHandler uh = updateHandlers.get( obj.type );
			
			BrObject newObj = obj;
			if( uh != null ) {
				//System.out.println( "--" + getTime() + " updating object " + entry.getKey() );
				newObj = uh.update( this, entry.getKey(), dt );				
			} 
			
			Boolean kh = updateKinematics.get( obj.type );
			//System.out.println( obj.type + " " + kh );
			if( kh != null && kh ) {
				newObj.updateObjectKinematics( dt );
			}
			
			updatedObjects.put( entry.getKey(), newObj );
		}
		objects = updatedObjects;
	}
	
	/* handleCollisions
	 * Respond to all computed collisions
	 */
	public void handleCollisions( double dt ) {
		collisions = globalCollisions;
		
		HashMap<Long,BrObject> updatedObjects = new HashMap<Long,BrObject>();
		// Because we may collide with an object multiple times, we first duplicate all objects and use
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
	public void updateNeighborhoods() {
		HashMap<Long,BrObject> updatedObjects = new HashMap<Long,BrObject>();
		for( Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
			BrObject obj = entry.getValue();
			Vector<Long> nbrs = new Vector<Long>();
			for( Map.Entry<Long,BrObject> otherEntry : objects.entrySet() ) {
				BrObject otherObj = otherEntry.getValue();
				//System.out.println( "Distance to " + obj.distanceTo( otherObj ) );
				if( obj.distanceTo( otherObj ) < neighborhoodRadius ) {
					nbrs.add( otherObj.uid );
				}
			}
			//System.out.println( "Neighbors of " + obj + " " + nbrs.size() );
			obj.nbrs = nbrs;
			updatedObjects.put( entry.getKey(), obj );
		}
		objects = updatedObjects;
	}
	
	/* initWorld
	 * Initialization functions
	 */
	public void initWorld( ) {
		physics.time = 0;		
		objects.clear();
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
		
		updateObjects( dt );
		synchronizeObjects();
		
		if( collisionsEnabled ) {
			handleCollisions( dt );
			synchronizeObjects();
		}
		
		if( neighborhoodsEnabled ) {
			updateNeighborhoods();
			synchronizeObjects();
		}
	}
	
	public DWorld getWorld() {
		return physics.getWorld();
	}
	
	public double getTime() {
		return physics.getTime();
	}
	
	/* addObject
	 * Add an object to the simulation
	 */
	public void addObject( Long UID, BrObject obj ) {
		addedObjects.put( UID, obj );
		//System.out.println( "addObject " + UID + " " + obj );
	}
	
	public void deleteObject( Long UID ) {
		deletedObjects.add( UID );
	}
	
	public void addUpdateHandler( String type, UpdateHandler uh ) {
		updateHandlers.put( type,  uh );
	}
	
	public void enableUpdateKinematics( String type ) {
		//System.out.println( type );
		updateKinematics.put( type, true );
	}
	
	public void addCollisionHandler( String typea, String typeb, CollisionHandler ch ) {
		SimpleEntry<String,String> typeEntry = new SimpleEntry<String,String>( typea, typeb );
		collisionHandlers.put( typeEntry,  ch );
	}
	
	public BrObject getObject( long UID ) {
		return objects.get( UID );
	}
	
	public void setNeighborhoodRadius( Double r ) {
		neighborhoodRadius = r;
	}
	
	public Collection<BrObject> getObjects() {
		return objects.values();
	}
}

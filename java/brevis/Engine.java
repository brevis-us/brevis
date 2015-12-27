
package brevis;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;








//import org.ejml.data.DenseMatrix64F;
import org.lwjgl.util.vector.Vector3f;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DJointGroup;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;
import org.ojalgo.access.Access2D.Builder;
//import org.ojalgo.access.Access2D.Factory;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.matrix.BasicMatrix.Factory;
import org.ojalgo.matrix.PrimitiveMatrix;

import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import duyn.algorithm.nearestneighbours.FastKdTree;
import duyn.algorithm.nearestneighbours.PrioNode;

public class Engine implements Serializable {	
	
	/*
	 * Callback classes. 
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5209150139378668298L;

	// Compute the update after DT amount of time for object with UID
	public static class UpdateHandler implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7301251223848548305L;

		public BrObject update( Engine engine, Long uid, Double dt ) {
			BrObject obj = engine.objects.get( uid );
			return obj;
		}
		private void writeObject(java.io.ObjectOutputStream out) throws IOException {
			 out.defaultWriteObject();
		}
			     
	 	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	 		in.defaultReadObject();
	 	}
	}
	
	public static class GlobalUpdateHandler implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5310171212429743564L;
		public Long priority = (long) 0;
		public void update( Engine engine ) {			
		}
		public Long getPriority () {
			return priority;
		}
		public void setPriority( Long priority2 ) {
			priority = priority2;
		}
		private void writeObject(java.io.ObjectOutputStream out) throws IOException {
			 out.defaultWriteObject();
		}
			     
	 	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	 		in.defaultReadObject();
	 	}
	}
	
	// All collisions are pairwise now.
	// Compute the collision after DT amount of time for object with UID colliding with UID, other	
	/*public static class CollisionHandler {
		public BrObject collide( BrObject subj, BrObject othr, Double dt) {			
			return subj;
		}
	}*/
	
	public static class CollisionHandler implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5029373761039142328L;

		public clojure.lang.PersistentVector collide( Engine engine, BrObject subj, BrObject othr, Double dt) {
			clojure.lang.PersistentVector v = clojure.lang.PersistentVector.create( subj, othr );						
			return v;
		}
		private void writeObject(java.io.ObjectOutputStream out) throws IOException {
			 out.defaultWriteObject();
		}
			     
	 	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	 		in.defaultReadObject();
	 	}
	}
	
	/* 
	 * Variables:
	 */
	
	// updateHandlers
	protected HashMap<clojure.lang.Keyword,UpdateHandler> updateHandlers;
	protected HashMap<clojure.lang.Keyword,Boolean> updateKinematics;
	protected PriorityQueue<GlobalUpdateHandler> globalUpdateHandlers; 
	// dt
	public double dt = 1.0;
	// neighborhoodRadius
	public double neighborhoodRadius = 25.0;
	// physics
	public BrPhysics physics;
	
	protected long simulationStart = -1;
	protected long numSteps = 0;
	
	public long rebalanceKDTreeSteps = 1; // Rebalance the KDtree every N steps
	
	public double startWallTime = 0;
	
	// objects
	//protected HashMap<Long,BrObject> objects;	
	protected ConcurrentHashMap<Long,BrObject> objects;	
	// addedObjects
	protected HashMap<Long,BrObject> addedObjects;
	// deletedObjects	
	protected HashSet<Long> deletedObjects;
	
	// collisionHandlers
	protected HashMap< SimpleEntry<clojure.lang.Keyword,clojure.lang.Keyword>, CollisionHandler > collisionHandlers;
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
	//public boolean brevisParallel = false;
	public boolean brevisParallel = false;
	
	public transient BrKDTree<BrKDNode> spaceTree = null;
	
	public transient ReentrantLock lock = new ReentrantLock();
	
	/* Methods: */	
	
	class GUHComparator implements Comparator<GlobalUpdateHandler>, Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8745204900496801037L;

		@Override
		public int compare(GlobalUpdateHandler gh1, GlobalUpdateHandler gh2) {
			return (int) ( gh1.priority - gh2.priority );
		}
		
		private void writeObject(java.io.ObjectOutputStream out) throws IOException {
			 out.defaultWriteObject();
		}
			     
	 	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	 		in.defaultReadObject();
	 	}
	}
	
	public Engine() {
		updateHandlers = new HashMap<clojure.lang.Keyword,UpdateHandler>();		
		updateKinematics = new HashMap<clojure.lang.Keyword,Boolean>();		
		physics = new BrPhysics();
		//objects = new HashMap<Long,BrObject>();
		objects = new ConcurrentHashMap<Long,BrObject>();
		addedObjects = new HashMap<Long,BrObject>();
		deletedObjects = new HashSet<Long>();
		
		collisionHandlers = new HashMap< SimpleEntry<clojure.lang.Keyword,clojure.lang.Keyword>, CollisionHandler >();
		collisions = new HashSet< SimpleEntry<Long,Long> >();
		globalCollisions = new HashSet< SimpleEntry<Long,Long> >();
		
		globalUpdateHandlers = 
				new PriorityQueue<GlobalUpdateHandler>(1, (Comparator<GlobalUpdateHandler>) new GUHComparator() );
		
		simulationStart = System.nanoTime();
		
		spaceTree = new BrKDTree<BrKDNode>(10);
	}
	
	public static class BrevisCollision implements DGeom.DNearCallback {

		@SuppressWarnings("unchecked")
		@Override
		public void call(Object data, DGeom o1, DGeom o2) {
			HashMap<String,Object> o1map = (HashMap<String,Object>)o1.getBody().getData();
			HashMap<String,Object> o2map = (HashMap<String,Object>)o2.getBody().getData();
			Long uid1 = (Long)o1map.get("uid");
			Long uid2 = (Long)o2map.get("uid");
			
			// Only add one collisions, and sort them small to big
			if( uid1 < uid2 ) 
				Engine.globalCollisions.add( new SimpleEntry<Long,Long>( uid1, uid2 ) );
			else if( uid2 > uid1 )
				Engine.globalCollisions.add( new SimpleEntry<Long,Long>( uid2, uid1 ) );
			
			
			// This used to add 2
			//SimpleEntry<Long,Long> p1 = new SimpleEntry<Long,Long>( uid1, uid2 );
			//SimpleEntry<Long,Long> p2 = new SimpleEntry<Long,Long>( uid2, uid1 );
			//System.out.println( "collision callback " + p1 + " " + p2  + " " + o1map + " " + o2map );
			//Engine.globalCollisions.add( p1 );
			//Engine.globalCollisions.add( p2 );
			
			/*if (physicsEnabled) {
				// Also do a physics collision?
			}*/
		}
		
	}	
	
	/* updatePhysics
	 * Move according to physics
	 */
	public void updatePhysics( double dt ) {
		lock.lock();  // block until condition holds
	     try {

	 		physics.contactGroup.empty();
	 		if( collisionsEnabled ) {
	 			OdeHelper.spaceCollide( physics.space, null, new BrevisCollision() );
	 		}
	 		physics.world.quickStep( dt );						
	 		
	 		physics.time += dt;		
	     } finally {
	       lock.unlock();
	     }
	}
	
	public void clearSimulation() {
		/* Just clear everything */
		
		lock.lock();  // block until condition holds
	     try {

	    	 physics = new BrPhysics();
	 		//objects = new HashMap<Long,BrObject>();
	 		objects = new ConcurrentHashMap<Long,BrObject>();
	 		addedObjects = new HashMap<Long,BrObject>();
	 		deletedObjects = new HashSet<Long>();
	 	} finally {
	       lock.unlock();
	     }
		
	}
	
	/* synchronizeObjects
	 * Apply all insertions/deletions
	 */
	public void synchronizeObjects() {
		
	     try {
	    	 lock.lock();  // block until condition holds
	    	//System.out.println( "synchronizeObjects del: " + deletedObjects.size() + " add: " + addedObjects.size() );
	 		// Remove deleted objects
	 		for( Long uid : deletedObjects ) {
	 			//objects.get(uid).clear();
	 			// things aren't getting removed from the physics engine
	 			BrObject object = objects.get( uid );
	 			physics.getSpace().remove( object.getGeom() );
	 			objects.remove( uid );	 			
	 		}
	 		deletedObjects = new HashSet<Long>();
	 		
	 		// Add newly created objects
	 		objects.putAll( addedObjects );
	 		addedObjects = new HashMap<Long,BrObject>();
	     } catch (Exception e) {
	            //do something clever with the exception
	            System.out.println(e.getMessage());
	            e.printStackTrace();
	     } finally {
	       lock.unlock();
	     }
	
	     //physics.space.cleanGeoms();
	}
	
	
	
	/* updateObjects
	 * Call individual update functions
	 */
	public void updateObjects( double dt ) {
		
		//System.out.println( getTime() );
	     try {
	    	 lock.lock();  // block until condition holds
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
	 				//System.out.println( obj );
	 				//System.out.println( "Updating");	 				
	 				newObj = uh.update( this, entry.getKey(), dt );
	 				//System.out.println( newObj );
	 			} 
	 			//System.out.println( "Updated");
	 			//else System.out.println( "--" + getTime() + " not updating object " + entry.getKey() + " " + entry.getValue().type );
	 			
	 			Boolean kh = updateKinematics.get( obj.type );
	 			//System.out.println( obj.type + " " + kh );
	 			if( kh != null && kh ) {
	 				newObj.updateObjectKinematics( dt );
	 			}
	 			
	 			updatedObjects.put( entry.getKey(), newObj );
	 		}
	 		objects = updatedObjects;
	 		//System.out.println( "DupdateObjects " + objects.keySet() );
	 		//System.out.println( getTime() );
	     } catch (Exception e) {
	            //do something clever with the exception
	            System.out.println(e.getMessage());
	            e.printStackTrace();
	     } finally {
	       lock.unlock();
	     }		
	}
	
	class BrObjectResult {
        public BrObject obj;
        public Long UID;

        BrObjectResult(){
        }
   }

   class UpdateObjectTask implements Callable<BrObjectResult> {
       BrObject obj;
       Long UID;

       UpdateObjectTask( BrObject myObj, Long myUID ){
           UID = myUID;
           obj = myObj;
       }

       public BrObjectResult call() throws Exception {
    	   BrObjectResult result = new BrObjectResult();
    	   
    	   UpdateHandler uh = updateHandlers.get( obj.type );
			
    	   result.obj = obj;
    	   result.UID = UID;
    	   if( uh != null ) {
				//System.out.println( "--" + getTime() + " updating object " + entry.getKey() );
    		   result.obj = uh.update( null, UID, dt );				
    	   } 
			//else System.out.println( "--" + getTime() + " not updating object " + entry.getKey() + " " + entry.getValue().type );
			
           return result;
       }

   }		
	
	/* parallelUpdateObjects (not working properly yet)
	 * Call individual update functions in parallel
	 */
	public void parallelUpdateObjects( final double dt ) {
		lock.lock();  // block until condition holds
		ConcurrentHashMap<Long,BrObject> updatedObjects = new ConcurrentHashMap<Long,BrObject>();
		ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	     try {
	    	 
	    	 
             CompletionService<BrObjectResult> taskCompletionService =
                new ExecutorCompletionService<BrObjectResult>(exec);

             // Send
	         for( Map.Entry<Long,BrObject> entry : objects.entrySet()  ){
	             taskCompletionService.submit(new UpdateObjectTask( entry.getValue(), entry.getKey() ));
	         }
	         
	         // Wait
	         for(int tasksHandled=0;tasksHandled<objects.size();tasksHandled++){
	             try {
	                 System.out.println("trying to take from Completion service");
	                 Future<BrObjectResult> result = taskCompletionService.take();
	                 System.out.println("result for a task availble in queue.Trying to get()"  );
	                 // above call blocks till atleast one task is completed and results availble for it
	                 // but we dont have to worry which one
	
	                 // process the result here by doing result.get()
	                 BrObjectResult obj = result.get();
	                 
	                 if( !deletedObjects.contains( obj.UID ) ) {// Can't update what isn't there (i.e. objects that self-delete)
	     	 			Boolean kh = updateKinematics.get( obj.obj.type );
	     	 			//System.out.println( obj.type + " " + kh );
	     	 			if( kh != null && kh ) {
	     	 				obj.obj.updateObjectKinematics( dt );
	     	 			}
	     	 			
	     	 			//updatedObjects.put( entry.getKey(), newObj ); //was here
	     			} else {
	     				// Delete the object
	     				if( obj.obj != null )
	     					obj.obj.destroy( null );
	     			}
	                 
	                 updatedObjects.put( obj.UID, obj.obj );
	                 //System.out.println("Task " + String.valueOf(tasksHandled) + "Completed - results obtained : " + String.valueOf(l.result));
	
	             } catch (InterruptedException e) {
	                 // Something went wrong with a task submitted
	                 System.out.println("Error Interrupted exception");
	                 e.printStackTrace();
	             } catch (ExecutionException e) {
	                 // Something went wrong with the result
	                 e.printStackTrace();
	                 System.out.println("Error get() threw exception");
	             }
	         }
	 		
	 		
	     } finally {
	    	 exec.shutdown();
	       lock.unlock();
	     }	
	     objects = updatedObjects;
	}	
	
	/* globalUpdateObjects
	 * Call individual update functions
	 */
	public void globalUpdateObjects( boolean preIndividual ) {
		
		try {
			lock.lock();
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
		} catch (Exception e) {
			System.out.println( "Exception in globalUpdateHandlers:" );
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	/* handleCollisions
	 * Respond to all computed collisions
	 */
	public void handleCollisions( double dt ) {
		
	     try {
	    	 lock.lock();  // block until condition holds
	    	collisions = globalCollisions;
	    	// It might be reasonable to shuffle here
	 		
	 		//HashMap<Long,BrObject> updatedObjects = new HashMap<Long,BrObject>();
	 		ConcurrentHashMap<Long,BrObject> updatedObjects = new ConcurrentHashMap<Long,BrObject>();
	 		updatedObjects.putAll(objects);	 	
	 		
	 		//System.out.println( "handleCollisions " + collisions );	 		
	 		
	 		for( SimpleEntry<Long,Long> entry: collisions ) {
	 			BrObject subj = updatedObjects.get( entry.getKey() );
	 			BrObject othr = updatedObjects.get( entry.getValue() );
	 			
	 			// If the colliding objects both still exist
	 			if( subj != null && othr != null ) {
	 				SimpleEntry<clojure.lang.Keyword,clojure.lang.Keyword> typeEntry = new SimpleEntry<clojure.lang.Keyword,clojure.lang.Keyword>((clojure.lang.Keyword)subj.type,(clojure.lang.Keyword)othr.type);
	 				
	 				// Find the corresponding collision handler
	 				CollisionHandler ch = collisionHandlers.get( typeEntry );
	 				
	 				// Collisions are only inserted into the list once, so let's check the opposite type ordering for another collision handler if we cant find one 
	 				if( ch == null ) {
	 					ch = collisionHandlers.get( new SimpleEntry<clojure.lang.Keyword,clojure.lang.Keyword>((clojure.lang.Keyword)othr.type,(clojure.lang.Keyword)subj.type) );
	 					if( ch != null ) {
	 						BrObject tmp = subj;
	 						subj = othr;
	 						othr = tmp;
	 					}
	 				}
	 				
	 				//System.out.println( "collision " + subj + " " + othr + " " + ch + " " + typeEntry + " " + collisionHandlers );				
	 				//System.out.println( "collision " + subj.type + " " + othr.type + " " + ch + " " + typeEntry + " " + collisionHandlers );				
	 				if( ch != null ) {
	 					//System.out.println( "Collision handler found " );
	 					PersistentVector pair = ch.collide( this, subj, othr, dt );
	 					BrObject newSubj = (BrObject) pair.get(0);
	 					BrObject newOthr = (BrObject) pair.get(1);
	 					// Need to avoid calling on the same key-value pair twice
	 					updatedObjects.put( entry.getKey() , newSubj );
	 					updatedObjects.put( entry.getValue() , newOthr);
	 				}
	 			}
	 		}
	 		objects = updatedObjects;
	 		collisions.clear();
	     } catch( Exception e ) {
	    	e.printStackTrace(); 
	     } finally {
	       lock.unlock();
	     }		
		
	}	
	
	public void reinitializeKDTree() {
		spaceTree.clear(); // also lazy but a little better
 		
 		// Add everyone to the KD tree (need to do this if clear or creating a new tree)
 		for( Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
 			BrObject obj = entry.getValue();
 			//Vector3f pos = obj.getPosition();
 			Vector3f pos = new Vector3f();
 			pos = Vector3f.add( obj.getPosition(), obj.getShape().center, pos);
 			double[] arryloc = { pos.x, pos.y, pos.z };
 			BrKDNode n = new BrKDNode( arryloc, entry.getKey() );
 			obj.myKDnode = n;
 			spaceTree.add( n );
 		}
	}
	
	/* updateNeighborhoods
	 * Update the neighborhoods of all objects
	 * KD tree implementation
	 */
	public void updateNeighborhoods() {
		
	     try {
	    	 lock.lock();  // block until condition holds
	 		//HashMap<Long,BrObject> updatedObjects = new HashMap<Long,BrObject>();
	 		ConcurrentHashMap<Long,BrObject> updatedObjects = new ConcurrentHashMap<Long,BrObject>();
	 		
	 		//spaceTree = new BrKDTree<BrKDNode>();//lazy
	 		//spaceTree.clear(); // also lazy but a little better
	 		
	 		// Add everyone to the KD tree (need to do this if clear or creating a new tree)
	 		/*for( Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
	 			BrObject obj = entry.getValue();
	 			//Vector3f pos = obj.getPosition();
	 			Vector3f pos = new Vector3f();
	 			pos = Vector3f.add( obj.getPosition(), obj.getShape().center, pos);
	 			double[] arryloc = { pos.x, pos.y, pos.z };
	 			BrKDNode n = new BrKDNode( arryloc, entry.getKey() );
	 			spaceTree.add( n );
	 		}*/
	 		
	 		
	 		//reinitializeKDTree();
	 		
	 		if( numSteps % rebalanceKDTreeSteps == 0 ) {
	 			reinitializeKDTree();
	 		}
	 		
	 		
	 		// Loop over everyone and cache their neighborhood (this could be made lazy)
	 		for( Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
	 			BrObject obj = entry.getValue();
	 			Vector<Long> nbrs = new Vector<Long>();
	 			
	 			Vector3f pos = obj.getPosition();
	 			double[] arryloc = { pos.x, pos.y, pos.z };
	 			
	 			//Iterable<PrioNode<BrKDNode>> itNbrs = spaceTree.search( arryloc, nResults);
	 			//Iterable<PrioNode<BrKDNode>> itNbrs = spaceTree.searchByDistance( arryloc, neighborhoodRadius );
	 			List<BrKDNode> searchNbrs = spaceTree.searchByDistance( arryloc, neighborhoodRadius );
	 			Iterator<BrKDNode> itr = searchNbrs.iterator();
	 			
	 			double closestDistance = 99999999;//maybe dangerous?
	 			long closestUID = 0;
	 			boolean foundClosest = false;	 				 		
	 			
	 			//System.out.println( "---" + obj.uid + "---" );
	 			
	 			Vector3f diff = new Vector3f();
	 			while( itr.hasNext() ) {
	 				BrKDNode nbr = itr.next();
	 				nbrs.add( nbr.UID );
	 				diff = Vector3f.sub( objects.get( nbr.UID ).getPosition(), obj.getPosition(), diff );	 				
	 				double ldiff = diff.length();
	 				if ( ( ldiff < closestDistance ) && ( nbr.UID != obj.uid ) ) {
	 					closestDistance = ldiff;
	 					closestUID = nbr.UID;
	 					foundClosest = true;
	 				}
	 				
		 			//System.out.println( nbr.UID + " : " + ldiff + " [ " + diff  + " ] { " + objects.get( nbr.UID ).getPosition() );
	 			}
	 			
	 			//System.out.println( "Closest: " + closestDistance );
	 			//System.out.println( "Closest: " + closestUID );
	 			
	 			//System.out.println( "---END " + obj.uid + "---" );
	 			
	 			if( foundClosest )
	 				obj.closestNeighbor = closestUID;
	 			else
	 				obj.closestNeighbor = (long) 0;
	 			
	 			//System.out.println( "Neighbors of " + obj + " " + nbrs.size() );
	 			obj.nbrs = nbrs;
	 			updatedObjects.put( entry.getKey(), obj );
	 		}
	 		objects = updatedObjects;
	     } catch( Exception e) {
	    	e.printStackTrace(); 
	     } finally {
	       lock.unlock();
	     }	

	}
	
	/* updateNeighborhoods
	 * Update the neighborhoods of all objects
	 * KD tree implementation
	 */
	public void parallelUpdateNeighborhoods() {
		
	     try {
	    	 lock.lock();  // block until condition holds
	 		//HashMap<Long,BrObject> updatedObjects = new HashMap<Long,BrObject>();
	 		final ConcurrentHashMap<Long,BrObject> updatedObjects = new ConcurrentHashMap<Long,BrObject>();
	 		
	 		if( numSteps % rebalanceKDTreeSteps == 0 ) {
	 			reinitializeKDTree();
	 		}
	 		
	 		// Loop over everyone and cache their neighborhood
	 		int brevisNumThreads = 8;
	 		ExecutorService exec = Executors.newFixedThreadPool( brevisNumThreads );
	 		try {
	 			//ArrayList<Future<BrObject>> alist  = new ArrayList<Future<BrObject>>();
	 			ArrayList<Map.Entry<Long,Future<BrObject>>> alist  = new ArrayList<Map.Entry<Long,Future<BrObject>>>();
	 			
	 			for( final Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
	 		    	Future<BrObject> future = exec.submit(new Callable() {
	 		            @Override
	 		            public BrObject call() {
	 		            	BrObject obj = entry.getValue();
	 			 			Vector<Long> nbrs = new Vector<Long>();
	 			 			
	 			 			Vector3f pos = obj.getPosition();
	 			 			double[] arryloc = { pos.x, pos.y, pos.z };
	 			 			
	 			 			//Iterable<PrioNode<BrKDNode>> itNbrs = spaceTree.search( arryloc, nResults);
	 			 			//Iterable<PrioNode<BrKDNode>> itNbrs = spaceTree.searchByDistance( arryloc, neighborhoodRadius );
	 			 			List<BrKDNode> searchNbrs = spaceTree.searchByDistance( arryloc, neighborhoodRadius );
	 			 			Iterator<BrKDNode> itr = searchNbrs.iterator();
	 			 			
	 			 			double closestDistance = 99999999;//maybe dangerous?
	 			 			long closestUID = 0;
	 			 			boolean foundClosest = false;	 				 		
	 			 			
	 			 			//System.out.println( "---" + obj.uid + "---" );
	 			 			
	 			 			Vector3f diff = new Vector3f();
	 			 			while( itr.hasNext() ) {
	 			 				BrKDNode nbr = itr.next();
	 			 				nbrs.add( nbr.UID );
	 			 				diff = Vector3f.sub( objects.get( nbr.UID ).getPosition(), obj.getPosition(), diff );	 				
	 			 				double ldiff = diff.length();
	 			 				if ( ( ldiff < closestDistance ) && ( nbr.UID != obj.uid ) ) {
	 			 					closestDistance = ldiff;
	 			 					closestUID = nbr.UID;
	 			 					foundClosest = true;
	 			 				}
	 			 			}
	 			 			
	 			 			if( foundClosest )
	 			 				obj.closestNeighbor = closestUID;
	 			 			else
	 			 				obj.closestNeighbor = (long) 0;
	 			 			
	 			 			//System.out.println( "Neighbors of " + obj + " " + nbrs.size() );
	 			 			obj.nbrs = nbrs;
	 			 			//updatedObjects.put( entry.getKey(), obj );
	 			 			return obj;
	 		            }
	 		            
	 		        });
	 		    	alist.add( new AbstractMap.SimpleEntry<Long,Future<BrObject>>(entry.getKey(), future)  );
	 		       //updatedObjects.put( entry.getKey(), future.get() );
	 		    }
	 		    
	 			for( final Map.Entry<Long,Future<BrObject>> entry : alist ) {	 		   
	 			   
	 			  updatedObjects.put( entry.getKey(), entry.getValue().get() );
	 		   }
	 		} catch ( Exception ex) {
	 			System.out.println("Exception in Parallel update neighborhoods.");
	 			ex.printStackTrace();
	 		}
	 		finally {
	 		    exec.shutdown();
	 		}
	 		objects = updatedObjects;
	     } catch( Exception e) {
	    	e.printStackTrace(); 
	     } finally {
	       lock.unlock();
	     }	

	}
	
	
	/*(defn distance-obj-to-line
			  "Distance of an object to a line."*/
	public double distanceToLine( Vector3f testPoint, Vector3f linePoint, Vector3f direction ) {
		Vector3f diff = Vector3f.sub( testPoint, linePoint, null );
		Vector3f diffXv = Vector3f.cross( diff, direction, null );
		double ldiff = diff.length();
		double sinTheta = diffXv.length() / ( ldiff * direction.length() );
		return ( ldiff * sinTheta );
	}
	
	/* 
	 * Return all objects along a line with start point, start, and direction vector, direction
	 * within distance, radius 
	 * NOTE: currently only centers of objects are considered, so radius should account for the largest dimension of the largest object to be considered
	 */
	public ArrayList<BrObject> objectsAlongLine( double[] start, double[] direction, double radius ) {
		ArrayList<BrObject> objs = null;
		Vector3f linePoint = new Vector3f( (float)start[0], (float)start[1], (float)start[2] );
		Vector3f dirVec= new Vector3f( (float)direction[0], (float)direction[1], (float)direction[2] );
		lock.lock();  // block until condition holds
	     try {	 		
	    	 objs = new ArrayList<BrObject>();
	 		
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
	 			//obj.nbrs = nbrs;
	 			if( distanceToLine( pos, linePoint, dirVec ) < radius )
	 				objs.add( obj );
	 		}	 
	 		
	     } finally {
	       lock.unlock();
	     }	
	     return objs;
	}
	
	/* initWorld
	 * Initialization functions
	 */
	public void initWorld( ) {
		lock.lock();  // block until condition holds
	     try {	 		
	    	 physics.time = 0;		
	 		startWallTime = System.nanoTime();
	 		objects.clear();
	 		addedObjects.clear();
	 		deletedObjects.clear();
	 		collisions.clear();
	 		globalCollisions.clear();
	 		spaceTree.clear();
	 		synchronizeObjects(); 		
	     } finally {
	       lock.unlock();
	     }	
				
	}
	
	/* updateWorld
	 * 	Run all of the enabled update subroutines including object updates, collisions, etc.
	 */
	//public void updateWorld( double dt ) {		
	public void updateWorld() {		
		
		// Clear up old collisions
		if( collisionsEnabled ) {
			physics.clearContactGroup();
		}
		
		if( physicsEnabled ) {
			synchronizeObjects();
			updatePhysics( dt );
			synchronizeObjects();
		}
		
		if( neighborhoodsEnabled ) {
			if( brevisParallel ) {
				parallelUpdateNeighborhoods();
			} else {
				updateNeighborhoods();
			}
			synchronizeObjects();
		}
		
		// Global update handlers < 0 run before individual object updates
		//System.out.println( " pre globalupdate ");
		globalUpdateObjects( true );
		synchronizeObjects();
		
		//System.out.println( " normal update " + globalUpdateHandlers.size() );
		if( false ) {//brevisParallel ) {
			parallelUpdateObjects( dt );
		} else {
			updateObjects( dt );
		}
		synchronizeObjects();
		
		//System.out.println( " post globalupdate ");
		// Global update handlers >= 0 run after individual object updates
		globalUpdateObjects( false );
		synchronizeObjects();
		
		
		if( collisionsEnabled ) {
			handleCollisions( dt );
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
	
	public boolean getNeighborhoodsEnabled() {
		return neighborhoodsEnabled;		
	}
	
	public void setNeighborhoodsEnabled( boolean newNE ) {
		neighborhoodsEnabled = newNE;
	}
	
	public void setRebalanceKDTreeSteps( long n ) {
		rebalanceKDTreeSteps = n;
	}
	
	public long getRebalanceKDTreeSteps( ) {
		return rebalanceKDTreeSteps;
	}
	
	public DWorld getWorld() {		
		return physics.getWorld();
	}
	
	public DJointGroup getContactGroup() {
		return physics.getContactGroup();
	}
	
	public DSpace getSpace() {
		return physics.getSpace();
	}
	
	public double getWallTime() {
		return (System.nanoTime() - startWallTime) / 1000000000.0;
	}
	
	public double getDT() {
		return dt;
	}
	
	public void setDT(double newDT) {
		dt = newDT;
	}
	
	public double getTime() {
		return physics.getTime();
	}
	
	public long getSteps() {
		return numSteps;
	}
	
	/* addObject
	 * Add an object to the simulation
	 */
	synchronized public void addObject( Long UID, BrObject obj ) {
		addedObjects.put( UID, obj );
		//System.out.println( "addObject " + UID + " " + obj );
	}
	
	synchronized public void setObject( Long UID, BrObject obj ) {
		objects.put( UID, obj );
		//System.out.println( "addObject " + UID + " " + obj );
	}
	
	public void deleteObject( Long UID ) {
		deletedObjects.add( UID );
	}
	
	public void addUpdateHandler( String type, UpdateHandler uh ) {
		System.out.println( "Adding update handler for type: " + type );
		updateHandlers.put( clojure.lang.Keyword.intern( clojure.lang.Symbol.create( type ) ),  uh );
	}
	
	public void addGlobalUpdateHandler( Long priority, GlobalUpdateHandler gh ) {
		System.out.println( "Adding global update handler " + gh + " with priority " + priority );
		gh.setPriority( priority );
		globalUpdateHandlers.add( gh );
	}
	
	public void enableUpdateKinematics( String type ) {
		//System.out.println( type );
		updateKinematics.put( clojure.lang.Keyword.intern( clojure.lang.Symbol.create( type ) ), true );
	}
	
	public void addCollisionHandler( String typea, String typeb, CollisionHandler ch ) {
		SimpleEntry<clojure.lang.Keyword,clojure.lang.Keyword> typeEntry = new SimpleEntry<clojure.lang.Keyword,clojure.lang.Keyword>( clojure.lang.Keyword.intern( clojure.lang.Symbol.create( typea ) ), 
				clojure.lang.Keyword.intern( clojure.lang.Symbol.create( typeb ) ) );
		collisionHandlers.put( typeEntry,  ch );
	}
	
	public Vector<Long> allObjectUIDs() {
		Vector<Long> v = new Vector<Long>();
		v.addAll( objects.keySet() );
		return v;
	}
	
	public Vector<Long> allObjectUIDsByType( clojure.lang.Keyword type ) {
		Vector<Long> v = new Vector<Long>();
		
		for( Map.Entry<Long,BrObject> entry : objects.entrySet() ) {
 			if( entry.getValue().getType() == type ) {
 				v.add( entry.getKey() );
 			}
 		}
		
		return v;
	}
	
	public Collection<BrObject> allObjects() {
		return objects.values();
	}
	
	public Collection<BrObject> allObjects(boolean includeAdded) {
		if( includeAdded ) {
			Collection<BrObject> res = new HashSet<BrObject>();
			res.addAll( objects.values() );
			res.addAll( addedObjects.values() );
			return res;
		}
		else
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
		
	public BasicMatrix pairwiseObjectDistances() {				
		final Factory<PrimitiveMatrix> tmpFactory = PrimitiveMatrix.FACTORY;
		
		final Builder<PrimitiveMatrix> tmpBuilder = tmpFactory.getBuilder( objects.size(), objects.size() );

        ArrayList<Long> objKeys = new ArrayList<Long>( objects.keySet() );
		for( int k = 0; k < objects.size(); k++ ) {
			BrObject thisObj = objects.get( objKeys.get( k ) );
			for( int j = 0; j < objects.size(); j++ ) {
				double val = 0;
				if( k == j ) val = 0;
				else {
					val = thisObj.distanceTo( objects.get( objKeys.get( j ) ) );
				}
				tmpBuilder.set(k,j,val);
				tmpBuilder.set(j,k,val);
			}
		}
        
        return tmpBuilder.build();
		
			
	}
	
	/* EJML version
	public DenseMatrix64F pairwiseObjectDistances() {
		DenseMatrix64F dists = new DenseMatrix64F( objects.size(), objects.size() );
		ArrayList<Long> objKeys = new ArrayList<Long>( objects.keySet() );
		for( int k = 0; k < objects.size(); k++ ) {
			BrObject thisObj = objects.get( objKeys.get( k ) );
			for( int j = 0; j < objects.size(); j++ ) {
				double val = 0;
				if( k == j ) val = 0;
				else {
					val = thisObj.distanceTo( objects.get( objKeys.get( j ) ) );
				}
				dists.set(k,j,val);
				dists.set(j,k,val);
			}
		}
		return dists;		
	}*/
	
	public void setParallel( java.lang.Boolean newParallel ) {
		brevisParallel = newParallel;
	}
	
	public boolean getParallel() {
		return brevisParallel;
	}
	
	/* Serialization stuff */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		 out.defaultWriteObject();
		 // Write handlers
		 // Write objects
	}
		     
 	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
 		in.defaultReadObject();
 	} 	 	
		 
 	/* private void readObjectNoData() throws ObjectStreamException {
 		
 	}*/
		     
}

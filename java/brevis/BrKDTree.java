/* 
 * brevis KDTree
 * based on Duy Nguyen's duyn.algorithm.nearestneighbours 
 */

package brevis;
import java.util.*;

import duyn.algorithm.nearestneighbours.FastBinaryHeap;
import duyn.algorithm.nearestneighbours.PrioNode;

/**
 * A k-dimensional binary partitioning tree which splits space on the
 * mean of the dimension with the largest variance. Points are held in
 * buckets so we can pick a better split point than whatever comes first.
 *
 * Does not store tree depth. If you want balance, re-build the tree
 * periodically.
 *
 * Optimisations in this tree assume distance metric is euclidian distance.
 * May work if retrofitted with other metrics, but that is purely
 * accidental.
 *
 * Note: results can become unpredictable if values are different but so
 * close together that rounding errors in computing their mean result in
 * all data being on one side of the mean. Performance degrades when
 * this occurs. Nearest neighbour search tested to work up to range
 * [1, 1 + 5e-16).
 *
 * Ideas for path ordering and bounds-overlap-ball come from:
 *   NEAL SAMPLE, MATTHEW HAINES, MARK ARNOLD, TIMOTHY PURCELL,
 *     'Optimizing Search Strategies in k-d Trees'
 *     http://ilpubs.stanford.edu:8090/723/
 *
 * Actual path ordering is based on split value, which is cheaper than
 * full distance calculation.
 *
 * Computation of variance from:
 *   John Cook, 'Accurately computing running variance'
 *     http://www.johndcook.com/standard_deviation.html
 *
 * Terminology note: points are called BrKDNodes. They must all be
 * descended from BrKDNode class. Position in k-d space is stored in each
 * BrKDNode's domain member. This is to avoid conflicting with already
 * existing classes referring to geometric points.
 *
 * Terminology comes from:
 *   Andrew Moore, 'An intoductory tutorial on kd-trees'
 *     http://www.autonlab.org/autonweb/14665
 *
 * @author duyn
 */

public final class BrKDTree<X extends BrKDNode> {

	final Queue<X> data;
	BrKDTree<X> left = null, right = null;

	// These aren't initialised until add() is called.
	private double[] exMean = null, exSumSqDev = null;

	// Optimisation when sub-tree contains only duplicates
	private boolean singularity = true;

	// Number of BrKDNodes to hold in a leaf before splitting
	private final int bucketSize;

	// Split properties. Not initialised until split occurs.
	private int splitDim = 0;
	private double split = Double.NaN;

	// Optimisation for searches. This lets us skip a node if its
	// scope intersects with a search hypersphere but it doesn't contain
	// any points that actually intersect.
	private double[] contentMax = null, contentMin = null;

	private static final int DEFAULT_BUCKET_SIZE = 10;

	public BrKDTree() {
		this(DEFAULT_BUCKET_SIZE);
	}

	public BrKDTree(int bucketSize) {
		this.bucketSize = bucketSize;
		this.data = new ArrayDeque<X>();
	}

	public void clear() {
		data.clear();
		left = null; right = null;
		exMean = null; exSumSqDev = null;
		singularity = true;
		splitDim = 0;
		split = Double.NaN;
		contentMax = null;
		contentMin = null;
	}
	
	//
	// PUBLIC METHODS
	//

	public void
	add(X ex) {
		BrKDTree<X> tree = addNoSplit(this, ex);
		if (shouldSplit(tree)) {
			split(tree);
		}
	}

	public void
	addAll(Collection<X> exs) {
		// Some spurious function calls. Optimised for readability over
		// efficiency.
		final Set<BrKDTree<X>> modTrees =
			new HashSet<BrKDTree<X>>();
		for(X ex : exs) {
			modTrees.add(addNoSplit(this, ex));
		}

		for(BrKDTree<X> tree : modTrees) {
			if (shouldSplit(tree)) {
				split(tree);
			}
		}
	}

	public Iterable<PrioNode<X>>
	search(double[] query, int nResults) {
		// Forward to a static method to avoid accidental reference to
		// instance variables while descending the tree
		return search(this, query, nResults);
	}

	//public Iterable<PrioNode<X>>
	public ArrayList<X>
	searchByDistance(double[] query, double distance) {
		// Forward to a static method to avoid accidental reference to
		// instance variables while descending the tree
		return searchByDistance(this, query, distance);
	}
	
	public ArrayList<X>
	searchAlongLine(double[] queryStart, double[] queryStop, double distance) {
		// This essentially searches along a cylinder with half-sphere caps
		//
		// Forward to a static method to avoid accidental reference to
		// instance variables while descending the tree
		return searchAlongLine(this, queryStart, queryStop, distance);
	}
	
	//
	// IMPLEMENTATION DETAILS
	//

	private final boolean
	isTree() { return left != null; }

	private int
	dimensions() { return contentMax.length; }

	// Addition

	// Adds an BrKDNode without splitting overflowing leaves.
	// Returns leaf to which BrKDNode was added.
	private static <X extends BrKDNode> BrKDTree<X>
	addNoSplit(BrKDTree<X> tree, X ex) {
		// Some spurious function calls. Optimised for readability over
		// efficiency.
		BrKDTree<X> cursor = tree;
		while (cursor != null) {
			updateBounds(cursor, ex);
			if (cursor.isTree()) {
				// Sub-tree
				cursor = ex.domain[cursor.splitDim] <= cursor.split
					? cursor.left : cursor.right;
			} else {
				// Leaf

				// Add BrKDNode to leaf
				cursor.data.add(ex);

				// Calculate running mean and sum of squared deviations
				final int nExs = cursor.data.size();
				final int dims = cursor.dimensions();
				if (nExs == 1) {
					cursor.exMean = Arrays.copyOf(ex.domain, dims);
					cursor.exSumSqDev = new double[dims];
				} else {
					for(int d = 0; d < dims; d++) {
						final double coord = ex.domain[d];
						final double oldMean = cursor.exMean[d], newMean;
						cursor.exMean[d] = newMean =
							oldMean + (coord - oldMean)/nExs;
						cursor.exSumSqDev[d] = cursor.exSumSqDev[d]
							+ (coord - oldMean)*(coord - newMean);
					}
				}

				// Check that data are still uniform
				if (cursor.singularity) {
					final Queue<X> cExs = cursor.data;
					if (cExs.size() > 0 && !ex.collocated(cExs.peek()))
						cursor.singularity = false;
				}

				// Finished walking
				return cursor;
			}
		}
		return null;
	}

	private static <X extends BrKDNode> void
	updateBounds(BrKDTree<X> tree, BrKDNode ex) {
		final int dims = ex.domain.length;
		if (tree.contentMax == null) {
			tree.contentMax = Arrays.copyOf(ex.domain, dims);
			tree.contentMin = Arrays.copyOf(ex.domain, dims);
		} else {
			for(int d = 0; d < dims; d++) {
				final double dimVal = ex.domain[d];
				if (dimVal > tree.contentMax[d])
					tree.contentMax[d] = dimVal;
				else if (dimVal < tree.contentMin[d])
					tree.contentMin[d] = dimVal;
			}
		}
	}

	// Splitting (internal operation)

	private static <X extends BrKDNode> boolean
	shouldSplit(BrKDTree<X> tree) {
		return tree.data.size() > tree.bucketSize
			&& !tree.singularity;
	}

	@SuppressWarnings("unchecked") private static <X extends BrKDNode> void
	split(BrKDTree<X> tree) {
		assert !tree.singularity;
		// Find dimension with largest variance to split on
		double largestVar = -1;
		int splitDim = 0;
		for(int d = 0; d < tree.dimensions(); d++) {
			// Don't need to divide by number of data to find largest
			// variance
			final double var = tree.exSumSqDev[d];
			if (var > largestVar) {
				largestVar = var;
				splitDim = d;
			}
		}

		// Find mean as position for our split
		double splitValue = tree.exMean[splitDim];

		// Check that our split actually splits our data. This also lets
		// us bulk load data into sub-trees, which is more likely
		// to keep optimal balance.
		final Queue<X> leftExs = new ArrayDeque<X>();
		final Queue<X> rightExs = new ArrayDeque<X>();
		for(X s : tree.data) {
			if (s.domain[splitDim] <= splitValue)
				leftExs.add(s);
			else
				rightExs.add(s);
		}
		int leftSize = leftExs.size();
		final int treeSize = tree.data.size();
		if (leftSize == treeSize || leftSize == 0) {
			System.err.println(
				"WARNING: Randomly splitting non-uniform tree");
			// We know the data aren't all the same, so try picking
			// an BrKDNode and a dimension at random for our split point

			// This might take several tries, so we copy our data to
			// an array to speed up process of picking a random point
			Object[] exs = tree.data.toArray();
			while (leftSize == treeSize || leftSize == 0) {
				leftExs.clear();
				rightExs.clear();

				splitDim = (int)
					Math.floor(Math.random()*tree.dimensions());
				final int splitPtIdx = (int)
					Math.floor(Math.random()*exs.length);
				// Cast is inevitable consequence of java's inability to
				// create a generic array
				splitValue = ((X)exs[splitPtIdx]).domain[splitDim];
				for(X s : tree.data) {
					if (s.domain[splitDim] <= splitValue)
						leftExs.add(s);
					else
						rightExs.add(s);
				}
				leftSize = leftExs.size();
			}
		}

		// We have found a valid split. Start building our sub-trees
		final BrKDTree<X> left = new BrKDTree<X>(tree.bucketSize);
		final BrKDTree<X> right = new BrKDTree<X>(tree.bucketSize);
		left.addAll(leftExs);
		right.addAll(rightExs);

		// Finally, commit the split
		tree.splitDim = splitDim;
		tree.split = splitValue;
		tree.left = left;
		tree.right = right;

		// Let go of data (and their running stats) held in this leaf
		tree.data.clear();
		tree.exMean = tree.exSumSqDev = null;
	}

	// Searching
	
	// ---------------   Search along line
	
	private static <X extends BrKDNode> ArrayList<X>
	searchAlongLine(BrKDTree<X> tree, double[] queryStart, double[] queryDir, double distance) {
		int nResults = tree.data.size();
		//final SearchState<X> state = new SearchState<X>(nResults);
		//final FastBinaryHeap<X> results = new FastBinaryHeap<X>(
		//		nResults, 4, FastBinaryHeap.MAX);
		//final ArrayList<X> results = new ArrayList<X>(nResults);
		ArrayList<X> results = new ArrayList<X>();
		
		final Deque<SearchStackEntry<X>> stack =
			new ArrayDeque<SearchStackEntry<X>>();
		if (tree.contentMin != null)
			stack.addLast(new SearchStackEntry<X>(false, tree));
//TREE_WALK:
		while (!stack.isEmpty()) {
			final SearchStackEntry<X> entry = stack.removeLast();
			final BrKDTree<X> cur = entry.tree;

			/*if (entry.needBoundsCheck && state.results.size() >= nResults) {
				/*final double d = minDistanceSqFrom(query,
					cur.contentMin, cur.contentMax);*
				if ( distance > state.results.peek().priority )
					continue TREE_WALK;
			}*/

			if (cur.isTree()) {
				searchTreeAlongLine(queryStart, queryDir, cur, stack);
			} else {
				searchLeafAlongLine(queryStart, queryDir, cur, results, distance);
			}
		}		

		return results;
	}
	
	private static <X extends BrKDNode> void
	searchTreeAlongLine(double[] queryStart, double[] queryDir, BrKDTree<X> tree,
		Deque<SearchStackEntry<X>> stack)
	{
		// The near/far stuff in this function is expected to not affect search in a significant way 
		BrKDTree<X> nearTree = tree.left, farTree = tree.right;
		if (queryStart[tree.splitDim] > tree.split) {
			nearTree = tree.right;
			farTree = tree.left;
		}

		// These variables let us skip empty sub-trees
		boolean nearEmpty = nearTree.contentMin == null;
		boolean farEmpty = farTree.contentMin == null;

		// Add nearest sub-tree to stack later so we descend it
		// first. This is likely to constrict our max distance
		// sooner, resulting in less visited nodes
		if (!farEmpty) {
			stack.addLast(new SearchStackEntry<X>(true, farTree));
		}

		if (!nearEmpty) {
			stack.addLast(new SearchStackEntry<X>(true, nearTree));
		}
	}
	
	private static <X extends BrKDNode> void
	searchLeafAlongLine(double[] queryStart, double[] queryDir, BrKDTree<X> leaf, ArrayList<X> results, double distance) {
		double exD = Double.NaN;
		for(X ex : leaf.data) {
			exD = Double.NaN;
			if (!leaf.singularity || Double.isNaN(exD)) {
				exD = distanceSqFromLine(queryStart, queryDir, ex.domain);
			}

			if ( exD < distance ) {
				//System.out.println( "Within distance " + distance + " " + exD );
				results.add(ex);
			} 
		}
	}

	// ---------------   Search by distance
	//private static <X extends BrKDNode> Iterable<PrioNode<X>>
	private static <X extends BrKDNode> ArrayList<X>
	searchByDistance(BrKDTree<X> tree, double[] query, double distance) {
		int nResults = tree.data.size();
		//final SearchState<X> state = new SearchState<X>(nResults);
		//final FastBinaryHeap<X> results = new FastBinaryHeap<X>(
		//		nResults, 4, FastBinaryHeap.MAX);
		//final ArrayList<X> results = new ArrayList<X>(nResults);
		ArrayList<X> results = new ArrayList<X>();
		
		double distance_sq = distance * distance; // easier than taking sqrt for every entry
		
		final Deque<SearchStackEntry<X>> stack =
			new ArrayDeque<SearchStackEntry<X>>();
		if (tree.contentMin != null)
			stack.addLast(new SearchStackEntry<X>(false, tree));
//TREE_WALK:
		while (!stack.isEmpty()) {
			final SearchStackEntry<X> entry = stack.removeLast();
			final BrKDTree<X> cur = entry.tree;

			/*if (entry.needBoundsCheck && state.results.size() >= nResults) {
				/*final double d = minDistanceSqFrom(query,
					cur.contentMin, cur.contentMax);*
				if ( distance > state.results.peek().priority )
					continue TREE_WALK;
			}*/

			if (cur.isTree()) {
				searchTree(query, cur, stack);
			} else {
				searchLeafByDistance(query, cur, results, distance_sq);
			}
		}		

		return results;
	}
	
	private static <X extends BrKDNode> void
	searchLeafByDistance(double[] query, BrKDTree<X> leaf, ArrayList<X> results, double distance) {
		double exD = Double.NaN;
		for(X ex : leaf.data) {
			exD = Double.NaN;
			if (!leaf.singularity || Double.isNaN(exD)) {
				exD = distanceSqFrom(query, ex.domain);
			}

			if ( exD < distance ) {
				//System.out.println( "Within distance " + distance + " " + exD );
				results.add(ex);
			} /*else {
				System.out.println( "Not within distance " + distance + " " + exD + " " + query[0] + "," + query[1] + "," + query[2] + " " +
						ex.domain[0] + "," + ex.domain[1] + "," + ex.domain[2] );
			}*/
		}
	}
	
	// ----------- Search by number of results
	
	// May return more results than requested if multiple data have
	// same distance from target.
	//
	// Note: this function works with squared distances to avoid sqrt()
	// operations
	private static <X extends BrKDNode> Iterable<PrioNode<X>>
	search(BrKDTree<X> tree, double[] query, int nResults) {
		final SearchState<X> state = new SearchState<X>(nResults);
		final Deque<SearchStackEntry<X>> stack =
			new ArrayDeque<SearchStackEntry<X>>();
		if (tree.contentMin != null)
			stack.addLast(new SearchStackEntry<X>(false, tree));
TREE_WALK:
		while (!stack.isEmpty()) {
			final SearchStackEntry<X> entry = stack.removeLast();
			final BrKDTree<X> cur = entry.tree;

			if (entry.needBoundsCheck && state.results.size() >= nResults) {
				final double d = minDistanceSqFrom(query,
					cur.contentMin, cur.contentMax);
				if (d > state.results.peek().priority)
					continue TREE_WALK;
			}

			if (cur.isTree()) {
				searchTree(query, cur, stack);
			} else {
				searchLeaf(query, cur, state);
			}
		}

		return state.results;
	}

	
	private static <X extends BrKDNode> void
	searchTree(double[] query, BrKDTree<X> tree,
		Deque<SearchStackEntry<X>> stack)
	{
		BrKDTree<X> nearTree = tree.left, farTree = tree.right;
		if (query[tree.splitDim] > tree.split) {
			nearTree = tree.right;
			farTree = tree.left;
		}

		// These variables let us skip empty sub-trees
		boolean nearEmpty = nearTree.contentMin == null;
		boolean farEmpty = farTree.contentMin == null;

		// Add nearest sub-tree to stack later so we descend it
		// first. This is likely to constrict our max distance
		// sooner, resulting in less visited nodes
		if (!farEmpty) {
			stack.addLast(new SearchStackEntry<X>(true, farTree));
		}

		if (!nearEmpty) {
			stack.addLast(new SearchStackEntry<X>(true, nearTree));
		}
	}

	private static <X extends BrKDNode> void
	searchLeaf(double[] query, BrKDTree<X> leaf, SearchState<X> state) {
		double exD = Double.NaN;
		for(X ex : leaf.data) {
			exD = Double.NaN;
			if (!leaf.singularity || Double.isNaN(exD)) {
				exD = distanceSqFrom(query, ex.domain);
			}

			if (examine(exD, state)) {
				state.results.offer(exD, ex);
			}
		}
	}

	private static <X extends BrKDNode> boolean
	examine(double distance, SearchState<X> state) {
		return state.results.size() < state.nResults
			|| distance < state.results.peek().priority;
	}

	// Distance calculations

	// Gets distance from target of nearest point on hyper-rect defined
	// by supplied min and max bounds
	private static double
	minDistanceSqFrom(double[] target, double[] min, double[] max) {
		// Note: profiling shows this is called lots of times, so it pays
		// to be well optimised
		double distanceSq = 0;
		for(int d = 0; d < target.length; d++) {
			if (target[d] < min[d]) {
				final double dst = min[d] - target[d];
				distanceSq += dst*dst;
			} else if (target[d] > max[d]) {
				final double dst = max[d] - target[d];
				distanceSq += dst*dst;
			}
		}
		return distanceSq;
	}

	private static double
	distanceSqFrom(double[] p1, double[] p2) {
		// Note: profiling shows this is called lots of times, so it pays
		// to be well optimised
		double dSq = 0;
		for(int d = 0; d < p1.length; d++) {
			final double dst = p1[d] - p2[d];
			if (dst != 0)
				dSq += dst*dst;
		}
		return dSq;
	}
	
	private static double[] cross( double[] v1, double[] v2 ) {
		return ( new double[]{ ( v1[1]*v2[2] - v1[2]*v2[1] ), ( v1[2]*v2[0] - v1[0]*v2[2] ), ( v1[0]*v2[1] - v1[1]*v2[0] ) } );
	}
		
	private static double length( double[] v ) {
		return ( v[0]*v[0] + v[1]*v[1] + v[2]*v[2] );
	}
	
	private static double
	distanceSqFromLine(double[] c, double[] v, double[] p) {
		// call with c, the base of the line, and v, the direction vector from c
		// p is the point in question
		//
		// Note: profiling shows this is called lots of times, so it pays
		// to be well optimised
		double dSq = 0;
		
		double[] a = new double[]{ p[0] - c[0], p[1] - c[1], p[2] - c[2] };
		
		double[] aXv = cross(a,v);
		double la = length( a );
		double sinTheta = length( aXv ) / ( la * length( v ) );
		
		return la * sinTheta;
	}

	//
	// class SearchStackEntry
	//

	private static class SearchStackEntry<X extends BrKDNode> {
		public final boolean needBoundsCheck;
		public final BrKDTree<X> tree;

		public SearchStackEntry(boolean needBoundsCheck,
			BrKDTree<X> tree)
		{
			this.needBoundsCheck = needBoundsCheck;
			this.tree = tree;
		}
	}

	//
	// class SearchState
	//
	// Holds data about current state of the search. Used for live updating
	// of pruning distance.

	private static class SearchState<X extends BrKDNode> {
		final int nResults;
		final FastBinaryHeap<X> results;

		public SearchState(int nResults) {
			this.nResults = nResults;
			results = new FastBinaryHeap<X>(
				nResults, 4, FastBinaryHeap.MAX);
		}
	}

}

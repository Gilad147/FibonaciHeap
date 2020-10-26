import java.util.HashMap;
import java.util.Map;

/* Einav Brosh
 * einavbrosh
 * 209335132
 */

/* Gilad Baruch
 * giladbaruch
 * 308015379
 */

/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers. complexity: O(1)
 */
public class FibonacciHeap {
	public static int allLinksDone;
	public static int allCutsDone;

	private final NodeFactory factory = new NodeFactory();
	private int heapSize;
	private HeapNode first;
	private HeapNode min;
	private int treesAmount;
	private int markedCount;

	/**
	 * public int logTheta(int x)
	 *
	 * returns the superior value of log(x) with base theta. complexity: O(1)
	 */
	public static int logTheta(int x) {
		return (int) Math.ceil(1.44 * (Math.log(x) / Math.log(2))) + 1;
	}

	/**
	 * public boolean isEmpty()
	 *
	 * precondition: none
	 * 
	 * The method returns true if and only if the heap is empty. Complexity: O(1)
	 * 
	 */
	public boolean isEmpty() {
		return this.heapSize == 0;
	}

	/**
	 * public HeapNode insert(int key)
	 *
	 * Creates a node (of type HeapNode) which contains the given key, and inserts
	 * it into the heap. Complexity: O(1)
	 */
	public HeapNode insert(int key) {
		HeapNode node = this.factory.createNode(key);
		insertNode(node);
		return node;
	}

	/**
	 * private void insertNode(HeapNode node)
	 *
	 * Inserts an existing heapNode into the heap. Complexity: O(1)
	 */
	private void insertNode(HeapNode node) {
		if (this.isEmpty()) {
			this.min = node;
		} else {
			node.setNext(this.first);
			node.setPrev(this.first.getPrev());
			this.first.setPrev(node);
			node.getPrev().setNext(node);
			if (this.min.getKey() > node.getKey()) {
				this.min = node;
			}
		}
		this.first = node;
		this.heapSize += 1;
		this.treesAmount++;
	}

	/**
	 * public void deleteMin()
	 *
	 * Delete the node containing the minimum key. If heap is empty nothing happens.
	 * Complexity: amortized O(log(n))
	 */
	public void deleteMin() {
		if (!this.isEmpty()) {
			if (this.heapSize == 1) {// min was only node in heap
				this.clearHeap();
			} else {
				if (this.min.getChild() != null) { // min has children
					// for each child of min, add the child as a tree-root
					HeapNode childPointer = this.min.getChild();
					HeapNode nextTreePointer = this.min.getNext();
					for (int i = 0; i < this.min.getRank(); i++) {
						childPointer.setParent(null);
						this.treesAmount++;
						childPointer = childPointer.getNext();
						childPointer.setMarked(false);
					}
					// childPointer is at first child
					HeapNode lastChild = childPointer.getPrev();
					this.min.setNext(childPointer);
					childPointer.setPrev(this.min);
					lastChild.setNext(nextTreePointer);
					nextTreePointer.setPrev(lastChild);
					this.min.setChild(null);
				}

				/*
				 * AT THIS POINT, MIN SHOULD HAVE NO CHILDREN. ALSO, THERE MUST BE MORE THAN 1
				 * TREE IN THE HEAP (min+child or min+other treefrom before)
				 */

				// remove z from roots-list
				if (this.min == this.first) {
					this.first = this.min.getNext();
				}
				this.treesAmount--;
				this.min.getPrev().setNext(this.min.getNext());
				this.min.getNext().setPrev(this.min.getPrev());
				this.heapSize--;
				// --- not sure this is needed --- this.min = this.first; // temporary, will be
				// altered in reconsolidation
				// consolidate
				this.consolidate();
			}
		}
	}

	/**
	 * public void consolidate()
	 *
	 * precondition: heap is not empty
	 *
	 * consolidates the trees in the heap into a valid binomial heap each valid
	 * binomial tree in the heap it's suitable index, according to it's rank.
	 * Complexity: amortized O(log(n))
	 */
	private void consolidate() {
		// do consolidation
		HeapNode[] buckets = this.toBuckets();
		this.fromBuckets(buckets);
	}

	/**
	 * public HeapNode[] toBuckets()
	 *
	 * precondition: heap is not empty
	 *
	 * returns an array of heepNodes that represent tree roots. Each node is in a
	 * bucket according to it's rank. Complexity: amortized O(log(n))
	 */
	private HeapNode[] toBuckets() {
		HeapNode[] treesArray = new HeapNode[logTheta(heapSize)];
		HeapNode rootsPointer = this.first;
		int amountRoots = this.treesAmount;
		for (int i = 0; i < amountRoots; i++) {
			HeapNode nextRootByOrder = rootsPointer.getNext();
			while (treesArray[rootsPointer.getRank()] != null) {
				HeapNode newMinTree = treesArray[rootsPointer.getRank()];
				HeapNode notMinTree = rootsPointer;
				treesArray[rootsPointer.getRank()] = null;
				// make sure I know who the new root will be
				if (notMinTree.getKey() < newMinTree.getKey()) {
					HeapNode temp = newMinTree;
					newMinTree = notMinTree;
					notMinTree = temp;
				}
				rootsPointer = link(this, newMinTree, notMinTree);
			}
			treesArray[rootsPointer.getRank()] = rootsPointer;
			rootsPointer = nextRootByOrder;
		}
		return treesArray;
	}

	/**
	 * public void fromBuckets(HeapNode[] treesArray)
	 *
	 * precondition: array is not empty
	 *
	 * creates a heap from an array of binomial trees. Complexity: O(log(n))
	 */
	private void fromBuckets(HeapNode[] treesArray) {
		int newHeapSize = this.heapSize;
		this.clearHeap();
		for (int i = treesArray.length - 1; i >= 0; i--) {
			if (treesArray[i] == null) {
				continue;
			} else {
				treesArray[i].setNext(treesArray[i]);
				treesArray[i].setPrev(treesArray[i]);
				this.insertNode(treesArray[i]);
			}
		}
		this.heapSize = newHeapSize;
	}

	/**
	 * public void link(HeapNode bTree1, HeapNode bTree2)
	 *
	 * precondition: trees are not empty, trees have same rank, trees are from the
	 * same heap
	 *
	 * updates this heap - in place link. Complexity: O(1)
	 *
	 */
	private HeapNode link(FibonacciHeap heap, HeapNode newMinTree, HeapNode notMinTree) {
		allLinksDone++;
		// make sure I know who the new root will be
		if (notMinTree.getKey() < newMinTree.getKey()) {
			HeapNode temp = newMinTree;
			newMinTree = notMinTree;
			notMinTree = temp;
		}
		// remove notMinTree from roots
		if (notMinTree == heap.first) {
			heap.first = notMinTree.getNext();
		}
		notMinTree.getPrev().setNext(notMinTree.getNext());
		notMinTree.getNext().setPrev(notMinTree.getPrev());
		this.treesAmount--;
		// link
		HeapNode oldChild = newMinTree.getChild();
		newMinTree.setChild(notMinTree);
		if (oldChild != null) {
			notMinTree.setNext(oldChild);
			notMinTree.setPrev(oldChild.getPrev());
			oldChild.setPrev(notMinTree);
			notMinTree.getPrev().setNext(notMinTree);
		} else {
			notMinTree.setNext(notMinTree);
			notMinTree.setPrev(notMinTree);
		}
		notMinTree.setParent(newMinTree);
		newMinTree.setRank(newMinTree.getRank() + 1);
		return newMinTree;
	}

	/**
	 * private void clearHeap()
	 *
	 * restarts the heap to empty state. Complexity: O(1)
	 */
	private void clearHeap() {
		this.first = null;
		this.heapSize = 0;
		this.markedCount = 0;
		this.min = null;
		this.treesAmount = 0;
	}

	/**
	 * public HeapNode findMin()
	 *
	 * Return the node of the heap whose key is minimal. Complexity: O(1)
	 */
	public HeapNode findMin() {
		return this.min;
	}

	/**
	 * public HeapNode getFirst()
	 *
	 * Return the first node of the heap
	 *
	 */
	public HeapNode getFirst() {
		return this.first;
	}

	/**
	 * public void meld (FibonacciHeap heap2)
	 *
	 * Meld the heap with heap2. Complexity: O(1)
	 */
	public void meld(FibonacciHeap heap2) {
		this.heapSize += heap2.heapSize;
		this.treesAmount += heap2.treesAmount;
		this.markedCount += heap2.markedCount;
		if (!isEmpty() && !heap2.isEmpty()) {
			if (this.min.getKey() > heap2.findMin().getKey()) {
				this.min = heap2.findMin();
			}
			HeapNode meldNode = this.first.getPrev();
			HeapNode endNode = heap2.first.getPrev();
			meldNode.setNext(heap2.first);
			meldNode.getNext().setPrev(meldNode);
			endNode.setParent(this.first);
			this.first.setPrev(endNode);
		} else {
			if (!heap2.isEmpty()) {
				this.first = heap2.first;
				this.min = heap2.findMin();
			}
		}
	}

	/**
	 * public int size()
	 *
	 * Return the number of elements in the heap. Complexity: O(1)
	 * 
	 */
	public int size() {
		return this.heapSize;
	}

	/**
	 * public int[] countersRep()
	 *
	 * Return a counters array, where the value of the i-th entry is the number of
	 * trees of order i in the heap. Complexity: O(n)
	 */
	public int[] countersRep() {
		int[] counter = new int[logTheta(heapSize)];
		HeapNode rootsPointer = this.first;
		do {
			int thisRank = rootsPointer.getRank();
			counter[thisRank]++;
			rootsPointer = rootsPointer.getNext();
		} while (!rootsPointer.equals(this.first));
		return counter;
	}

	/**
	 * public void delete(HeapNode x)
	 *
	 * Deletes the node x from the heap. Complexity: amortized O(log(n))
	 * 
	 * @throws IllegalDecreaseKeyException
	 *
	 */
	public void delete(HeapNode x) {
		if (this.min.getKey() == x.getKey()) {
			// x is the min tree root
			deleteMin();
		} else {
			decreaseKey(x, x.getKey() - this.min.getKey() + 1);
			deleteMin();
		}
	}

	/**
	 * public void decreaseKey(HeapNode x, int delta)
	 *
	 * The function decreases the key of the node x by delta. The structure of the
	 * heap should be updated to reflect this change (for example, the cascading
	 * cuts procedure should be applied if needed). Complexity: amortized O(1)
	 * 
	 * @throws IllegalDecreaseKeyException
	 */
	public void decreaseKey(HeapNode x, int delta) {
		if (x.getKey() - delta < 0) {
			System.out.println("delta is too big");
		} else { // input is valid
			if (x.getKey() > this.min.getKey() && x.getParent() != null
					&& (x.getKey() - delta) <= x.getParent().getKey()) { // x isn't already min and is needs to change
				x.setKey(x.getKey() - delta); // position
				HeapNode parentX = x.getParent();
				this.cut(x, parentX);
				this.cascadingCuts(parentX);
			} else { // no change in position needed
				x.setKey(x.getKey() - delta);
			}
			if (x.getKey() < this.min.getKey()) {
				this.min = x;
			}
		}
	}

	/**
	 * public void cut(HeapNode x, HeapNode parentX)
	 *
	 * The function removes x from the list of children of parentX and adds it as a
	 * new treeRoot in the heap. The structure of the heap is updated to reflect
	 * this change. Complexity: O(1)
	 * 
	 */
	public void cut(HeapNode x, HeapNode parentX) {
		allCutsDone++;
		// remove x from previous position
		if (x.getNext().equals(x)) {
			parentX.setChild(null);
		} else {
			if (parentX.getChild().equals(x)) {
				parentX.setChild(x.getNext());
			}
			x.getPrev().setNext(x.getNext());
			x.getNext().setPrev(x.getPrev());
		}
		x.setParent(null);
		parentX.setRank(parentX.getRank() - 1);
		// insert x as new treeRoot
		this.insertNode(x); // insert node increases size by 1
		this.heapSize--;
		x.setMarked(false);
	}

	/**
	 * public void cascadingCuts(HeapNode parentX)
	 *
	 * precondition: the son of parentX was just cut off
	 * 
	 * The function works recursively to make all roots legal - no more than one
	 * child removed. Complexity: amortized O(1)
	 * 
	 */
	public void cascadingCuts(HeapNode parentX) {
		if (!parentX.isMarked() && parentX.getParent() != null) { // x was the first child removed from parentX, and it
			// is not a root
			parentX.setMarked(true);
		} else {
			if (parentX.getParent() != null) {
				HeapNode superParent = parentX.getParent();
				cut(parentX, parentX.getParent());
				cascadingCuts(superParent);
			}
		}
	}

	/**
	 * public int potential()
	 *
	 * This function returns the current potential of the heap, which is: Potential
	 * = #trees + 2*#marked The potential equals to the number of trees in the heap
	 * plus twice the number of marked nodes in the heap. Complexity: O(1)
	 */
	public int potential() {
		return this.treesAmount + (2 * this.markedCount);
	}

	/**
	 * public static int totalLinks()
	 *
	 * This static function returns the total number of link operations made during
	 * the run-time of the program. A link operation is the operation which gets as
	 * input two trees of the same rank, and generates a tree of rank bigger by one,
	 * by hanging the tree which has larger value in its root on the tree which has
	 * smaller value in its root. Complexity: O(1)
	 */
	public static int totalLinks() {
		return allLinksDone;
	}

	/**
	 * public static int totalCuts()
	 *
	 * This static function returns the total number of cut operations made during
	 * the run-time of the program. A cut operation is the operation which
	 * diconnects a subtree from its parent (during decreaseKey/delete methods).
	 * Complexity: O(1)
	 */
	public static int totalCuts() {
		return allCutsDone;
	}

	/**
	 * public static int[] kMin(FibonacciHeap H, int k)
	 *
	 * This static function returns the k minimal elements in a binomial tree H. The
	 * function should run in O(k(logk + deg(H)).
	 */
	public static int[] kMin(FibonacciHeap H, int k) {
		int[] kSmallest = new int[k];
		if (k == 0) {
			return kSmallest;
		}
		Map<Integer, HeapNode> potentialPointers = new HashMap<>();
		FibonacciHeap K = new FibonacciHeap();
		K.insert(H.min.getKey());
		potentialPointers.put(H.min.getKey(), H.min);
		int hMin = H.heapSize;
		for (int i = 0; i < Math.min(k, hMin); i++) {// min in case k>treesize
			kSmallest[i] = K.min.getKey();
			HeapNode thisParent = potentialPointers.get(K.min.getKey());
			HeapNode childPointer = thisParent.getChild();
			if (childPointer != null) {
				for (int j = 0; j < thisParent.getRank(); j++) {
					// for each child, add child to K heap and map so pointer is saved
					potentialPointers.put(childPointer.getKey(), childPointer);
					K.insert(childPointer.getKey());
					childPointer = childPointer.getNext();
				}
			}
			potentialPointers.remove(K.min.getKey());
			K.deleteMin();
		}
		return kSmallest;
	}

	/**
	 * public class HeapNode
	 * 
	 * If you wish to implement classes other than FibonacciHeap (for example
	 * HeapNode), do it in this file, not in another file
	 * 
	 */

	public class HeapNode {

		private int key;
		private int rank;
		private boolean marked;
		private HeapNode child;
		private HeapNode next;
		private HeapNode prev;
		private HeapNode parent;

		public HeapNode(int key) {
			this.key = key;
			this.rank = 0;
			this.child = null;
			this.next = this;
			this.prev = this;
			this.parent = null;
			this.marked = false;
		}

		public int getKey() {
			return this.key;
		}

		public void setKey(int k) {
			this.key = k;
		}

		public int getRank() {
			return this.rank;
		}

		public void setRank(int x) {
			this.rank = x;
		}

		public boolean isMarked() {
			return this.marked;
		}

		public void mark() {
			this.marked = true;

		}

		public void setMarked(boolean m) {
			if (m) {
				FibonacciHeap.this.markedCount++;
			} else {
				if (this.marked) {
					FibonacciHeap.this.markedCount--;
				}
			}
			this.marked = m;

		}

		public HeapNode getChild() {
			return this.child;
		}

		public void setChild(HeapNode node) {
			this.child = node;
		}

		public HeapNode getNext() {
			return this.next;
		}

		public void setNext(HeapNode node) {
			this.next = node;
		}

		public HeapNode getPrev() {
			return this.prev;
		}

		public void setPrev(HeapNode node) {
			this.prev = node;
		}

		public HeapNode getParent() {
			return this.parent;
		}

		public void setParent(HeapNode node) {
			this.parent = node;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + key;
			result = prime * result + rank;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			HeapNode other = (HeapNode) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (key != other.key)
				return false;
			if (rank != other.rank)
				return false;
			return true;
		}

		private FibonacciHeap getEnclosingInstance() {
			return FibonacciHeap.this;
		}

	}

	public class NodeFactory {
		public HeapNode createNode(int k) {
			return new HeapNode(k);
		}
	}

}

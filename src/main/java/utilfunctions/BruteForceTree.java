package utilfunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores all possible mod-type mappings
 * @author linki
 *
 */
public class BruteForceTree {
	
	private int numOfLevels = 0;
	
	private int numOfBranches = 0;
	
	// all nodes
	private List<node> nodes = null;
	
	// a node in the tree
	public static class node {
		// type id
		int nodeId = -1;
		
		// mod id
		int level = 0;
		
		// mapping
		List<Integer> pathIds = null;
		
		// constructor
		node( node parent, int myid ) {
			// set node id
			this.nodeId = myid;	
			
			// set level
			if (parent == null) {
				// root node
				this.level = 0;
				
				// path ids
				this.pathIds = new ArrayList<Integer>();
				this.pathIds.add(this.nodeId);

			} else {
				// extend parent info
				this.level = parent.level + 1;
				
				// path ids
				this.pathIds = new ArrayList<Integer>(parent.pathIds);
				this.pathIds.add(this.nodeId);
			}
		}
		
		public List<Integer> getPath() {
			return pathIds;
		}
		
		public void printpath() {
			for (int i: this.pathIds) {
				System.out.printf("%d ",i);
			}
			System.out.print('\n');
		}
	}
	
	// if a node is a leaf node of this tree
	public static boolean isleaf(node nod, BruteForceTree tree) {
		// if in max level
		if (nod.level == tree.getNumOfLevels()-1) {
			return true;
		} else {
			return false;
		}
	}

	// extend a level based on an existing tree
	public static BruteForceTree extend(BruteForceTree basetree) {
		List<node> leafs = new ArrayList<node>();
		// collect all leaf nodes
		for (node nod: basetree.getNodes()) {
			if (isleaf(nod, basetree)) {
				leafs.add(nod);
			}
		}
		
		// extend a level from them
		for (node nod: leafs) {
			for (int i=0; i<basetree.getNumOfBranches(); i++) {
				node newnode = new node(nod, i);
				basetree.addNode(newnode);
				//newnode.printpath();
			}
		}
	
		basetree.setNumOfLevels(basetree.getNumOfLevels()+1);
		return basetree;
	}
	
	// print all mappings
	public static void printLeafPaths(BruteForceTree tree) {
		for (node nod: tree.getNodes()) {
			if (isleaf(nod, tree)) {
				//System.out.printf("level %d:", nod.level);
				nod.printpath();
			}
			
		}
	}

	//a trivial tree with one node 
	public BruteForceTree(int numOfBranches) {
		// init info
		setNumOfLevels(1);
		setNumOfBranches(numOfBranches);
		setNodes(new ArrayList<node>());
		
		// one node
		node root = new node(null, -1);
		addNode(root);
	}
	
	//a brute force tree
	public static BruteForceTree bruteforcetree(int numOfLevels, int numOfBranches) {
		// a trivial base
		BruteForceTree basetree = new BruteForceTree(numOfBranches);
		// extend levels
		for (int i=1; i<numOfLevels; i++) {
			basetree = extend(basetree);
		}
		return basetree;
	}
	
	
	public List<node> getLeafs() {
		List<node> leafs = new ArrayList<node>();
		for (node nod: this.getNodes()) {
			if(isleaf(nod, this)) {
				leafs.add(nod);
			}
		}
		return leafs;
	}
	
	public static void main(String[] args) {		
		BruteForceTree mytest = bruteforcetree(4,3);
		printLeafPaths(mytest);

	}


	public int getNumOfLevels() {
		return numOfLevels;
	}


	public void setNumOfLevels(int numOfLevels) {
		this.numOfLevels = numOfLevels;
	}


	public int getNumOfBranches() {
		return numOfBranches;
	}


	public void setNumOfBranches(int numOfBranches) {
		this.numOfBranches = numOfBranches;
	}
	
	public void setNodes(List<node> nodes) {
		this.nodes = nodes;
	}

	public List<node> getNodes() {
		return nodes;
	}
	
	public void addNode(node newnode) {
		nodes.add(newnode);
	}

}

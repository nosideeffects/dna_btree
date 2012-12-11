import java.io.*;
import java.util.ArrayList;

public class BTree<T extends Comparable<T>> {
	
	private int degree;
	private int sequenceLength;
	private BTreeNode<T> root;

	public BTree(int degree, int sequenceLength) {
		
		this.degree = degree;
		this.sequenceLength = sequenceLength;
		
		if (degree < 1) {
			
			System.err.println("Invalid degree. Must be a positive integer.");
			System.exit(3);
		}
		
		if (sequenceLength < 1 || sequenceLength > 31) {
			
			System.err.println("Invalid sequence length. Muse be an integer between 1 and 31 (inclusive).");
			System.exit(4);
		}
		
		this.root = new BTreeNode<T>();
	}
	
	public BTree(String bTreeFile) {
		
		try {
			
			FileInputStream fis = new FileInputStream(bTreeFile);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			
			//@TODO Do something
			/*
			
			*/
			
			dis.close();
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			
			
		}
	}
	
	public void insert(T key){
		BTreeNode<T> r = root;
		if (r.isFull()) {
			BTreeNode<T> s = new BTreeNode<T>();
			this.root = s;
			
			s.setChild(0, r);
		} else {
			r.insert(key);
		}
	}
	
	private class BTreeNode<T> {
		private boolean leaf;
		private long key;
		private ArrayList<TreeObject<T>> keys;
		private ArrayList<BTreeNode<T>> children; 
		
		public BTreeNode(){
			this.leaf = true;
			
			this.keys = new ArrayList<TreeObject<T>>();
			this.keys.ensureCapacity(degree * 2 - 1);
			this.children = new ArrayList<BTreeNode<T>>();
			this.children.ensureCapacity(degree*2);
			
			this.key = 0;
		}
		
		public void setChild(int index, BTreeNode<T> node){
			children.set(index, node);
		}
		
		public BTreeNode<T> getChild(int index){
			return children.get(index);
		}
		
		public void splitChild(int index){
			BTreeNode<T> z = new BTreeNode<T>();
			BTreeNode<T> y = getChild(index);
			
			z.isLeaf(y.isLeaf());
			
			// TODO: split childrne into each node
		}
		
		public void setKey(int index, TreeObject<T> obj){
			keys.set(index, obj);
		}
		
		public TreeObject<T> getKey(int index){
			return keys.get(index);
		}
		
		/**
		 * Inserted key assuming node is not full.
		 * @param key key to insert
		 */
		public void insert(T key){
			
		}
		
		/**
		 * Returns number of contains objects
		 * @return # of objects
		 */
		public int n(){
			return keys.size();
		}
		
		/**
		 * Returns true if node is full, false otherwise.
		 * @return 
		 */
		public boolean isFull(){
			return n() == (2 * degree) - 1;
		}
		
		public boolean isLeaf(){
			return this.leaf;
		}
		
		public void isLeaf(boolean leaf){
			this.leaf = leaf;
		}
	}
	
	private class TreeObject<T>{
		private int frequency;
		private T key;
		
		public TreeObject(T key){
			this.key = key;
		}
		
		public T key(){
			return this.key;
		}
		
		public int frequency(){
			return this.frequency;
		}
	}
}
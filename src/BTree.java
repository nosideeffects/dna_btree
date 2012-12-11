import java.io.*;
import java.util.ArrayList;

public class BTree<T extends Comparable<T>> {
	
	private int degree;
	private BTreeNode<T> root;

	public BTree(int degree) {
		
		this.degree = degree;
		
		if (degree < 1) {
			
			System.err.println("Invalid degree. Must be a positive integer.");
			System.exit(3);
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
			s.splitChild(0);
			s.insert(key);
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
		
		public BTreeNode<T> removeChild(int index){
			BTreeNode<T> c = children.get(index);
			children.set(index, null);
			return c;
		}
		
		public void splitChild(int index){
			BTreeNode<T> z = new BTreeNode<T>();
			BTreeNode<T> y = getChild(index);
			
			z.isLeaf(y.isLeaf());
			
			for (int j = 0; j < degree - 1; j++) {
				z.setKey(j, y.removeKey(j+degree));
			}
			
			if (!y.isLeaf()) {
				for (int j = 0; j < degree; j++) {
					z.setChild(j, y.removeChild(j+degree));
				}
			}
			
			for (int j = this.n() + 1; j > index + 1; j--) {
				this.setChild(j+1, this.removeChild(j));
			}
			this.setChild(index+1, z);
			
			for (int j = this.n(); j > index; j--){
				this.setKey(j+1,this.removeKey(j));
			}
			this.setKey(index, y.removeKey(degree));
			
			y.save();
			z.save();
			this.save();
		}
		
		/**
		 * Saves node to disk.
		 */
		private void save() {
			// TODO Auto-generated method stub
			
		}

		public void setKey(int index, TreeObject<T> obj){
			keys.set(index, obj);
		}
		
		public TreeObject<T> getKey(int index){
			return keys.get(index);
		}
		
		public TreeObject<T> removeKey(int index){
			TreeObject<T> k = keys.get(index);
			keys.set(index, null);
			return k;
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
		
		public void incrementFrequency(){
			this.frequency++;
		}
	}
}
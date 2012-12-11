import java.io.*;
import java.util.ArrayList;

public class BTree<T> {
	
	private int degree;
	private int sequenceLength;
	private BTreeNode<T> root;

	public BTree(int degree, int sequenceLength) {
		
		//@TODO Do something
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
	}
	
	public BTree(String bTreeFile) {
		
		//@TODO Do something
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
	
	private class BTreeNode<T> {
		private boolean leaf;
		private long key;
		private ArrayList<T> keys;
		private ArrayList<BTreeNode<T>> children; 
		
		public BTreeNode(){
			this.leaf = true;
			
			this.keys = new ArrayList<T>();
			this.children = new ArrayList<BTreeNode<T>>();
			
			this.key = 0;
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
	}
}
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class BTree<T extends Comparable<T> & Serializable> {

	private final static String EXTENSION = ".btree.data.k.t";
	private final static int BLOCK_SIZE = 4096;
	
	private int degree;
	private int numNodes;
	private BTreeNode<T> root;
	
	private StringBuffer sb = new StringBuffer();
	private static ArrayList<byte[]> bytes = new ArrayList<byte[]>();
	
	private RandomAccessFile raf;

	public BTree(int degree, String name) throws IOException {

		this.degree = degree;
		this.numNodes = 1;
		
		// Ensure BRAND NEW file is created.
		File btreefile = new File(name + EXTENSION);
		btreefile.delete();
		
		// Allow random access
		this.raf = new RandomAccessFile(btreefile, "rw");

		if (degree < 0) {

			System.err.println("Invalid degree. Must be a positive integer.");
			System.exit(3);
		}
		
		if (degree == 0) {
			// Select Optimal Degree
			this.degree = 97;
		}

		this.raf.writeInt(this.degree);
		this.raf.seek(this.raf.length() + 8);
		this.raf.writeInt(this.numNodes);
		this.root = new BTreeNode<T>();
	}

	public BTree(String bTreeFile) {

		try {
			this.raf = new RandomAccessFile(bTreeFile, "rw");

			FileInputStream fis = new FileInputStream(bTreeFile);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));

			// @TODO Do something
			/*
			
			*/

			dis.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}

	public void insert(T key) throws IOException {

		TreeObject<T> t_obj = findKeyObject(key);

		if (t_obj != null) {
			t_obj.incrementFrequency();
		} else {
			BTreeNode<T> r = root;
			if (r.isFull()) {
				BTreeNode<T> s = new BTreeNode<T>();
				this.root = s;
				s.isLeaf(false);

				s.setChild(0, r);
				s.splitChild(0);
				s.insert(key);
			} else {
				r.insert(key);
			}
		}
	}

	public T search(T key) {
		
		TreeObject<T> t_obj = findKeyObject(key);

		if (t_obj != null) {
			return t_obj.getKey();
		}

		return null;
	}

	private TreeObject<T> findKeyObject(T key) {
		return root.search(key);
	}

	private String build() {
		StringBuilder sb = new StringBuilder();
		sb.append(root.toString());
		sb.append(" (head)\n");
		build(sb, root, 0, "head", 0, true);
		return sb.toString();
	}

	private void build(StringBuilder sb, BTreeNode<T> node, int height,
			String prevLevel, int child, boolean first) {
		String thisLevel = "";
		for (int i = 0; i < height; i++) {
			sb.append("  ");
		}
		if (!first) {
			sb.append("--> ");
			sb.append(node.toString());
			sb.append("(");
			sb.append(prevLevel);
			sb.append(".c" + child);
			sb.append(")\n");
			thisLevel = prevLevel + ".c" + child;
			first = false;
		} else {
			thisLevel = prevLevel;
		}
		int i = 1;
		for (Object obj : node.children) {
			if (obj != null) {
				build(sb, (BTreeNode<T>) obj, height + 1, thisLevel, i, false);
				i++;
			}
		}
	}
	
	public byte[] read() {
		
		return null;
	}
	
	public long getNewOffset(){
		try {
			return this.raf.length();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Cannot allocate new node!");
			System.exit(1);
		}
		return -1;
	}
	
	public String toString() {
		return build();
	}

	@SuppressWarnings("hiding")
	private class BTreeNode<T extends Comparable<T> & Serializable> {
		private boolean leaf;
		private long key;
		/**
		 * Number of keys.
		 */
		private int n;
		private Object[] keys;
		private Object[] children;

		public BTreeNode() {
			numNodes += 1;
			
			this.leaf = true;
			this.n = 0;

			this.keys = new Object[degree * 2 - 1];
			this.children = new Object[degree * 2];

			this.key = getNewOffset();
		}

		public BTreeNode(Long key) throws IOException {
			this.key = key;
			
			this.keys = new Object[degree * 2 - 1];
			this.children = new Object[degree * 2];
			
			this.load();
		}

		public TreeObject<T> search(T key) {
			
			int i = this.n - 1;
			while (i >= 0 && key.compareTo(this.getKey(i).getKey()) < 0) {
				i--;
			}

			// If the key was found
			if (i >= 0 && key.compareTo(this.getKey(i).getKey()) == 0) {
				return this.getKey(i);

				// If there are more children to search
			} else if (!this.isLeaf()) {
				return this.getChild(i + 1).search(key);
			}
			return null;
		}

		public void setChild(int index, BTreeNode<T> node) {
			children[index] = node;
		}

		@SuppressWarnings("unchecked")
		public BTreeNode<T> getChild(int index) {
			return (BTreeNode<T>) children[index];
		}

		@SuppressWarnings("unchecked")
		public BTreeNode<T> removeChild(int index) {
			BTreeNode<T> c = (BTreeNode<T>) children[index];
			children[index] = null;
			return c;
		}

		public void splitChild(int index) throws IOException {
			BTreeNode<T> z = new BTreeNode<T>();
			BTreeNode<T> y = getChild(index);

			z.isLeaf(y.isLeaf());
			z.n(degree - 1);

			for (int j = 0; j <= degree - 2; j++) {
				z.setKey(j, y.removeKey(j + degree));
			}

			if (!y.isLeaf()) {
				for (int j = 0; j <= degree - 1; j++) {
					z.setChild(j, y.removeChild(j + degree));
				}
			}

			y.n(degree - 1);

			for (int j = this.n; j >= index + 1; j--) {
				this.setChild(j + 1, this.removeChild(j));
			}
			this.setChild(index + 1, z);

			for (int j = this.n() - 1; j >= index; j--) {
				this.setKey(j + 1, this.removeKey(j));
			}
			this.setKey(index, y.removeKey(degree - 1));
			this.n = this.n + 1;

			y.save();
			z.save();
			this.save();
		}

		public void setKey(int index, TreeObject<T> obj) {
			keys[index] = obj;
		}

		@SuppressWarnings("unchecked")
		public TreeObject<T> getKey(int index) {
			return (TreeObject<T>) keys[index];
		}

		@SuppressWarnings("unchecked")
		public TreeObject<T> removeKey(int index) {
			TreeObject<T> k = (TreeObject<T>) keys[index];
			keys[index] = null;
			return k;
		}

		/**
		 * Inserted key assuming node is not full.
		 * 
		 * @param key
		 *          key to insert
		 * @throws IOException 
		 */
		public void insert(T key) throws IOException {
			int i = this.n - 1;
			if (this.isLeaf()) {
				while (i >= 0 && (this.getKey(i) != null)
						&& key.compareTo(this.getKey(i).getKey()) < 0) {
					this.setKey(i + 1, this.removeKey(i));
					i--;
				}

				this.setKey(i + 1, new TreeObject<T>(key));
				this.n += 1;

				this.save();
			} else {
				while (i >= 0 && key.compareTo(this.getKey(i).getKey()) < 0) {
					i--;
				}
				i++;
				this.getChild(i).load();
				if (this.getChild(i).isFull()) {
					this.splitChild(i);
					if (key.compareTo(this.getKey(i).getKey()) > 0) {
						i++;
					}
				}
				this.getChild(i).insert(key);
			}
		}

		/**
		 * Loads node from disk.
		 * @throws IOException 
		 */
		private void load() throws IOException {
			raf.seek(key + 8);
			// Get Number of Nodes
			this.n = raf.readInt();
			// Get Node leaf value
			this.leaf = raf.readBoolean();

			// Get each node
			for (int i = 0; i < this.n; i++){
				TreeObject<T> t_obj = new TreeObject<T>();
				t_obj.readObject(raf);
				setKey(i,t_obj);
			}
			
			if (!this.isFull()) {
				int serialLength = getKey(0).serialLength();
				long pos = raf.getFilePointer();
				pos += ((2*degree - 1) - this.n) * serialLength;
			}
			
			// Get each child
			
		}

		/**
		 * Saves node to disk.
		 * @throws IOException 
		 */
		private void save() throws IOException {
			raf.seek(this.key);
			
			// Key/offset
			raf.writeLong(this.key);
			// Number of keys
			raf.writeInt(this.n);
			// If node is leaf
			raf.writeBoolean(this.leaf);
			
			// Write each key
			for (int i = 0; i < this.n; i++) {
				getKey(i).writeObject(raf);
			}
			
			// Leave space for unused key indices
			if (!this.isFull()) {
				int serialLength = getKey(0).serialLength();
				long pos = raf.getFilePointer();
				pos += ((2*degree - 1) - this.n) * serialLength;
				raf.seek(pos);
			}
			
			// Write each child
			if (!this.leaf) {
				for (int i = 0; i < this.n + 1; i++) {
					raf.writeLong(getChild(i).key);
				}
				
				if(!this.isFull()){
					long pos = raf.getFilePointer();
					pos += ((2*degree) - (this.n + 1)) * 8;
					raf.seek(pos-1);
					raf.write(0);
				}
			} else {
				long pos = raf.getFilePointer();
				pos += (2*degree) * 8;
				raf.seek(pos-1);
				raf.write(0);
			}
		}

		/**
		 * Returns number of contains objects
		 * 
		 * @return # of objects
		 */
		public int n() {
			return n;
		}

		public void n(int n) {
			this.n = n;
		}

		/**
		 * Returns true if node is full, false otherwise.
		 * 
		 * @return
		 */
		public boolean isFull() {
			return this.n == (2 * degree) - 1;
		}

		public boolean isLeaf() {
			return this.leaf;
		}

		public void isLeaf(boolean leaf) {
			this.leaf = leaf;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Object obj : keys) {
				if (obj != null) {
					sb.append(obj.toString());
					sb.append(", ");
				}
			}
			int l = sb.length();
			sb.delete(l - 2, l);
			sb.append("]");
			return sb.toString();
		}

	}

	@SuppressWarnings("serial")
	private class TreeObject<T extends Comparable<T> & Serializable> implements Serializable {
		private int frequency;
		private T key;

		public TreeObject() {
			this.key = (T) new Object();
			this.frequency = 0;
		}
		
		public TreeObject(T key) {
			this.key = key; 
			this.frequency = 1;
		}

		public T getKey() {
			return this.key;
		}

		public int frequency() {
			return this.frequency;
		}

		public void incrementFrequency() {
			this.frequency++;
		}

		public String toString() {
			String str = "";
			str = key.toString() + " (" + frequency + ")";
			return str;
		}
		
		@Override
		public void writeObject(RandomAccessFile raf) throws IOException {
			this.key.writeObject(raf);
			raf.writeInt(this.frequency);
		}

		@Override
		public void readObject(RandomAccessFile raf) throws IOException {
			this.key.readObject(raf);
			this.frequency = raf.readInt();
		}

		@Override
		public final int serialLength() {
			// TODO Auto-generated method stub
			return this.key.serialLength() + 1;
		}
	}
	
	/**
	 * Contains key (byte offset) of a child node.
	 */
	private class NodeKey{
		private Long key;
		
		public NodeKey(Long key){
			this.key = key;
		}
		
		/**
		 * Loads node from disk.
		 * @throws IOException 
		 */
		public BTreeNode<T> load() throws IOException {
			return new BTreeNode<T>(key);
		}
	}

}
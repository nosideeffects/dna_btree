import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BTree<T extends Comparable<T> & Serializable> {

	private final static String EXTENSION = ".btree.data.";
	private final static int BLOCK_SIZE = 4096;
	
	private int degree;
	private int nodeSize;
	private int numNodes;
	private BTreeNode<T> root;
	
	private Factory<T> factory;
	private FileChannel fc;

	public BTree(int degree, String name, int sequenceLength, Factory<T> factory) throws IOException {

		this.factory = factory;
		this.degree = degree;
		this.numNodes = 0;

		if (degree < 0) {

			System.err.println("Invalid degree. Must be a positive integer.");
			System.exit(3);
		}
		
		if (degree == 0) {
			// Select Optimal Degree
			this.degree = 97;
		}
		
		// Ensure BRAND NEW file is created.
		File btreefile = new File(name + EXTENSION + sequenceLength + "." + this.degree);
		btreefile.delete();
		
		// Allow random access
		this.fc = (new RandomAccessFile(btreefile,"rw")).getChannel();
		
		this.nodeSize = getNodeByteLength();
		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.clear();

		bb.putInt(this.degree);
		bb.putLong(16);
		bb.putInt(this.numNodes);
		
		bb.flip();
		
		this.fc.write(bb);
		this.root = new BTreeNode<T>();
	}

	public BTree(String bTreeFile, Factory<T> factory) throws IOException {
		this.factory = factory;
		File file = new File(bTreeFile);
		this.fc = (new RandomAccessFile(file, "rw")).getChannel();
		
		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.clear();
		this.fc.read(bb, 0);
		bb.flip();
		
		this.degree = bb.getInt();
		Long rootOffset = bb.getLong();
		this.numNodes = bb.getInt();
		
		this.nodeSize = getNodeByteLength();
		this.root = new BTreeNode(rootOffset);
		root.load();
	}
	
	private int getNodeByteLength(){
		TreeObject<T> obj = new TreeObject<T>();
		return 13 + (degree*2 - 1)*obj.serialLength() + (degree*2)*8;
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
				s.insert(key);
			} else {
				r.insert(key);
			}
		}
	}

	public String search(T key) throws IOException {
		
		TreeObject<T> t_obj = findKeyObject(key);

		if (t_obj != null) {
			return key.toString() + ": " + t_obj.frequency();
		}

		return key.toString() + ": 0";
	}

	private TreeObject<T> findKeyObject(T key) throws IOException {
		return root.search(key);
	}

	private String build() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(root.toString());
		sb.append("(head)--");
		sb.append(root.key);
		sb.append("\n");
		build(sb, root, 0, "head", 0, true);
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private void build(StringBuilder sb, BTreeNode<T> node, int height,
			String prevLevel, int child, boolean first) throws IOException {
		
		
		String thisLevel = "";
		for (int i = 0; i < height; i++) {
			sb.append("  ");
		}
		if (!first) {
			node.load();
			sb.append("--> ");
			sb.append(node.toString());
			sb.append("(");
			sb.append(prevLevel);
			sb.append(".c" );
			sb.append(child);
			sb.append("--");
			sb.append(node.key);
			sb.append(")\n");
			thisLevel = prevLevel + ".c" + child;
			first = false;
		} else {
			thisLevel = prevLevel;
		}
		int i = 1;
		if (!node.isLeaf()) {
			for (int j = 0; j < node.n + 1; j++) {
				BTreeNode<T> obj = node.getChild(j);
				if (obj != null) {
					build(sb, obj, height + 1, thisLevel, i, false);
					i++;
				}
			}
		}
	}
	
	public long getNewOffset(){
		return 16 + nodeSize*numNodes;
	}
	
	public String toString() {
		try {
			return build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void closeFile() throws IOException {
		root.save();
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.clear();
		bb.putLong(root.key);
		bb.flip();
		this.fc.write(bb, 4);
		this.fc.close();
	}

	@SuppressWarnings("hiding")
	private class BTreeNode<T extends Comparable<T> & Serializable> {
		private boolean loaded;
		private boolean leaf;
		private long key = 0L;
		/**
		 * Number of keys.
		 */
		private int n = 0;
		private Object[] keys;
		private Object[] children;

		public BTreeNode() {
			this.key = getNewOffset();
			numNodes += 1;
			
			this.leaf = true;
			this.loaded = true;
			this.n = 0;

			this.keys = new Object[degree * 2 - 1];
			this.children = new Object[degree * 2];
		}

		public BTreeNode(long k) throws IOException {
			this.loaded = false;
			this.key = k;
			
			this.keys = null;
			this.children = null;
		}

		public TreeObject<T> search(T key) throws IOException {

			int i = this.n - 1;
			while (i >= 0 && key.compareTo(this.getKey(i).getKey()) < 0) {
				i--;
			}

			// If the key was found
			if (i >= 0 && key.compareTo(this.getKey(i).getKey()) == 0) {
				return this.getKey(i);

				// If there are more children to search
			} else if (!this.isLeaf()) {
				this.getChild(i + 1).load();
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
				//this.getChild(i).load();
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
			if (!this.loaded) {
				this.keys = new Object[degree*2 - 1];
				this.children = new Object[degree*2];
				
				ByteBuffer bb = ByteBuffer.allocate(nodeSize);
				bb.clear();
				fc.read(bb, this.key);
				bb.flip();
				
				this.key = bb.getLong();
				this.n = bb.getInt();
				// Get Node leaf value
				this.leaf = (bb.get()==0)?false:true;
	
				// Get each node
				for (int i = 0; i < this.n; i++){
					TreeObject<T> t_obj = new TreeObject<T>();
					t_obj.readObject(bb);
					setKey(i,t_obj);
				}
				
				// Get each child
				if (!this.leaf) {
					for (int i = 0; i < this.n + 1; i++) {
						this.children[i] = new BTreeNode<T>(bb.getLong()); 
					}
				}
				
				this.loaded = true;
			}
		}
		
		private void unload() {
			if (this.loaded) {
				this.keys = null;
				this.children = null;
				
				this.loaded = false;
			}
		}

		/**
		 * Saves node to disk.
		 * @throws IOException 
		 */
		private void save() throws IOException {
			ByteBuffer bb = ByteBuffer.allocate(nodeSize);
			bb.clear();
			int num = nodeSize;
			
			// Key/offset
			bb.putLong(this.key);
			// Number of keys
			bb.putInt(this.n);
			// If node is leaf
			bb.put((byte) ((this.leaf)?1:0));
			
			// Write each key
			for (int i = 0; i < this.n; i++) {
				getKey(i).writeObject(bb);
			}
			
			// Write each child
			if (!this.leaf) {
				for (int i = 0; i < this.n + 1; i++) {
					bb.putLong(getChild(i).key);
				}
			}
			bb.flip();
			fc.write(bb,this.key);
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

		@SuppressWarnings("unchecked")
		public TreeObject() {
			this.key = (T) factory.newInstance();
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
		public void writeObject(ByteBuffer bb) throws IOException {
			this.key.writeObject(bb);
			bb.putInt(this.frequency);
		}

		@Override
		public void readObject(ByteBuffer bb) throws IOException {
			this.key.readObject(bb);
			this.frequency = bb.getInt();
		}

		@Override
		public final int serialLength() {
			// TODO Auto-generated method stub
			return this.key.serialLength() + 4;
		}
	}

}
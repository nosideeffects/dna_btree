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

	private Cache<BTreeNode> cache;
	private boolean cached;

	/**
	 * Creates a BTree with specified degree, name and sequenceLenth.
	 * 
	 * @param degree
	 *          - degree of the BTree
	 * @param name
	 *          - what the file will be named
	 * @param sequenceLength
	 *          - length of the sequences generated
	 * @param factory
	 * @throws IOException
	 */
	public BTree(int degree, String name, int sequenceLength, Factory<T> factory)
			throws IOException {

		this.factory = factory;
		this.degree = degree;
		this.numNodes = 0;
		this.cached = false;

		if (degree < 0) {

			System.err.println("Invalid degree. Must be a positive integer.");
			System.exit(3);
		}

		if (degree == 0) {
			// Select Optimal Degree
			this.degree = 97;
		}

		// Ensure BRAND NEW file is created.
		File btreefile = new File(name + EXTENSION + sequenceLength + "."
				+ this.degree);
		btreefile.delete();

		// Allow random access
		this.fc = (new RandomAccessFile(btreefile, "rw")).getChannel();

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

	/**
	 * Creates a BTree with specified degree, name and sequenceLenth that uses a
	 * cashe.
	 * 
	 * @param degree
	 *          - degree of the BTree
	 * @param name
	 *          - what the file will be named
	 * @param sequenceLength
	 *          - length of the sequences generated
	 * @param factory
	 * @param casheSize
	 *          - sets the allocated memory for the cache
	 * @throws IOException
	 */
	public BTree(int degree, String name, int sequenceLength, Factory<T> factory,
			int cacheSize) throws IOException {
		this(degree, name, sequenceLength, factory);
		if (cacheSize > 1) {
			this.cached = true;
			this.cache = new Cache<BTreeNode>(cacheSize);
		}
	}

	/**
	 * Recreates a btree from a binary file.
	 * 
	 * @param bTreeFile
	 *          - name of file btree is stored in
	 * @param factory
	 * @throws IOException
	 */
	public BTree(String bTreeFile, Factory<T> factory) throws IOException {
		this.factory = factory;
		this.cached = false;
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
		this.root = loadNode(rootOffset);
	}

	/**
	 * Recreates a btree from a binary file with a cache.
	 * 
	 * @param bTreeFile
	 *          - name of file btree is stored in
	 * @param factory
	 * @param cacheSize
	 *          - sets the allocated memory for the cache
	 * @throws IOException
	 */
	public BTree(String bTreeFile, Factory<T> factory, int cacheSize)
			throws IOException {
		this(bTreeFile, factory);

		if (cacheSize > 1) {
			this.cached = true;
			this.cache = new Cache<BTreeNode>(cacheSize);
			this.cache.addObject(root);
		}
	}

	/**
	 * Gets the length of the byte that represents a node
	 * 
	 * @return - length of the node byte
	 */
	private int getNodeByteLength() {
		TreeObject<T> obj = new TreeObject<T>();
		return 13 + (degree * 2 - 1) * obj.serialLength() + (degree * 2) * 8;
	}

	/**
	 * Inserts a key into the btree
	 * 
	 * @param key
	 *          - the key for the new node
	 * @throws IOException
	 */
	public void insert(T key) throws IOException {
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

	/**
	 * Searches btree for node with a specific key
	 * 
	 * @param key
	 *          - the key of the requested node
	 * @return - a string of the key and its frequency
	 * @throws IOException
	 */
	public String search(T key) throws IOException {

		TreeObject<T> t_obj = findKeyObject(key);

		if (t_obj != null) {
			return key.toString() + ": " + t_obj.frequency();
		}

		return ""; // key.toString() + ": 0";
	}

	/**
	 * Finds the TreeObject with specified key
	 * 
	 * @param key
	 *          - key of the desired Tree Object
	 * @return - the TreeObject
	 * @throws IOException
	 */
	private TreeObject<T> findKeyObject(T key) throws IOException {
		return root.search(key);
	}

	/**
	 * Begins to build a string representation of the the btree
	 * 
	 * @return - the btree as a string
	 * @throws IOException
	 */
	private String build() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(root.toString());
		sb.append("(head)--");
		sb.append(root.key);
		sb.append("\n");
		build(sb, root, 0, "head", 0, true);
		return sb.toString();
	}

	/**
	 * Begins to build a string representation of the btree when traversed
	 * inorder.
	 * 
	 * @return - the btree as a string
	 * @throws IOException
	 */
	public String inorderBuild() throws IOException {
		StringBuffer sb = new StringBuffer();
		inorderBuild(sb, root);
		return sb.toString();
	}

	/**
	 * Builds a string representation of a btree when traversed inorder.
	 * 
	 * @param sb
	 *          - the string buffer used to build the string
	 * @param node
	 *          - the node to build next
	 * @throws IOException
	 */
	private void inorderBuild(StringBuffer sb, BTreeNode<T> node)
			throws IOException {
		if (node.isLeaf()) {
			for (int i = 0; i < node.n; i++) {
				TreeObject<T> key = node.getKey(i);
				sb.append(key.frequency());
				sb.append(' ');
				sb.append(key.getKey().toString());
				sb.append('\n');
			}
		} else {
			for (int i = 0; i < node.n; i++) {
				inorderBuild(sb, node.getChild(i));
				TreeObject<T> key = node.getKey(i);
				sb.append(key.frequency());
				sb.append(' ');
				sb.append(key.getKey().toString());
				sb.append('\n');
			}
			inorderBuild(sb, node.getChild(node.n));
		}
	}

	/**
	 * Builds a string representation of the btree.
	 * 
	 * @param sb
	 *          - stringbuffer used to build the string
	 * @param node
	 *          - the node to build next
	 * @param height
	 *          - height of current node
	 * @param prevLevel
	 *          - the name of the previous level
	 * @param child
	 *          - current child
	 * @param first
	 *          - determines if it is the first call in order to skip printing the
	 *          head node
	 * @throws IOException
	 */
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
			sb.append(".c");
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

	/**
	 * Loads the node with key k into memory.
	 * 
	 * @param k
	 *          - key of desired node
	 * @return - the node that is loaded into memory
	 * @throws IOException
	 */
	private BTreeNode<T> loadNode(Long k) throws IOException {
		BTreeNode<T> node = null;

		if (cached) {
			node = cache.getByKey(k);

			if (node == null) {
				node = new BTreeNode<T>(k);
				node.load();

				cache.addObject(node);
			}
		} else {
			node = new BTreeNode<T>(k);
			node.load();
		}

		return node;
	}

	/**
	 * Gets the new offset in binary file
	 * 
	 * @return - the new offset
	 */
	private long getNewOffset() {
		return 16 + nodeSize * numNodes;
	}

	public String toString() {
		try {
			return build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Closes the binary file.
	 * 
	 * @throws IOException
	 */
	public void closeFile() throws IOException {
		root.save();
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.clear();
		bb.putLong(root.key);
		bb.flip();
		this.fc.write(bb, 4);
		this.fc.close();
	}

	/**
	 * The class for the internal node of the btree
	 * 
	 * @param <T>
	 */
	@SuppressWarnings("hiding")
	private class BTreeNode<T extends Comparable<T> & Serializable> implements
			Key {
		private boolean leaf;
		private Long key = 0L;
		/**
		 * Number of keys.
		 */
		private int n = 0;
		private Object[] keys;
		private Object[] children;

		/**
		 * Constructor for BTreeNode
		 */
		public BTreeNode() {
			this.key = getNewOffset();
			numNodes += 1;

			this.leaf = true;
			this.n = 0;

			this.keys = new Object[degree * 2 - 1];
			this.children = new Object[degree * 2];

			if (cached) {
				cache.addObject(this);
			}
		}

		/**
		 * Constructor for BTreeNode with specified key
		 * 
		 * @param k
		 *          - key for created node
		 * @throws IOException
		 */
		public BTreeNode(long k) throws IOException {
			this.key = k;

			this.keys = null;
			this.children = null;
		}

		/**
		 * Internal search of BTreeNode to find TreeObject with specified key
		 * 
		 * @param key
		 *          - desired key
		 * @return - key if found
		 * @return - null if not
		 * @throws IOException
		 */
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
				return this.getChild(i + 1).search(key);
			}
			return null;
		}

		public void setChild(int index, BTreeNode<T> node) {
			children[index] = node.key();
		}

		@SuppressWarnings("unchecked")
		public BTreeNode<T> getChild(int index) throws IOException {
			return (BTreeNode<T>) loadNode((Long) children[index]);
		}

		public Long getChildKey(int index) {
			return (Long) children[index];
		}

		public void setChildKey(int index, Long k) {
			children[index] = k;
		}

		@SuppressWarnings("unchecked")
		public Long removeChildKey(int index) {
			Long k = (Long) children[index];
			children[index] = null;
			return k;
		}

		/**
		 * Splits a child in the btree.
		 * 
		 * @param index
		 *          - index of child
		 * @throws IOException
		 */
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
					z.setChildKey(j, y.removeChildKey(j + degree));
				}
			}

			y.n(degree - 1);

			for (int j = this.n; j >= index + 1; j--) {
				this.setChildKey(j + 1, this.removeChildKey(j));
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

		public boolean hasKey(T key) {
			for (int i = n - 1; i >= 0; i--) {
				if (key.compareTo(this.getKey(i).getKey()) == 0) {
					return true;
				}
			}

			return false;
		}

		@SuppressWarnings("unchecked")
		public TreeObject<T> getKey(int index) {
			return (TreeObject<T>) keys[index];
		}

		public Object[] getKeys() {
			return keys;
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
					i--;
				}

				if (i >= 0 && key.compareTo(this.getKey(i).getKey()) == 0) {
					this.getKey(i).incrementFrequency();
				} else {
					for (int j = n - 1; j > i; j--) {
						this.setKey(j + 1, this.removeKey(j));
					}
					this.setKey(i + 1, new TreeObject<T>(key));
					this.n += 1;
				}

				this.save();
			} else {
				while (i >= 0 && key.compareTo(this.getKey(i).getKey()) < 0) {
					i--;
				}

				if (i >= 0 && key.compareTo(this.getKey(i).getKey()) == 0) {
					this.getKey(i).incrementFrequency();
					this.save();
					return;
				}

				i++;

				BTreeNode<T> ci = this.getChild(i);
				if (!ci.hasKey(key) && ci.isFull()) {
					this.splitChild(i);
					if (key.compareTo(this.getKey(i).getKey()) > 0) {
						ci = this.getChild(++i);
					}
				}
				ci.insert(key);
			}
		}

		/**
		 * Loads node from disk.
		 * 
		 * @throws IOException
		 */
		private void load() throws IOException {
			this.keys = new Object[degree * 2 - 1];
			this.children = new Object[degree * 2];

			ByteBuffer bb = ByteBuffer.allocate(nodeSize);
			bb.clear();
			fc.read(bb, this.key);
			bb.flip();

			this.key = bb.getLong();
			this.n = bb.getInt();
			// Get Node leaf value
			this.leaf = (bb.get() == 0) ? false : true;

			// Get each node
			for (int i = 0; i < this.n; i++) {
				TreeObject<T> t_obj = new TreeObject<T>();
				t_obj.readObject(bb);
				setKey(i, t_obj);
			}

			// Get each child
			if (!this.leaf) {
				for (int i = 0; i < this.n + 1; i++) {
					this.children[i] = bb.getLong();
				}
			}
		}

		/**
		 * Saves node to disk.
		 * 
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
			bb.put((byte) ((this.leaf) ? 1 : 0));

			// Write each key
			for (int i = 0; i < this.n; i++) {
				getKey(i).writeObject(bb);
			}

			// Write each child
			if (!this.leaf) {
				for (int i = 0; i < this.n + 1; i++) {
					bb.putLong((Long) children[i]);
				}
			}
			bb.flip();
			fc.write(bb, this.key);
		}

		/**
		 * Returns number of contains objects
		 * 
		 * @return # of objects
		 */
		public int n() {
			return n;
		}

		/**
		 * Sets nuber of contained objects
		 * 
		 * @param n
		 *          # of objects
		 */
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

		@Override
		public Long key() {
			return this.key;
		}

	}

	@SuppressWarnings("serial")
	private class TreeObject<T extends Comparable<T> & Serializable> implements
			Serializable {
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
			return this.key.serialLength() + 4;
		}
	}

}
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class BTree<T extends Comparable<T> & Serializable> {

	private final String BTreeFileExten = ".btree.data.k.t";
	private int degree;
	private BTreeNode<T> root;
	private final static int BLOCK_SIZE = 4096;
	private StringBuffer sb = new StringBuffer();
	private static ArrayList<byte[]> bytes = new ArrayList<byte[]>();

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

			// @TODO Do something
			/*
			
			*/

			dis.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
	}

	public void insert(T key) {

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

	public String toString() {
		return build();
	}
	
	public void write() throws IOException {
		
		byte[] byteArray = new byte[BLOCK_SIZE];
		
		int j = 0;
		for(int i = 0; i < root.keys.length; i++) {
			
			if (root.keys[i] != null) {
				
				for(byte b: ((TreeObject<T>) root.keys[i]).serialize()) {
					
					byteArray[j++] = b;
				}
			}
		}
		
		bytes.add(byteArray);
		
		for(int i = 0; i < root.children.length; i++) {
			
			if (root.children[i] != null) {
				
				write((BTreeNode<T>) root.children[i]);
			}
		}
		
		FileOutputStream fos = new FileOutputStream("treefile");
		for (byte[] b: bytes) {
			fos.write(b);
		}
		fos.close();
	}
	
	private void write(BTreeNode<T> node) throws IOException {
		
		int j = 0;
		byte[] byteArray = new byte[BLOCK_SIZE];
		for(int i = 0; i < node.keys.length; i++) {
			
			if (node.keys[i] != null) {
				for(byte b: ((TreeObject<T>) node.keys[i]).serialize()) {
					
					byteArray[j++] = b;
				}
			}
		}
		
		bytes.add(byteArray);

		for (int i = 0; i < node.children.length; i++) {
			
			if (node.children[i] != null) {
				
				write((BTreeNode<T>) node.children[i]);
			}
		}
	}
	
	private byte[] serialize(Object obj) throws IOException {
		
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);
		
		o.writeObject(obj);
		o.close();
		
		return b.toByteArray();
	}
	
	public byte[] read() {
		
		return null;
	}
	
	private T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		
		ByteArrayInputStream b = new ByteArrayInputStream(bytes);
		ObjectInputStream o = new ObjectInputStream(b);
		
		T obj = (T) o.readObject();
		
		return obj;
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
			this.leaf = true;

			this.n = 0;

			this.keys = new Object[degree * 2 - 1];
			this.children = new Object[degree * 2];

			this.key = 0;
		}

		public BTreeNode(Long key) {
			// TODO Read from disk
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

		public void splitChild(int index) {
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
		 */
		public void insert(T key) {
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
		 */
		private void load() {
			// TODO Auto-generated method stub

		}

		/**
		 * Saves node to disk.
		 */
		private void save() {
			// TODO Auto-generated method stub

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
		
		public byte[] serialize() throws IOException {
			
			//ByteArrayOutputStream b = new ByteArrayOutputStream();
			//ObjectOutputStream o = new ObjectOutputStream(b);
			
			//o.writeObject(this.key);
			byte[] frequency = ByteBuffer.allocate(4).putInt(this.frequency).array();
			byte[] key = ((Sequence) this.key).serialize();
			
			byte[] object = new byte[frequency.length + key.length];
			for (int i = 0; i < frequency.length; i++) {
				
				object[i] = frequency[i];
			}
			
			for (int i = 0; i < key.length; i++) {
				
				object[i + frequency.length] = key[i];
			}
			
			return object;
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
		 */
		public BTreeNode<T> load() {
			// TODO Auto-generated method stub
			return new BTreeNode<T>(key);
		}
	}

}
import java.io.*;

public class BTree {
	
	private int degree, sequenceLength;

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
}
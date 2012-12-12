import java.io.*;

/*
 * Notes: Error codes
 * 0: all good
 * 1: improper command format
 * 2: io exception
 * 3: BTree: invalid degree
 * 4: BTree: invalid sequence length
 * 404: file not found
 */

public class GeneBankCreateBTree {

	public static void main(String[] args) {
		
		int degree = 0, debugLevel = 0, sequenceLength = 0;
		String gbkFile = "";
		
		// Get parameters
		try {
			
			degree = Integer.parseInt(args[0]);
			gbkFile = args[1];
			sequenceLength = Integer.parseInt(args[2]);
			
			if (sequenceLength < 1 || sequenceLength > 31) {
				
				System.err.println("Invalid sequence length. Muse be an integer between 1 and 31 (inclusive).");
				System.exit(4);
			}
			
			// If exists, set debug level
			if (args.length > 3) {
				debugLevel = Integer.parseInt(args[3]);
			}
		} catch (IndexOutOfBoundsException e) {
			
			System.err.println("Improper command format: GeneBankCreateBTree <debree> <gbk file> <sequence length> [<debug level>]");
			System.exit(1);
		}
		
		// Create empty BTree, with degree and sequence length, then read gbkFile
		try {
			
			// Create empty BTree
			BTree<Sequence> btree = new BTree<Sequence>(degree);
			
			// Read gbkFile
			FileInputStream fis = new FileInputStream(gbkFile);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			String line = "", pattern ="[\\s\\d]";
			
			while ((line = br.readLine()) != null) {
				
				line = line.replaceAll(pattern, "");
				
				//@TODO Run static sequence method
				//Sequence s = new Sequence();
				//btree.insert(s);
			}
			
			dis.close();
		} catch (FileNotFoundException e) {
			
			System.err.println("GBK File not found. Please check your filename and make sure the GBK file is in the 'data' folder.");
			if (debugLevel > 1) {
				
				e.printStackTrace();
			}
			
			System.exit(404);
		} catch (IOException e) {
			
			System.err.println("IO Exception");
			if (debugLevel > 1) {
				
				e.printStackTrace();
			}
			
			System.exit(404);
		}
	}
}
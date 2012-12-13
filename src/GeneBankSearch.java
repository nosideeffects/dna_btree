import java.io.*;
import java.util.ArrayList;

public class GeneBankSearch {

	public static void main(String[] args) {
		
		BTree btree = null;
		String bTreeFile = "", queryFile = "";
		ArrayList<Sequence> queries = new ArrayList<Sequence>();
		int debugLevel = 0, sequenceLength = 0;
		
		// Get parameters
		try {
			
			bTreeFile = args[0];
			queryFile = args[1];
			
			// If exists, set debug level
			if (args.length > 2) {
				
				debugLevel = Integer.parseInt(args[2]);
			}
		}
		catch(IndexOutOfBoundsException e) {
			
			System.err.println("Improper command format: GeneBankSearch <btree file> <query file> [<debug level>]");
			System.exit(1);
		}
		
		// Read bTreeFile, create BTree
		try {
			FileInputStream fis = new FileInputStream(bTreeFile);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			
			//@TODO Do something
			/*
			btree = new BTree(btreeFile);
			*/
			
			dis.close();
		} catch (FileNotFoundException e) {
			
			System.err.println("BTree file not found. Please check your filename and make sure the BTree file is in the 'data' folder.");
			if (debugLevel > 0) {
				
				e.printStackTrace();
			}
			
			System.exit(404);
		} catch (IOException e) {

			//@TODO Set a proper error message
			System.err.println("IO Exception.");
			if (debugLevel > 0) {
				
				e.printStackTrace();
			}
			
			System.exit(2);
		}
		
		// Read queryFile, search BTree for each sequence
		try {
			
			FileInputStream fis = new FileInputStream(queryFile);
			DataInputStream dis = new DataInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(dis));
			String str = "";
			ArrayList<String> results = new ArrayList<String>();
			
			//@TODO Read sequences and search, print/store results
			while ((str = br.readLine()) != null) {
				
				results.add(btree.search(new Sequence(str, str.length())).toString());
			}
			
			for (String result: results) {
			
				System.out.println(result);
			}
			
			
			dis.close();
		} catch (FileNotFoundException e) {
			
			System.err.println("Query file not found. Please check your filename and the file's location.");
			if (debugLevel > 0) {
				
				e.printStackTrace();
			}
			
			System.exit(404);
		} catch (IOException e) {
			
			//@TODO Set a proper error message
			System.err.println("IO Exception");
			if (debugLevel > 0) {
				
				e.printStackTrace();
			}
			
			System.exit(2);
		}
	}
}
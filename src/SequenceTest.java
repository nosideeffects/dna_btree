import java.io.IOException;


public class SequenceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String str = "test3.gbk.btree.data.7.97";
		
		try {
			BTree<Sequence> btree = new BTree<Sequence>(str ,new Sequence.SequenceFactory());
			
			System.out.println(btree.toString());
			btree.closeFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

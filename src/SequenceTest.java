import java.io.IOException;


public class SequenceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String str = "test1.gbk.btree.data.k.t";
		
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


public class SequenceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String str = "acg";
		Sequence s = new Sequence(str, 3);
		
		/* Test string converted to binary correct
		System.out.println(str);
		System.out.println(s.val());
		System.out.println(Long.toBinaryString(s.val()));
		System.out.println(Long.toBinaryString(s.val()).length());
		*/
		
		/* Test parseSequences
		Sequence[] sa = Sequence.parseSequences(str, 3);
		System.out.println("\nhere");
		for (Sequence s: sa) {
			
			System.out.println(s.val());
			System.out.println(Long.toBinaryString(s.val()));
			System.out.println(Sequence.toSequence(s));
		}*/
		
		System.out.println(s);
	}

}

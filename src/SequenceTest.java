
public class SequenceTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String str = "t    g c";
		Sequence s = new Sequence(str, 8);
		
		System.out.println(str);
		System.out.println(s.val());
		System.out.println(Long.toBinaryString(s.val()));
		System.out.println(Long.toBinaryString(s.val()).length());
	}

}

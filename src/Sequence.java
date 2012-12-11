/**
 * A Java Long disguised with a fancier name
 * and some useful methods to represent a
 * DNA sequence of length between 1 - 31.
 * @author Jacob Biggs
 *
 */
public class Sequence {
	private Long seq;
	
	/**
	 * Creates a new Sequence with the given
	 * Long
	 * @param seq
	 */
	public Sequence(Long seq){
		
	}
	

	public Sequence(String str){
		if (str.length() == 0 || str.length() > 31){
			throw new IllegalArgumentException("Sequence length was 0 or >31:\n" + str);
		}
	
	}
}
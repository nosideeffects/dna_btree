import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * A Java Long disguised with a fancier name
 * and some useful methods to represent a
 * DNA sequence of length between 1 - 31.
 * @author Jacob Biggs
 *
 */
public class Sequence implements Comparable<Sequence>, Serializable {
	private Long seq = 0L;
	private int length = 0;
	
	public Sequence(){
		
	}
	
	/**
	 * Creates a new Sequence with the given
	 * Long
	 * @param seq
	 */
	public Sequence(Long seq, int sequenceLength){
		this.seq = seq;
		this.length = sequenceLength;
	}
	
	
	public Sequence(String str, int sequenceLength){
		if (str.length() > 31 || str.length() < 1){
			throw new IllegalArgumentException("Input String length was 0 or >31:\n" + str);
		} else if (sequenceLength > 31 || sequenceLength < 1) {
			throw new IllegalArgumentException("sequenceLength must be between 1 and 31 (inclusive)");
		} else if (sequenceLength != str.length()) {
			throw new IllegalArgumentException("Input String length ("+str.length()+") was different from sequenceLength ("+sequenceLength+")");
		}
		
		this.length = sequenceLength;
		char c;
		
		for (int i = 0, p = str.length() - 1; p >= 0; i++, p--) {
			
			//System.out.println("i:" + i + " p:" + p);
			c = str.charAt(p);
			switch (c) {
			
				case ' ':
					i--;
					break;
					
				case 'A':
				case 'a':
					break;
					
				case 'T':
				case 't':
					seq += (long) 3 * (long) Math.pow(2, i*2);
					break;
					
				case 'C':
				case 'c':
					seq += (long) 1 * (long) Math.pow(2, i*2);
					break;
					
				case 'G':
				case 'g':
					seq += (long) 2 * (long) Math.pow(2, i*2);
					break;
			}
		}
	}

	public static Sequence[] parseSequences(String str, int sequenceLength){
		// 'str' must be void of any whitespace and numbers
		ArrayList<Sequence> al = new ArrayList<Sequence>();
		String s = "";
		
		for (int i = 0; i < str.length() - sequenceLength + 1; i++) {
			
			s = str.substring(i, i + sequenceLength);
			
			if (s.contains("n")) {
				
				i += s.indexOf('n');
				continue;
			}
			// System.out.println(s);
			
			al.add(new Sequence(s, sequenceLength));
		}
		
		return al.toArray(new Sequence[al.size()]);
	}
	
	public static String toSequence(Sequence s) {
		
		String str = Long.toBinaryString(s.val());
		String r = "";
		
		while (str.length() < s.length * 2) {
			
			str = "0" + str;
		}
		
		for (int i = 0; i < str.length(); i += 2) {
			
			switch (Integer.parseInt(str.substring(i, i+2))) {
				case 0:
					r += "a";
					break;
				case 1:
					r += "c";
					break;
				case 10:
					r += "g";
					break;
				case 11:
					r += "t";
					break;
			}
		}
		
		return r;
	}
	
	public int getSequenceLength() {
		
		return length;
	}
	
	public Long val() {
		
		return this.seq;
	}
	
	public String toString() {
		
		return Sequence.toSequence(this);
	}

	@Override
	public int compareTo(Sequence that) {
		if (this.val() > that.val()) {
			return 1;
		} else if(this.val().equals(that.val())) {
			return 0;
		}
		return -1;
	}
	
	public void writeObject(ObjectOutputStream oos) throws IOException {
		oos.write((byte) this.length);
		oos.writeLong(this.seq);
	}
	
	public void readObject(ObjectInputStream ois) throws IOException {
		this.length = (int) ois.readByte();
		this.seq = ois.readLong();
	}
	
	/**
	 * Returns data of Sequence as 9 bytes: <br />
	 * <code><1B>[sequence length] <8B>[sequence]</code>
	 * @return
	 */
	public byte[] serialize() {
		return ByteBuffer.allocate(9).put((byte) this.length).putLong(this.seq).array();
	}

}
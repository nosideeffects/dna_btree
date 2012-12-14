import java.io.IOException;
import java.io.RandomAccessFile;


public interface Serializable {
	public void writeObject(RandomAccessFile raf) throws IOException;
	public void readObject(RandomAccessFile raf) throws IOException;
	
	/**
	 * Returns length in bytes of serialized object.
	 * @return length
	 */
	public int serialLength();
}

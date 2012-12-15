import java.io.IOException;
import java.nio.ByteBuffer;


public interface Serializable {
	public void writeObject(ByteBuffer bb) throws IOException;
	public void readObject(ByteBuffer bb) throws IOException;
	
	/**
	 * Returns length in bytes of serialized object.
	 * @return length
	 */
	public int serialLength();
}

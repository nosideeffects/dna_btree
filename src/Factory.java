
public interface Factory<T> {
	public T newInstance();
	public T[] newArray(int size);
}

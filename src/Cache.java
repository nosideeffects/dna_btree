import java.util.Iterator;
import java.util.LinkedList;

/**
 * Cache designed for storing objects by key.
 * @author Jacob Biggs
 *
 */
public class Cache<E extends Key> {
	
	private int maxSize;
	private int size;
	
	private LinkedList<E> list;
	
	/**
	 * Creates a cache with a specified number of elements.
	 * @param maxSize - number of elements
	 */
	public Cache(int maxSize){
		this.size = 0;
		this.maxSize = maxSize;
		
		this.list = new LinkedList<E>();
	}
	
	public E getByKey(Long key) {
		E r_obj = null;
		int i = 0;
		for (E obj : list) {
			if (obj.key().equals(key)) {
				r_obj = list.remove(i);
				list.addFirst(r_obj);
				break;
			}
			i++;
		}
		return r_obj;
	}
	
	/**
	 * Adds object to the front of the cache, removing last element of cache
	 * if cache is full.
	 * @param obj - object to add
	 */
	public void addObject(E obj){
		list.addFirst(obj);
	
		if (size == maxSize) {
			list.removeLast();
		} else {
			size++;
		}
	}
	
	/**
	 * Removes the specified object from the cache.
	 * @param obj - object to remove
	 * @return true if removed, false otherwise
	 */
	public boolean removeObject(E obj){
		boolean removed = list.remove(obj);
		if (removed) this.size--;
		return removed;
	}
	
	/**
	 * Clears all objects from the cache and resets cache statistics.
	 */
	public void clearCache(){
		list.clear();
		size = 0;
	}

	public Iterator<E> iterator() {
		return this.list.iterator();
	}
	
	
}

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Generic Cache for storing a specified number of T objects and recording
 * the number of cache references and hits.
 * @author Jacob Biggs
 *
 * @param <T> type
 */
public class Cache<T> {
	
	private int maxSize;
	private int size;
	
	private int cacheHit;
	private int cacheRef;
	
	private LinkedList<T> list;
	
	/**
	 * Creates a cache with a specified number of elements.
	 * @param maxSize - number of elements
	 */
	public Cache(int maxSize){
		this.size = 0;
		this.maxSize = maxSize;
		
		this.cacheHit = 0;
		this.cacheRef = 0;
		
		this.list = new LinkedList<T>();
	}
	
	/**
	 * Gets an object from the cache if it exists and moves it to the front of
	 * the cache. Returns null otherwise.
	 * @param obj - object to get
	 * @return obj or null
	 */
	public T getObject(T obj){
		cacheRef++;
		
		if( list.contains(obj) ){
			cacheHit++;
			list.remove(obj);
			list.addFirst(obj);
			return obj;
		} else {
			addObject(obj);
		}
		return null;
	}
	
	/**
	 * Adds object to the front of the cache, removing last element of cache
	 * if cache is full.
	 * @param obj - object to add
	 */
	public void addObject(T obj){
		list.addFirst(obj);
		
		if (size == maxSize)
			list.removeLast();
		else
			size++;
	}
	
	/**
	 * Removes the specified object from the cache.
	 * @param obj - object to remove
	 * @return true if removed, false otherwise
	 */
	public boolean removeObject(T obj){
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
		
		cacheHit = 0;
		cacheRef = 0;
	}
	
	/**
	 * Returns number of times this cache was referenced looking for an object.
	 * @return number of references
	 */
	public int getReferenceCount(){
		return this.cacheRef;
	}
	
	/**
	 * Returns number of times a cache lookup was successful (getObject).
	 * @return number of hits
	 */
	public int getHitCount(){
		return this.cacheHit;
	}

	
	public Iterator<T> iterator() {
		return this.list.iterator();
	}
}

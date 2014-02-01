package at.workflow.webdesk.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * An alternative to apache-commons MultiMap (provides no type-safety) and
 * Spring MultiValueMap (requires calling add instead of put).
 * <p/>
 * This class provides static generic methods to manage e.g. a Map&lt;String,List&lt;String>>
 * where you want to value-aggregation instead of overwrite when calling put().
 * 
 * @author fritzberger 05.09.2013
 */
public class MultiMapUtil
{
	/**
	 * Adds the given value to a (non-unique) collection stored under given key.
	 * @param key the key where to store in given multiMap.
	 * @param value the value to aggregate under given key in given multiMap.
	 */
	public static <K,V> void put(K key, V value, Map<K,Collection<V>> multiMap) {
		put(key, value, multiMap, false);
	}

	/**
	 * Adds the given value to the collection stored under given key.
	 * @param key the key where to store in given multiMap.
	 * @param value the value to aggregate under given key in given multiMap.
	 * @param uniqueValueList when true, a HashSet will be used as value collection, else an ArrayList.
	 */
	public static <K,V> void put(K key, V value, Map<K,Collection<V>> multiMap, boolean uniqueValueList) {
		Collection<V> collection = multiMap.get(key);
		
		if (collection == null)	{
			collection = uniqueValueList ? new HashSet<V>() : new ArrayList<V>();
			multiMap.put(key, collection);
		}
		
		collection.add(value);
	}

}

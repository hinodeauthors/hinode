package hwanglab.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A MultiValuedHashMap associates each key with a set of values.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * @param <K>
 *            the type of keys.
 * @param <V>
 *            the type of values.
 */
public class MultiValuedHashMap<K, V> extends HashMap<K, Set<V>> {

	/**
	 * An automatically generated serial version ID.
	 */
	private static final long serialVersionUID = 8198293194500909080L;

	/**
	 * Adds the specified key and value.
	 * 
	 * @param k
	 *            the key to add.
	 * @param v
	 *            the value to add.
	 */
	public void add(K k, V v) {
		getSet(k).add(v);
	}

	/**
	 * Adds the specified key and value.
	 * 
	 * @param k
	 *            the key to add.
	 * @param v
	 *            the values to add.
	 */
	public void add(K k, Set<V> v) {
		getSet(k).addAll(v);
	}

	/**
	 * Removes the specified key and value.
	 * 
	 * @param k
	 *            the key to remove.
	 * @param v
	 *            the value to remove.
	 */

	/**
	 * Returns the set of values for the specified key.
	 * 
	 * @param k
	 *            a key.
	 * @return the set of values for the specified key.
	 */
	protected Set<V> getSet(K k) {
		Set<V> s = get(k);
		if (s == null) {
			s = new HashSet<V>();
			put(k, s);
		}
		return s;
	}

	/**
	 * Constructs from this MultiValuedHashMap a set of pairs each of which contains a key and a related set of values.
	 * 
	 * @return the constructed set of pairs each of which contains a key and a related set of values.
	 */
	public Set<Pair<K, Set<V>>> toPairs() {
		HashSet<Pair<K, Set<V>>> s = new HashSet<Pair<K, Set<V>>>();
		for (Map.Entry<K, Set<V>> e : entrySet()) {
			s.add(new Pair<K, Set<V>>(e.getKey(), e.getValue()));
		}
		return s;
	}
}

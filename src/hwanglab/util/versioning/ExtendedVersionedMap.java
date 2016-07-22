package hwanglab.util.versioning;

import java.util.Map;
import java.util.Set;

/**
 * The ExtendedVersionedMap class includes additional methods for VersionedMaps to be used as a component of a
 * HierarchicalVersionedMap.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * @param <K>
 *            the type of keys.
 * @param <V>
 *            the type of values.
 * @param <N>
 *            the type of version numbers.
 */
public abstract class ExtendedVersionedMap<K, V, N> extends VersionedMap<K, V, N> {

	/**
	 * Automatically generated serial version UID.
	 */
	private static final long serialVersionUID = 4206043237450598162L;

	/**
	 * Puts the specified key and value in all registered versions.
	 * 
	 * @param k
	 *            a key.
	 * @param v
	 *            a value.
	 */
	public abstract void put(K k, V v);

	/**
	 * Determines whether or not this ExtendedVersionedMap contains the specified version.
	 * 
	 * @param n
	 *            a version number.
	 * @return true if this ExtendedVersionedMap contains the specified version; false otherwise.
	 */
	public abstract boolean containsVersion(N n);

	/**
	 * Returns the smallest among the registered version numbers.
	 * 
	 * @return the smallest among the registered version numbers.
	 */
	public abstract N smallestVersionNumber();

	/**
	 * Removes the mapping for the specified key from all of the registered versions.
	 * 
	 * @param k
	 *            a key.
	 */
	public abstract void remove(K k);

	/**
	 * Determines whether or not all of the specified versions contain the specified key-value pair.
	 * 
	 * @param k
	 *            a key.
	 * @param v
	 *            a value.
	 * @param n
	 *            a set of version numbers.
	 * @return true if all of the specified versions contain the specified key-value pair; false otherwise.
	 */
	public abstract boolean contains(K k, V v, Set<N> n);

	/**
	 * Removes key-value pairs that belong to all of the registered versions.
	 * 
	 * @return a map consisting of key-value pairs that belonged to all of the registered versions.
	 */
	public abstract Map<K, V> clearIntersection();

	/**
	 * Moves a half of this ExtendedVersionedMap entries into a new ExtendedVersionedMap.
	 * 
	 * @return a new ExtendedVersionedMap that contains a half of this ExtendedVersionedMap entries.
	 */
	public abstract ExtendedVersionedMap<K, V, N> split();

}

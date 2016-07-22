package hwanglab.util.versioning;

import hwanglab.util.Pair;
import hwanglab.util.Triplet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A VersionedMap maintains a mapping from keys to values for each registered version.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * @param <K>
 *            the type of keys.
 * @param <V>
 *            the type of values.
 * @param <N>
 *            the type of version numbers.
 */
public abstract class VersionedMap<K, V, N> implements java.io.Serializable {

	/**
	 * Automatically generated serial version UID.
	 */
	private static final long serialVersionUID = 8659896636826213971L;

	/**
	 * Puts the specified key-value pair in the specified version.
	 * 
	 * @param k
	 *            a key.
	 * @param v
	 *            a value.
	 * @param n
	 *            a version number.
	 */
	public abstract void put(K k, V v, N n);

	/**
	 * Removes the mapping for the specified key from the specified version.
	 * 
	 * @param k
	 *            a key.
	 * @param n
	 *            a version number.
	 */
	public abstract V remove(K k, N n);

	/**
	 * Constructs a new version identical to the specified version.
	 * 
	 * @param n
	 *            the version number of the version to create.
	 * @param s
	 *            the version number of the source version.
	 * @throws DuplicateVersionException
	 *             if the version to create already exists.
	 */
	public abstract void clone(N n, N s) throws DuplicateVersionException;

	/**
	 * Removes the specified version.
	 * 
	 * @param n
	 *            a version number.
	 * @return the key-value pairs that belonged to the removed version.
	 */
	public abstract Map<K, V> removeVersion(N n);

	/**
	 * Returns the registered version numbers.
	 * 
	 * @return the registered version numbers.
	 */
	public abstract Set<N> versionNumbers();

	/**
	 * Returns a set of entries, each of which contains (1) a value associated with the specified key and one of the
	 * specified versions, as well as (2) the version numbers of the versions that contain that key-value pair.
	 * 
	 * @param k
	 *            a key.
	 * @param n
	 *            a set of version numbers.
	 * @return a set of entries, each of which contains (1) a value associated with the specified key and one of the
	 *         specified versions, as well as (2) the version numbers of the versions that contain that key-value pair.
	 */
	public abstract Collection<Pair<V, Set<N>>> get(K k, Set<N> n);

	/**
	 * Returns an Iterator over entries, each of which contains (1) a key-value pair contained in one of the specified
	 * versions, as well as (2) the version numbers of the versions that contain that key-value pair.
	 * 
	 * @param n
	 *            a set of version numbers.
	 * @return an Iterator over entries, each of which contains (1) a key-value pair contained in one of the specified
	 *         versions, as well as (2) the version numbers of the versions that contain that key-value pair.
	 */
	public abstract Iterator<Triplet<K, V, Set<N>>> get(Set<N> n);

	/**
	 * Returns a singleton that contains the specified version number.
	 * 
	 * @param n
	 *            a version number.
	 * @return a singleton that contains the specified version number.
	 */
	public Set<N> singleton(N n) {
		TreeSet<N> s = new TreeSet<N>();
		s.add(n);
		return s;
	}

	/**
	 * Returns the difference from the first set to the second set.
	 * 
	 * @param first
	 *            the first set.
	 * @param second
	 *            the second set.
	 * @return the difference from the first set to the second set.
	 */
	protected Set<N> difference(Set<N> first, Set<N> second) {
		TreeSet<N> s = new TreeSet<N>(first);
		s.removeAll(second);
		return s;
	}

}

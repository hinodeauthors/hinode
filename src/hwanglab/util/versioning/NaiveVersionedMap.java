package hwanglab.util.versioning;

import hwanglab.util.MergeIterator;
import hwanglab.util.MultiValuedHashMap;
import hwanglab.util.Pair;
import hwanglab.util.Triplet;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * A NaiveVersionedMap associates each version number with a key-value Map.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * @param <K>
 *            the type of keys.
 * @param <V>
 *            the type of values.
 * @param <N>
 *            the type of version numbers.
 */
public class NaiveVersionedMap<K, V, N> extends ExtendedVersionedMap<K, V, N> {

	/**
	 * Automatically generated serial version UID.
	 */
	private static final long serialVersionUID = -5145094180896818753L;

	/**
	 * A Map that associates each version number with a key-value Map.
	 */
	protected TreeMap<N, TreeMap<K, V>> version2map = new TreeMap<N, TreeMap<K, V>>();

	@Override
	public String toString() {
		return version2map.toString();
	}

	@Override
	public void put(K k, V v, N n) {
		map(n).put(k, v);
	}

	@Override
	public V remove(K k, N n) {
		TreeMap<K, V> map = map(n);
		if (k != null)
			return map.remove(k);
		return null;
	}

	@Override
	public void clone(N n, N s) throws DuplicateVersionException {
		TreeMap<K, V> map = version2map.get(n);
		if (map != null)
			throw new DuplicateVersionException(n);
		map = map(n);
		TreeMap<K, V> o = version2map.get(s);
		if (o != null) {
			map.putAll(o);
		}
	}

	@Override
	public Map<K, V> removeVersion(N n) {
		return version2map.remove(n);
	}

	@Override
	public Set<N> versionNumbers() {
		return version2map.keySet();
	}

	@Override
	public Collection<Pair<V, Set<N>>> get(K k, Set<N> n) {
		Collection<Pair<V, Set<N>>> s = new LinkedList<Pair<V, Set<N>>>();
		for (N vn : n) { // each version number
			N f = version2map.floorKey(vn);
			if (f != null) {
				TreeMap<K, V> map = version2map.get(f);
				if (map != null) {
					V v = map.get(k);
					if (v != null)
						s.add(new Pair<V, Set<N>>(v, singleton(vn)));
				}
			}
		}
		return s;
	}

	@Override
	public Iterator<Triplet<K, V, Set<N>>> get(Set<N> versionNumbers) {
		MultiValuedHashMap<N, N> map2numbers = new MultiValuedHashMap<N, N>();
		for (final N n : versionNumbers) {
			N f = version2map.floorKey(n);
			if (f != null) {
				map2numbers.add(f, n);
			}
		}
		MergeIterator<Triplet<K, V, Set<N>>> iterator = new MergeIterator<Triplet<K, V, Set<N>>>();
		for (final Entry<N, Set<N>> e : map2numbers.entrySet()) {
			final Iterator<Entry<K, V>> i = version2map.get(e.getKey()).entrySet().iterator();
			iterator.add(new Iterator<Triplet<K, V, Set<N>>>() {

				@Override
				public boolean hasNext() {
					return i.hasNext();
				}

				@Override
				public Triplet<K, V, Set<N>> next() {
					Entry<K, V> entry = i.next();
					return new Triplet<K, V, Set<N>>(entry.getKey(), entry.getValue(), e.getValue());
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

			});
		}
		return iterator;
	}

	@Override
	public void put(K key, V value) {
		for (Map<K, V> map : version2map.values()) {
			map.put(key, value);
		}
	}

	@Override
	public boolean containsVersion(N n) {
		return version2map.containsKey(n);
	}

	@Override
	public N smallestVersionNumber() {
		return version2map.firstKey();
	}

	@Override
	public void remove(K k) {
		for (N n : version2map.keySet()) {
			version2map.get(n).remove(k);
		}
	}

	@Override
	public boolean contains(K k, V v, Set<N> n) {
		for (N e : n) {
			if (!v.equals(version2map.get(e).get(k)))
				return false;
		}
		return true;
	}

	@Override
	public ExtendedVersionedMap<K, V, N> split() {
		NaiveVersionedMap<K, V, N> m = new NaiveVersionedMap<K, V, N>();
		int n = version2map.size() / 2;
		for (int i = 0; i < n; i++) {
			N last = version2map.lastKey();
			m.version2map.put(last, version2map.remove(last));
		}
		return m;
	}

	@Override
	public Map<K, V> clearIntersection() {
		Map<K, V> common = new TreeMap<K, V>();
		Map<K, V> map = version2map.get(smallestVersionNumber());
		Set<N> versionNumbers = difference(versionNumbers(), singleton(smallestVersionNumber()));
		for (Map.Entry<K, V> e : map.entrySet()) {
			if (contains(e.getKey(), e.getValue(), versionNumbers))
				common.put(e.getKey(), e.getValue());
		}
		for (K k : common.keySet()) { // each common key
			for (Map<K, V> m : version2map.values()) { // each map
				m.remove(k);
			}
		}
		return common;
	}

	/**
	 * Returns the key-value Map associated with the specified version number.
	 * 
	 * @param n
	 *            a version number.
	 * @return the key-value Map associated with the specified version number.
	 */
	protected TreeMap<K, V> map(N n) {
		TreeMap<K, V> map = version2map.get(n);
		if (map == null) {
			map = new TreeMap<K, V>();
			version2map.put(n, map);
		}
		return map;
	}

}

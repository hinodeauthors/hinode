package hwanglab.util.versioning;

import hwanglab.util.MergeIterator;
import hwanglab.util.MultiValuedHashMap;
import hwanglab.util.Pair;
import hwanglab.util.Triplet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A HierarchicalVersionedMap maintains a tree structure to efficiently implement a VersionedMap.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * @param <K>
 *            the type of keys.
 * @param <V>
 *            the type of values.
 * @param <N>
 *            the type of version numbers.
 */
public class HierarchicalVersionedMap<K, V, N> extends VersionedMap<K, V, N> {

	/**
	 * Automatically generated serial version UID.
	 */
	private static final long serialVersionUID = -5145094180896818753L;

	/**
	 * The arity of this HierarchicalVersionedMap.
	 */
	protected int arity;

	/**
	 * The root Node.
	 */
	protected Node root;

	/**
	 * The smallest version number.
	 */
	protected N smallestVersionNumber;

	/**
	 * The Node class implements nodes that constitute HierarchicalVersionedMaps.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	protected class Node implements java.io.Serializable {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = 4861944737953418014L;

		/**
		 * The parent Node.
		 */
		protected Node parent;

		/**
		 * The start version number of this Node.
		 */
		protected N start;

		/**
		 * The ExtendedVersionedMap associated with this Node.
		 */
		protected ExtendedVersionedMap<K, V, N> vmap;

		/**
		 * The child Nodes.
		 */
		protected TreeMap<N, Node> children = new TreeMap<N, Node>();

		/**
		 * Constructs a Node.
		 * 
		 * @param start
		 *            the start version number.
		 * @param parent
		 *            the parent Node.
		 * @param vmap
		 *            a VersionedMap.
		 */
		protected Node(N start, Node parent, ExtendedVersionedMap<K, V, N> vmap) {
			this.start = start;
			this.parent = parent;
			this.vmap = vmap;
		}

		/**
		 * Returns a String representation of this Node.
		 * 
		 * @param prefix
		 *            a prefix to be used in the String representation.
		 * @return a String representation of this Node.
		 */
		protected String toString(String prefix) {
			String s = prefix + vmap.toString() + "\r\n";
			for (Node child : children.values()) {
				s += child.toString(prefix + " ");
			}
			return s;
		}

		/**
		 * Returns the child Node in charge of the specified version.
		 * 
		 * @param n
		 *            a version number.
		 * @return the child Node in charge of the specified version.
		 */
		public Node child(N n) {
			return children.floorEntry(n).getValue();
		}

		/**
		 * Adds a child Node.
		 * 
		 * @param child
		 *            the child Node to add.
		 */
		public void addChild(Node child) {
			children.put(child.start, child);
			child.parent = this;
			vmap.remove(null, child.start); // ensure that child.start is registered in the vmap
		}

		/**
		 * Removes all of the key-value pairs contained in the specified version.
		 * 
		 * @param n
		 *            a version number.
		 * @return all of the key-value pairs removed from the specified version.
		 */
		public Map<K, V> clearVersion(N n) {
			Map<K, V> removed = vmap.removeVersion(n);
			vmap.remove(null, n);
			return removed;
		}

	}

	/**
	 * Constructs a HierarchicalVersionedMap.
	 * 
	 * @param arity
	 *            the arity of this HierarchicalVersionedMap.
	 */
	public HierarchicalVersionedMap(int arity, N smallestVersionNumber) {
		this.arity = arity;
		this.smallestVersionNumber = smallestVersionNumber;
		root = new Node(smallestVersionNumber, null, new NaiveVersionedMap<K, V, N>());
		root.addChild(new Node(smallestVersionNumber, root, new NaiveVersionedMap<K, V, N>()));
	}

	@Override
	public String toString() {
		return root.toString("");
	}

	@Override
	public void put(K k, V v, N n) {
		Node node = node(n); // node handling version n
		if (!node.vmap.containsVersion(n)) { // if n is a new version
			removeUp(n, node); // remove version n at and above node
			node.vmap.remove(null, n); // registers an empty version n
		} else
			removeUp(k, n, node); // remove k from version n at and above node
		putUp(k, v, n, node); // put (k, v) in version n at or above node
		if (full(node)) // if too many versions at node
			splitUp(node); // split node
	}

	@Override
	public V remove(K k, N n) {
		return removeUp(k, n, node(n)); // remove k from version n at and above node handling version n
	}

	@Override
	public void clone(N n, N s) throws DuplicateVersionException {
		Node node = node(n); // node handling version n
		Node source = node(s); // node handling version s
		if (source == node) { // if versions n and s are handled by the same node
			node.vmap.clone(n, s); // create version n as a clone of s at node
		} else { // if versions n and s are handled by different nodes
			removeUp(n, node); // remove version n at and above node
			node.vmap.remove(null, n); // registers an empty version n
			putUp(get(singleton(s)), n, node); // copy data from version s to version n at or above node
		}
		if (full(node)) // if too many versions at node
			splitUp(node); // split node
	}

	@Override
	public Map<K, V> removeVersion(N n) {
		return removeUp(n, node(n)); // remove version n at and above node handling version n
	}

	@Override
	public Set<N> versionNumbers() {
		TreeSet<N> versionNumbers = new TreeSet<N>();
		versionNumbers(root, versionNumbers); // find version numbers under the leaf nodes under root
		return versionNumbers;
	}

	@Override
	public Collection<Pair<V, Set<N>>> get(K k, Set<N> n) {
		MultiValuedHashMap<Node, N> nodes = nodes(n); // nodes handling specified versions
		MultiValuedHashMap<V, N> values = new MultiValuedHashMap<V, N>();
		for (Map.Entry<Node, Set<N>> e : nodes.entrySet()) { // for each relevant node
			for (Pair<V, Set<N>> p : e.getKey().vmap.get(k, e.getValue())) { // for each value associated with k
				values.add(p.first(), p.second()); // add the value and related versions
			}
		}
		return values.toPairs();
	}

	@Override
	public Iterator<Triplet<K, V, Set<N>>> get(Set<N> n) {
		MultiValuedHashMap<Node, N> nodes = nodes(n); // nodes handling specified versions
		MergeIterator<Triplet<K, V, Set<N>>> i = new MergeIterator<Triplet<K, V, Set<N>>>();
		for (Entry<Node, Set<N>> e : nodes.entrySet()) { // for each relevant node
			i.add(e.getKey().vmap.get(e.getValue())); // add an iterator over relevant triplets
		}
		return i;
	}

	/**
	 * Returns the leaf node that handles the specified version number.
	 * 
	 * @param n
	 *            a version number.
	 * @return the leaf node that handles the specified version number.
	 */
	protected Node node(N n) {
		Node node = root;
		while (node.children.size() > 0) { // if non-leaf node
			node = node.child(n); // move to the child handling version n
		}
		return node;
	}

	/**
	 * Returns a collection of entries each of which contains (1) a node associated with some of the specified versions,
	 * and (2) all, among the specified version numbers, of the version numbers associated with the node.
	 * 
	 * @param n
	 *            a set of version numbers.
	 * @return a collection of entries each of which contains (1) a node associated with some of the specified versions,
	 *         and (2) all, among the specified version numbers, of the version numbers associated with the node.
	 */
	protected MultiValuedHashMap<Node, N> nodes(Set<N> n) {
		MultiValuedHashMap<Node, N> map = new MultiValuedHashMap<Node, N>();
		for (N v : n) { // for each version v
			Node node = node(v); // node handling version v
			while (node != null) { // add node and its ancestors in conjunction with version v
				map.add(node, v);
				node = node.parent;
			}
		}
		return map;
	}

	/**
	 * Finds the version numbers handled by the descendants of the specified node.
	 * 
	 * @param node
	 *            a Node.
	 * @param n
	 *            a set for containing the found version numbers.
	 */
	protected void versionNumbers(Node node, TreeSet<N> n) {
		if (node.children.size() == 0) // if leaf node
			n.addAll(node.vmap.versionNumbers()); // add version numbers from the leaf node
		else { // if non-leaf node
			for (Node child : node.children.values()) { // for each child
				versionNumbers(child, n); // find version numbers handled by the descendants of each child
			}
		}
	}

	/**
	 * Puts the specified key-value pair in the specified version at or above the specified node.
	 * 
	 * @param k
	 *            a key.
	 * @param v
	 *            a value.
	 * @param n
	 *            a version number.
	 * @param node
	 *            a node.
	 */
	protected void putUp(K k, V v, N n, Node node) {
		if (node.parent != null && node.vmap.contains(k, v, difference(node.vmap.versionNumbers(), singleton(n)))) {
			node.vmap.remove(k); // remove k from all versions at node
			putUp(k, v, node.start, node.parent); // put (k, v) in the start version of node at or above node's parent
		} else
			node.vmap.put(k, v, n); // put (k, v) in version n at node.
	}

	/**
	 * Removes the specified version at and above the specified node.
	 * 
	 * @param n
	 *            a version number.
	 * @param node
	 *            a node.
	 * @return a map containing the key-value pairs removed from the version.
	 */
	protected Map<K, V> removeUp(N n, Node node) {
		Map<K, V> removed = node.vmap.removeVersion(n);
		if (removed == null)
			removed = new TreeMap<K, V>();
		if (node.parent != root) { // if parent is not root
			Map<K, V> removedUp = removeUp(node.start, node.parent); // key-value pairs removed at and above parent
			if (removedUp != null) {
				for (Map.Entry<K, V> e : removedUp.entrySet()) { // for each removed key-value pair
					node.vmap.put(e.getKey(), e.getValue()); // put the pair in all versions at node
				}
				removed.putAll(removedUp); // put all removed key-value pairs together
			}
		}
		return removed;
	}

	/**
	 * Removes the specified key from the specified version at and above the specified node.
	 * 
	 * @param k
	 *            a key
	 * @param n
	 *            a version number.
	 * @param node
	 *            a Node.
	 * @return the previous value associated with the removed key.
	 */
	protected V removeUp(K k, N n, Node node) {
		V v = node.vmap.remove(k, n); // remove k from version n at node
		if (v == null) { // if node didn't have k in version n
			if (node.parent != null) { // if can go up
				v = removeUp(k, node.start, node.parent); // remove k from version n at an ancestor of node
				if (v != null)
					for (N vn : node.vmap.versionNumbers())
						if (!n.equals(vn)) // for each version vn other than version n
							node.vmap.put(k, v, vn); // put (k, v) in version vn
			}
			return v;
		} else
			return v;
	}

	/**
	 * Puts the data from the specified iterator in the specified version at or above the specified node.
	 * 
	 * @param i
	 *            an iterator.
	 * @param n
	 *            a version number.
	 * @param node
	 *            a node.
	 */
	protected void putUp(Iterator<Triplet<K, V, Set<N>>> i, N n, Node node) {
		TreeMap<K, V> map = new TreeMap<K, V>();
		while (i.hasNext()) {
			Triplet<K, V, Set<N>> t = i.next();
			map.put(t.first(), t.second());
		}
		for (Map.Entry<K, V> e : map.entrySet()) {
			putUp(e.getKey(), e.getValue(), n, node);
		}
	}

	/**
	 * Determines whether or not the specified node is full (i.e., the number of versions that the node manages is
	 * larger than the arity of this HierarchicalVersionedMap).
	 * 
	 * @param node
	 *            a node.
	 * @return true if the specified node is full; false otherwise.
	 */
	protected boolean full(Node node) {
		return node.vmap.versionNumbers().size() > arity;
	}

	/**
	 * Splits the specified Node and its ancestors if they manage too many versions.
	 * 
	 * @param node
	 *            a node.
	 */
	protected void splitUp(Node node) {
		Node parent = node.parent; // parent
		if (parent == root) { // if the parent is the root
			root = new Node(smallestVersionNumber, null, new NaiveVersionedMap<K, V, N>()); // new root
			root.addChild(parent); // connect new root and the parent
		}
		Node sibling = split(node); // split node and get sibling
		if (parent.children.size() == 2) { // if the parent has two children
			Map<K, V> commonalities = parent.clearVersion(node.start); // commonalities between node and sibling
			put(commonalities, parent.start, parent.parent); // move the commonalities above parent
		} else {
			try {
				parent.vmap.removeVersion(sibling.start);
				parent.vmap.clone(sibling.start, node.start); // versions at parent for node and sibling are equal
			} catch (DuplicateVersionException e) {
				e.printStackTrace();
			}
		}
		consolidate(node); // push up the key-value pairs that belong to all versions at node
		consolidate(sibling); // push up the key-value pairs that belong to all versions at sibiling
		if (full(parent)) // if too many versions at parent
			splitUp(parent); // split parent
	}

	/**
	 * Splits the specified node.
	 * 
	 * @param node
	 *            a node to split.
	 * @return a newly created sibling.
	 */
	protected Node split(Node node) {
		ExtendedVersionedMap<K, V, N> m = node.vmap.split(); // split vmap
		Node sibling = new Node(m.smallestVersionNumber(), node.parent, m); // new sibling
		for (N vn : m.versionNumbers()) { // move children from node to sibling
			Node child = node.children.remove(vn);
			if (child != null)
				sibling.addChild(child);
		}
		node.parent.addChild(sibling); // connect parent and sibling
		return sibling;
	}

	/**
	 * Pushes up the key-value pairs that belong to all versions at the specified node.
	 * 
	 * @param node
	 *            a node.
	 */
	protected void consolidate(Node node) {
		Map<K, V> commonalities = node.vmap.clearIntersection(); // commonalities among all versions
		if (commonalities != null) {
			put(commonalities, node.start, node.parent); // move commonalities above parent
		}
	}

	/**
	 * Adds the entries in the specified Map to the specified version at the specified node.
	 * 
	 * @param map
	 *            a Map.
	 * @param n
	 *            a version number.
	 * @param node
	 *            a node.
	 */
	protected void put(Map<K, V> map, N n, Node node) {
		if (map != null)
			for (Map.Entry<K, V> e : map.entrySet()) {
				node.vmap.put(e.getKey(), e.getValue(), n);
			}
	}

}

package gstar.query.summary;


/**
 * A Minimum maintains a minimum.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Minimum<V> implements Summary<V, V> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -3651351216884552415L;

	/**
	 * The current minimum value.
	 */
	protected V minimum = null;

	/**
	 * Constructs a Minimum.
	 */
	public Minimum() {
	}

	/**
	 * Constructs a Minimum based on the specified value.
	 * 
	 * @param v
	 *            the initial value.
	 */
	public Minimum(V v) {
		this.minimum = v;
	}

	@Override
	public String toString() {
		return "" + minimum;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean update(V v) {
		if (v == null || ((Comparable<V>) minimum).compareTo(v) > 0) {
			minimum = v;
			return true;
		}
		else 
			return false;
	}

	@Override
	public boolean update(Summary<V, V> summary) {
		return update(((Minimum<V>) summary).minimum);
	}

	@Override
	public V value() {
		return minimum;
	}

	@Override
	public Summary<V, V> clone() {
		return new Minimum<V>(minimum);
	}
	
	@Override
	public int hashCode() {
		return minimum.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object other) {
		return minimum.equals(((Minimum<V>) other).minimum);
	}

}

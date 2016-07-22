package gstar.query.summary;

/**
 * A Maximum maintains a maximum.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Maximum<V> implements Summary<V, V> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -4422510237146596009L;

	/**
	 * The current maximum value.
	 */
	protected V maximum = null;

	/**
	 * Constructs a Maximum.
	 */
	public Maximum() {
	}

	/**
	 * Constructs a Maximum based on the specified value.
	 * 
	 * @param v
	 *            the initial value.
	 */
	public Maximum(V v) {
		maximum = v;
	}

	@Override
	public String toString() {
		return maximum.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean update(V v) {
		if (maximum == null || ((Comparable<V>) maximum).compareTo(v) < 0) {
			maximum = v;
			return true;
		}
		return false;
	}

	@Override
	public boolean update(Summary<V, V> summary) {
		update(((Maximum<V>) summary).maximum);
		return true;
	}

	@Override
	public V value() {
		return maximum;
	}

	@Override
	public Summary<V, V> clone() {
		return new Maximum<V>(maximum);
	}

	@Override
	public int hashCode() {
		return maximum.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object other) {
		return maximum.equals(((Maximum<V>) other).maximum);
	}

}
package gstar.query.summary;

/**
 * A Count maintains a count.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Count implements Summary<Object, Integer> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5426077527950871401L;

	/**
	 * The count managed by this Count.
	 */
	protected int count = 0;

	/**
	 * Constructs a Count.
	 */
	public Count() {
	}

	/**
	 * Constructs a Count based on the specified Count.
	 * 
	 * @param other
	 *            another Count to consume.
	 */
	protected Count(Count other) {
		count = other.count;
	}

	@Override
	public String toString() {
		return "" + count;
	}

	@Override
	public boolean update(Object v) {
		count++;
		return true;
	}

	@Override
	public Integer value() {
		return count;
	}

	@Override
	public Summary<Object, Integer> clone() {
		return new Count(this);
	}

	@Override
	public boolean update(Summary<Object, Integer> summary) {
		return update((Count) summary);
	}

	@Override
	public int hashCode() {
		return count;
	}

	@Override
	public boolean equals(Object other) {
		return count == (((Count) other).count);
	}

}
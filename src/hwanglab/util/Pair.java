package hwanglab.util;

/**
 * A Pair contains two values.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * @param <A>
 *            the type of the first value.
 * @param <B>
 *            the type of the second value.
 */
public class Pair<A, B> implements java.io.Serializable {

	/**
	 * An automatically generated serial version ID.
	 */
	private static final long serialVersionUID = -1943375464508742009L;

	/**
	 * The first value.
	 */
	A first;

	/**
	 * The second value.
	 */
	B second;

	/**
	 * Constructs a Pair.
	 * 
	 * @param first
	 *            the first value.
	 * @param second
	 *            the second value.
	 */
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Returns the first value.
	 * 
	 * @return the first value.
	 */
	public A first() {
		return first;
	}

	/**
	 * Returns the second value.
	 * 
	 * @return the second value.
	 */
	public B second() {
		return second;
	}

	/**
	 * Returns a Triplet containing the values in this Pair and the specified value.
	 * 
	 * @param <C>
	 *            the type of the third value.
	 * @param third
	 *            the third value.
	 * @return a Triplet containing the values in this Pair and the specified value.
	 */
	public <C> Triplet<A, B, C> append(C third) {
		return new Triplet<A, B, C>(first, second, third);
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}

}

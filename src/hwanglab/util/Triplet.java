package hwanglab.util;

/**
 * A Pair contains three values.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * @param <A>
 *            the type of the first value.
 * @param <B>
 *            the type of the second value.
 * @param <C>
 *            the type of the third value.
 */
public class Triplet<A, B, C> extends Pair<A, B> implements java.io.Serializable {

	/**
	 * An automatically generated serial version ID.
	 */
	private static final long serialVersionUID = -7874984054611674L;

	/**
	 * The third value.
	 */
	protected C third;

	/**
	 * Constructs a Triplet.
	 * 
	 * @param first
	 *            the first value.
	 * @param second
	 *            the second value.
	 * @param third
	 *            the third value.
	 */
	public Triplet(A first, B second, C third) {
		super(first, second);
		this.third = third;
	}

	/**
	 * Returns the third value.
	 * 
	 * @return the third value.
	 */
	public C third() {
		return third;
	}
	
	@Override
	public String toString() {
		return "(" + first + ", " + second + ", " + third + ")";
	}

}

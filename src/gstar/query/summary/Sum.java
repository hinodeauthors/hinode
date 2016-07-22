package gstar.query.summary;


/**
 * A Sum maintains a sum.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Sum implements Summary<Number, Number> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -3241645363604411074L;

	/**
	 * The sum maintained by this Sum.
	 */
	Number sum = 0;

	/**
	 * Constructs a Sum.
	 */
	public Sum() {
	}

	/**
	 * Constructs a Sum based on the specified Sum.
	 * 
	 * @param other
	 *            another Sum to consume.
	 */
	protected Sum(Sum other) {
		sum = other.sum;
	}

	@Override
	public String toString() {
		return "" + sum;
	}

	@Override
	public boolean update(Number v) {
		if (v instanceof Integer)
			sum = ((Integer) v).intValue() + sum.intValue();
		else if (v instanceof Double)
			sum = ((Double) v).doubleValue() + sum.doubleValue();
		else
			throw new UnsupportedOperationException();
		return true;
	}

	@Override
	public Number value() {
		return sum;
	}

	@Override
	public Summary<Number, Number> clone() {
		return new Sum(this);
	}

	@Override
	public boolean update(Summary<Number, Number> summary) {
		return update((Sum) summary);
	}

	/**
	 * Updates this Sum.
	 * 
	 * @param other
	 *            another Sum to consume.
	 * @return true if this Sum is updated; false otherwise.
	 */
	protected boolean update(Sum other) {
		if (other.sum instanceof Integer)
			sum = ((Integer) other.sum).intValue() + sum.intValue();
		else if (other.sum instanceof Double)
			sum = ((Double) other.sum).doubleValue() + sum.doubleValue();
		else
			throw new UnsupportedOperationException();
		return true;
	}

	@Override
	public int hashCode() {
		return sum.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return sum.equals(((Sum) other).sum);
	}

}
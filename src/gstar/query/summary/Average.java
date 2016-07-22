package gstar.query.summary;


/**
 * An Average obtains the average of given numbers.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Average extends Sum {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 763108157031464350L;
	
	/**
	 * The count.
	 */
	int count = 0;
	
	/**
	 * Constructs an Average.
	 */
	public Average() {
	}

	/**
	 * Constructs a Sum based on the specified Sum.
	 * 
	 * @param other
	 *            another Sum to consume.
	 */
	protected Average(Average other) {
		super(other);
		count = other.count;
	}

	@Override
	public String toString() {
		return "" + super.toString() + ", " + count;
	}

	@Override
	public boolean update(Number v) {
		count++;
		return super.update(v);
	}

	@Override
	public Number value() {
		if (sum instanceof Integer)
			return ((Integer)sum)/count;
		else if (sum instanceof Double)
			return ((Double)sum)/count;
		else
			throw new UnsupportedOperationException();
	}

	@Override
	public Summary<Number, Number> clone() {
		return new Average(this);
	}

	@Override
	public boolean update(Summary<Number, Number> summary) {
		return update((Average) summary);
	}

	/**
	 * Updates this Average.
	 * 
	 * @param other
	 *            another Average to consume.
	 * @return true if this Average is updated; false otherwise.
	 */
	protected boolean update(Average other) {
		count += other.count;
		return super.update(other);
	}

	@Override
	public int hashCode() {
		return sum.hashCode() + count;
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other) && count == (((Average) other).count);
	}

}
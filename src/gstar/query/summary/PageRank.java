package gstar.query.summary;

/**
 * A PageRank maintains a sum of the received PageRank values.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class PageRank implements Summary<Double, Double> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -3241645363604411074L;

	/**
	 * The sum maintained by this PageRank.
	 */
	Double sum = 0.0;

	/**
	 * The damping factor.
	 */
	protected double dampingFactor = 0.85;

	/**
	 * Constructs a PageRank.
	 */
	public PageRank(double pageRank) {
		this.sum = pageRank;
	}

	@Override
	public String toString() {
		return "" + value();
	}

	@Override
	public boolean update(Double v) {
		sum += v;
		return true;
	}

	@Override
	public Double value() {
		return (1 - dampingFactor) + dampingFactor * sum;
	}

	@Override
	public Summary<Double, Double> clone() {
		return new PageRank(this);
	}

	@Override
	public boolean update(Summary<Double, Double> summary) {
		return update((PageRank) summary);
	}

	/**
	 * Constructs a PageRank based on the specified PageRank.
	 * 
	 * @param other
	 *            another PageRank.
	 */
	protected PageRank(PageRank other) {
		sum = other.sum;
	}

	/**
	 * Updates this PageRank.
	 * 
	 * @param other
	 *            another PageRank.
	 * @return true if this Sum is updated; false otherwise.
	 */
	protected boolean update(PageRank other) {
		sum += other.sum;
		return true;
	}

	@Override
	public int hashCode() {
		return sum.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		return sum.equals(((PageRank) other).sum);
	}

}
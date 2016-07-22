package gstar.query;

/**
 * An OperatorID is an ID of an Operator.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class OperatorID implements Comparable<OperatorID>, java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 3536278219852648947L;

	/**
	 * A Wildcard used to representing all workers.
	 */
	public static int WORKER_WILDCARD = Integer.MIN_VALUE;

	/**
	 * A value representing the local worker.
	 */
	public static int LOCAL_WORKER = Integer.MIN_VALUE + 1;

	/**
	 * The name of the Operator.
	 */
	protected String operatorName;

	/**
	 * The ID of the Worker that manages the Operator.
	 */
	protected int workerID;

	/**
	 * Constructs an OperatorID.
	 * 
	 * @param operatorName
	 *            the name of the Operator.
	 * @param workerID
	 *            the ID of the Worker that manages the Operator.
	 */
	public OperatorID(String operatorName, int workerID) {
		this.operatorName = operatorName;
		this.workerID = workerID;
	}

	/**
	 * Returns the name of the Operator.
	 * 
	 * @return the name of the Operator.
	 */
	public String operatorName() {
		return operatorName;
	}

	/**
	 * Returns the ID of the Worker that manages the Operator.
	 * 
	 * @return the ID of the Worker that manages the Operator.
	 */
	public int workerID() {
		return workerID;
	}

	@Override
	public String toString() {
		return operatorName + "@"
				+ (workerID == WORKER_WILDCARD ? "*" : (workerID == LOCAL_WORKER ? "local" : "" + workerID));
	}

	@Override
	public int hashCode() {
		return operatorName.hashCode() + workerID;
	}

	@Override
	public int compareTo(OperatorID other) {
		int c = operatorName.compareTo(other.operatorName);
		if (c != 0)
			return c;
		else
			return Math.min(1, Math.max(-1, workerID - other.workerID));
	}

	@Override
	public boolean equals(Object other) {
		return compareTo((OperatorID) other) == 0;
	}

}

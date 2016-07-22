package hwanglab.net.message;

/**
 * An IteratorProxy represents an iterator (i.e., indicates that the result of an invocation is an iterator).
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class IteratorProxy implements java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -2524241262101755689L;

	/**
	 * The identifier of the associated iterator.
	 */
	int iteratorID;

	/**
	 * Constructs an IteratorProxy.
	 * 
	 * @param iteratorID
	 *            the identifier of the associated iterator.
	 */
	public IteratorProxy(int iteratorID) {
		this.iteratorID = iteratorID;
	}

	/**
	 * Returns the identifier of the associated iterator.
	 * 
	 * @return the identifier of the associated iterator.
	 */
	public int iteratorID() {
		return iteratorID;
	}

}

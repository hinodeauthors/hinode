package hwanglab.net.message;

/**
 * A RequestForPrefetchedObjects is a request for objects that the Registry prefetched using an iterator.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu).
 */
public class RequestForPrefetchedObjects implements java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5673870220282465368L;

	/**
	 * The identifier of the associated iterator.
	 */
	int iteratorID;

	/**
	 * Constructs a RequestForPrefetchedObjects.
	 * 
	 * @param iteratorID
	 *            the identifier of the associated iterator.
	 */
	public RequestForPrefetchedObjects(int iteratorID) {
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

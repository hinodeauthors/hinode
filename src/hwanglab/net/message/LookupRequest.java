package hwanglab.net.message;

/**
 * A LookupRequest represents an object lookup request.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class LookupRequest implements java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 4432510755798207325L;

	/**
	 * The identifier of the object.
	 */
	protected String objectID;

	/**
	 * The type of the object.
	 */
	protected Class<?> objectType;

	/**
	 * A LookupRequest represents a lookup request made by a RegistryClient.
	 * 
	 * @param objectID
	 *            the identifier of the object.
	 * @param objectType
	 *            the type of the object (must be an interface).
	 */
	public LookupRequest(String objectID, Class<?> objectType) {
		this.objectID = objectID;
		this.objectType = objectType;
	}

	/**
	 * Returns the identifier of the object.
	 * 
	 * @return the identifier of the object.
	 */
	public String objectID() {
		return objectID;
	}

	/**
	 * Returns the type of the object.
	 * 
	 * @return the type of the object.
	 */
	public Class<?> objectType() {
		return objectType;
	}

}

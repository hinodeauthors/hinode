package hwanglab.net;

/**
 * A LookupException occurs if a remote object lookup fails.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class LookupException extends Exception {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 2852209841903957004L;

	/**
	 * Constructs an LookupException.
	 * 
	 * @param message
	 *            the message that explains the LookupException.
	 */
	LookupException(String message) {
		super(message);
	}
}

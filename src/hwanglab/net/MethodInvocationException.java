package hwanglab.net;

/**
 * A MethodInvocationException indicates the failure of a method invocation.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class MethodInvocationException extends Exception {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 6613233897136978118L;

	/**
	 * Constructs a new MethodInvocationException.
	 * 
	 * @param cause
	 *            the cause of the MethodInvocationException.
	 */
	public MethodInvocationException(Throwable cause) {
		super(cause);
	}
}

package hwanglab.util.versioning;

/**
 * A DuplicateVersionException is thrown if an existing version number is attempted to be used for a new version.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class DuplicateVersionException extends Exception {

	/**
	 * Automatically generated serial version UID.
	 */
	private static final long serialVersionUID = 6977167297522012386L;

	/**
	 * Constructs a new DuplicateVersionException.
	 * 
	 * @param n
	 *            the version number associated with this DuplicateVersionException.
	 */
	public DuplicateVersionException(Object n) {
		super("duplicate version number: " + n);
	}

}

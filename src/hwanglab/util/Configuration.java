package hwanglab.util;

/**
 * A Configuration represents a configuration for a system.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Configuration implements java.io.Serializable {

	/**
	 * Automatically generated serial version UID.
	 */
	private static final long serialVersionUID = -16634264144616900L;

	/**
	 * Updates this Configuration according to the specified arguments.
	 * 
	 * @param args
	 *            String arguments.
	 * @throws ParsingException
	 *             if an error occurs during parsing the specified arguments.
	 */
	public void update(String... args) throws ParsingException {
		StringArrayIterator i = new StringArrayIterator(args);
		try {
			while (i.hasNext()) {
				update(i.next(), i);
			}
		} catch (ParsingException e) {
			throw e;
		} catch (Throwable cause) {
			throw new ParsingException(i, cause);
		}
	}

	/**
	 * Updates this Configuration according to the specified argument.
	 * 
	 * @param argument
	 *            the current argument.
	 * @param i
	 *            an iterator over the remaining arguments.
	 * @throws ParsingException
	 *             if an error occurs during parsing the specified arguments.
	 */
	protected void update(String argument, hwanglab.util.StringArrayIterator i) throws ParsingException {
		throw new ParsingException("Cannot parse " + i.mostRecentString());
	}

	/**
	 * A ParsingException is thrown if an error occurs during parsing.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public class ParsingException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = -5236625468445124031L;

		/**
		 * Constructs a ParsingException.
		 * 
		 * @param message
		 *            the message.
		 */
		public ParsingException(String message) {
			super(message);
		}

		/**
		 * Constructs a Parsing Exception.
		 * 
		 * @param i
		 *            an iterator over String arguments.
		 * @param cause
		 *            the cause of the ParsingException.
		 */
		public ParsingException(StringArrayIterator i, Throwable cause) {
			super("parsing status: " + i, cause);
		}

	}

	@Override
	public String toString() {
		return "";
	}

}

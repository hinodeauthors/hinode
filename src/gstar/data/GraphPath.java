package gstar.data;

/**
 * A GraphPath is a logical (system-independent) path to a GraphDirectory.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class GraphPath implements java.io.Serializable, Comparable<GraphPath> {

	/**
	 * Automatically generated serial version UID.
	 */
	private static final long serialVersionUID = 8958655884001382827L;

	/**
	 * The name separator for physical path representations.
	 */
	private static final String PHYSICAL_SEPARATOR = "!";

	/**
	 * The name separator for logical path representations.
	 */
	public static final String SEPARATOR = "/";

	/**
	 * The wildcard character.
	 */
	public static final String WILDCARD = "*";

	/**
	 * The physical representation of this GraphPath.
	 */
	protected String physicalRepresentation;

	/**
	 * Constructs a GraphPath.
	 * 
	 * @param path
	 *            the String representation of the GraphPath.
	 * @throws InvalidPathException
	 *             if an invalid path name is given.
	 */
	public GraphPath(String path) throws InvalidPathException {
		if (path.contains(PHYSICAL_SEPARATOR) || path.contains("@"))
			throw new InvalidPathException();
		path = path.trim();
		if (path.length() > 1 && path.endsWith(SEPARATOR)) // if not root and ends with /
			path = path.substring(0, path.length() - 1);
		this.physicalRepresentation = path.replace(SEPARATOR, PHYSICAL_SEPARATOR);
	}

	@Override
	public String toString() {
		return physicalRepresentation.replace(PHYSICAL_SEPARATOR, SEPARATOR);
	}

	/**
	 * Returns the file name that corresponds to the concatenation of this GraphPath
	 * 
	 * @return the file name that corresponds to the concatenation of this GraphPath.
	 */
	public String fileName() {
		return physicalRepresentation;
	}

	/**
	 * Determines whether or not this GraphPath is relative.
	 * 
	 * @return true if this GraphPath is relative; false otherwise.
	 */
	public boolean isRelative() {
		return !physicalRepresentation.startsWith(PHYSICAL_SEPARATOR);
	}

	/**
	 * Returns the concatenation of this GraphPath and the specified suffix.
	 * 
	 * @param suffix
	 *            the suffix.
	 * @return the concatenation of this GraphPath and the specified suffix.
	 * @throws InvalidPathException
	 *             if an invalid path name is given.
	 */
	public GraphPath concatenate(GraphPath suffix) throws InvalidPathException {
		if (!suffix.isRelative())
			throw new InvalidPathException();
		String current = toString();
		return new GraphPath(current + (current.endsWith(SEPARATOR) ? "" : SEPARATOR) + suffix);
	}

	/**
	 * Returns the names obtained by splitting this GraphPath at the name separators.
	 * 
	 * @return the names obtained by splitting this GraphPath at the name separators.
	 */
	public String[] split() {
		String[] names = physicalRepresentation.split(PHYSICAL_SEPARATOR);
		if (names.length == 0)
			return new String[] { "" };
		else
			return names;
	}

	@Override
	public int compareTo(GraphPath o) {
		return this.physicalRepresentation.compareTo(o.physicalRepresentation);
	}

	@Override
	public boolean equals(Object other) {
		return compareTo((GraphPath) other) == 0;
	}

	/**
	 * Returns true if and only if this GraphPath contains the specified String.
	 * 
	 * @param s
	 *            the String to search for.
	 * @return true if this GraphPath contains the specified String; false otherwise.
	 */
	public boolean contains(String s) {
		return physicalRepresentation.contains(s);
	}

	/**
	 * An InvalidPathException is thrown if an invalid path name is given.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public static class InvalidPathException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = 1640980474356032989L;

	}

}

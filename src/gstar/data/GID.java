package gstar.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A GID represents the ID of a graph.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class GID implements Comparable<GID>, java.io.Externalizable {

	/**
	 * The minimum GID value.
	 */
	public static GID MIN_VALUE = new GID(Double.NEGATIVE_INFINITY);

	/**
	 * The GID value.
	 */
	protected Double value = null;

	/**
	 * Constructs a GID.
	 */
	public GID() {
	}

	/**
	 * Constructs a GID.
	 * 
	 * @param value
	 *            the new GID value.
	 */
	public GID(double value) {
		this.value = value;
	}

	/**
	 * Constructs a GID from the specified input stream.
	 * 
	 * @param in
	 *            the input stream.
	 * @throws IOException
	 *             if an error occurs.
	 * @throws ClassNotFoundException
	 *             if an appropriate class cannot be found.
	 */
	public GID(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}

	@Override
	public String toString() {
		return "" + value;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public int compareTo(GID other) {
		return this.value < other.value ? -1 : (this.value > other.value ? 1 : 0);
	}

	@Override
	public boolean equals(Object other) {
		return compareTo((GID) other) == 0;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		value = in.readDouble();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeDouble(value);
	}

}

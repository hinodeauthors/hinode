package gstar.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A VID represents the ID of a vertex.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class VID implements Comparable<VID>, java.io.Externalizable {

	/**
	 * The VID value.
	 */
	String value;

	/**
	 * Constructs a VID.
	 */
	public VID() {
	}

	/**
	 * Constructs a VID.
	 * 
	 * @param value
	 *            the value of the VID.
	 */
	public VID(String value) {
		this.value = value;
	}

	/**
	 * Constructs a VID from the specified input stream.
	 * 
	 * @param in
	 *            an input stream.
	 * @throws IOException
	 *             if an IO error occurs.
	 * @throws ClassNotFoundException
	 *             if a relevant class cannot be found.
	 */
	public VID(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public int compareTo(VID other) {
		return value.compareTo(other.value);
	}

	@Override
	public boolean equals(Object other) {
		return compareTo((VID) other) == 0;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		value = (String) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(new String(value));
	}

}

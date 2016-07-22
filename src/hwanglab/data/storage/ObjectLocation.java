package hwanglab.data.storage;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * An ObjectLocation represents the location of an object within the Storage manager.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class ObjectLocation implements java.io.Externalizable {

	/**
	 * The ID of the SlottedPage containing the object.
	 */
	protected int pageID;

	/**
	 * The index within the SlottedPage.
	 */
	protected int index;

	/**
	 * Constructs an ObjectLocation.
	 * 
	 * @param pageID
	 *            the ID of the SlottedPage containing the object.
	 * @param index
	 *            the index within the SlottedPage.
	 */
	protected ObjectLocation(int pageID, int index) {
		this.pageID = pageID;
		this.index = index;
	}

	/**
	 * Constructs an ObjectLocation.
	 */
	public ObjectLocation() {
		this(0, 0);
	}

	/**
	 * Constructs an ObjectLocation.
	 * 
	 * @param in
	 *            an input stream.
	 * @throws IOException
	 *             if an IO error occurs.
	 * @throws ClassNotFoundException
	 *             if a relevant class cannot be found.
	 */
	public ObjectLocation(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}

	@Override
	public String toString() {
		return pageID + ":" + index;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		pageID = in.readInt();
		index = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(pageID);
		out.writeInt(index);
	}

}

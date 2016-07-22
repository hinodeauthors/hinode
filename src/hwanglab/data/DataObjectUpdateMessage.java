package hwanglab.data;

/**
 * A DataObjectUpdateMessage contains information for updating a DataObject.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class DataObjectUpdateMessage extends DataObject {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -2215402349704811052L;

	/**
	 * A flag indicating whether or not DataObjects that consume this DataObjectUpdateMessage will be reset.
	 */
	protected boolean resetScheduled = false;

	/**
	 * Constructs a DataObjectUpdateMessage.
	 */
	public DataObjectUpdateMessage() {
	}

	/**
	 * Constructs a DataObjectUpdateMessage as a clone of the specified DataObjectUpdateMessage.
	 * 
	 * @param other
	 *            another DataObjectUpdateMessage.
	 */
	public DataObjectUpdateMessage(DataObjectUpdateMessage other) {
		super(other);
		this.resetScheduled = other.resetScheduled;
	}

	/**
	 * Updates this DataObjectUpdateMessage based on the specified attribute.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @param attributeValue
	 *            the new value of the attribute; if null, DataObjects that consume this DataObjectUpdateMessage will
	 *            remove the specified attribute.
	 * @return the previous value of the specified attribute; null if none.
	 */
	public Object update(String attributeName, Object attributeValue) {
		return attributes.put(attributeName, attributeValue);
	}

	/**
	 * Updates this DataObjectUpdateMessage so that DataObjects that consume this DataObjectUpdateMessage will be reset.
	 */
	public void scheduleReset() {
		attributes.clear();
		resetScheduled = true;
	}

	/**
	 * Returns true if DataObjects that consume this DataObjectUpdateMessage will be reset.
	 * 
	 * @return true DataObjects that consume this DataObjectUpdateMessage will be reset; false otherwise.
	 */
	public boolean resetScheduled() {
		return resetScheduled;
	}

}

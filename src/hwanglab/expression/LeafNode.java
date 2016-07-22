package hwanglab.expression;

/**
 * A LeafNode contains a value.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
class LeafNode extends Node {

	/**
	 * The value.
	 */
	protected Object val;

	/**
	 * Constructs a LeafNode.
	 * 
	 * @param val
	 *            a value.
	 */
	LeafNode(Object val) {
		this.val = val;
	}

	/**
	 * Sets the value of this LeafNode.
	 * 
	 * @param val
	 *            the value.
	 */
	public void set(Object val) {
		this.val = val;
	}

	@Override
	public Object evaluate() throws UnboundVariableException {
		return val;
	}

	@Override
	public String toString() {
		return val.toString();
	}

}

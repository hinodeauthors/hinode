package hwanglab.expression;

import hwanglab.expression.UnboundVariableException;

/**
 * An Addition represents a binary addition operation.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
class Addition extends BinaryOperation {

	/**
	 * Constructs an Addition.
	 * 
	 * @param left
	 *            the left child.
	 * @param right
	 *            the right child.
	 */
	public Addition(Node left, Node right) {
		super(left, right);
	}

	@Override
	public Object evaluate() throws UnboundVariableException {
		Object l = object2num(left.evaluate());
		Object r = object2num(right.evaluate());
		if (l instanceof Integer && r instanceof Integer)
			return ((Integer) l).intValue() + ((Integer) r).intValue();
		else if (l instanceof Integer && r instanceof Double)
			return ((Integer) l).intValue() + ((Double) r).doubleValue();
		else if (l instanceof Double && r instanceof Integer)
			return ((Double) l).doubleValue() + ((Integer) r).intValue();
		else if (l instanceof Double && r instanceof Double)
			return ((Double) l).doubleValue() + ((Double) r).doubleValue();
		else
			throw new UnsupportedOperationException();
	}
	
}

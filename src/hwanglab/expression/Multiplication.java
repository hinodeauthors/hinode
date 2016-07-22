package hwanglab.expression;

/**
 * A Multipliction represents a binary mulitplication operation.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Multiplication extends BinaryOperation {

	/**
	 * Constructs a Multiplication.
	 * 
	 * @param left
	 *            the left child.
	 * @param right
	 *            the right child.
	 */
	public Multiplication(Node left, Node right) {
		super(left, right);
	}

	@Override
	public Object evaluate() throws UnboundVariableException {
		Object l = object2num(left.evaluate());
		Object r = object2num(right.evaluate());
		if (l instanceof Integer && r instanceof Integer)
			return ((Integer) l).intValue() * ((Integer) r).intValue();
		else if (l instanceof Integer && r instanceof Double)
			return ((Integer) l).intValue() * ((Double) r).doubleValue();
		else if (l instanceof Double && r instanceof Integer)
			return ((Double) l).doubleValue() * ((Integer) r).intValue();
		else if (l instanceof Double && r instanceof Double)
			return ((Double) l).doubleValue() * ((Double) r).doubleValue();
		else
			throw new UnsupportedOperationException();
	}

}

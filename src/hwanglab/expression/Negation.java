package hwanglab.expression;

import java.io.PrintStream;

/**
 * A Negation represents a negation operation.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Negation extends Node {

	/**
	 * The child.
	 */
	protected Node child;

	/**
	 * Constructs a Negation.
	 * 
	 * @param child
	 *            the single child.
	 */
	public Negation(Node child) {
		this.child = child;
	}

	@Override
	public Object evaluate() throws UnboundVariableException {
		Object c = object2num(child.evaluate());
		if (c instanceof Integer)
			return -1 * ((Integer) c).intValue();
		else if (c instanceof Double)
			return -1 * ((Double) c).doubleValue();
		else
			throw new UnsupportedOperationException();
	}

	@Override
	protected void print(PrintStream out, int indentation) {
		super.print(out, indentation);
		child.print(out, indentation + 1);
	}
	
}

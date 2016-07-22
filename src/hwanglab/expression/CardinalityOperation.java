package hwanglab.expression;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

/**
 * A CardinalityOperation provides, for each given collection, the cardinality of that collection.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class CardinalityOperation extends Node {

	/**
	 * The child.
	 */
	protected Node child;

	/**
	 * Constructs a CardinalityOperation.
	 * 
	 * @param child
	 *            the single child.
	 */
	public CardinalityOperation(Node child) {
		this.child = child;
	}

	@Override
	public Object evaluate() throws UnboundVariableException {
		Object c = child.evaluate();
		if (c instanceof Collection)
			return ((Collection<?>) c).size();
		else if (c instanceof Map<?, ?>)
				return ((Map<?, ?>) c).size();
		else
			return 1;
	}

	@Override
	protected void print(PrintStream out, int indentation) {
		super.print(out, indentation);
		child.print(out, indentation + 1);
	}

}

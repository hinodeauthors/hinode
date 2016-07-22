package hwanglab.expression;

/**
 * A Variable represents a variable in an expression.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Variable extends LeafNode {

	/**
	 * The name of this Variable.
	 */
	protected String name;

	/**
	 * Constructs a Variable.
	 * 
	 * @param name
	 *            the name of the Variable.
	 */
	Variable(String name) {
		super(null);
		this.name = name;
	}

	/**
	 * Returns the name of this Variable.
	 * 
	 * @return the name of this Variable.
	 */
	public String name() {
		return name;
	}

	@Override
	public Object evaluate() throws UnboundVariableException {
		if (val == null)
			throw new UnboundVariableException();
		return val;
	}

	@Override
	public String toString() {
		return name + "=" + val;
	}

}
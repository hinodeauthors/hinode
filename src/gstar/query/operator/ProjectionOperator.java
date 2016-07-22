package gstar.query.operator;

import hwanglab.data.DataObject;
import hwanglab.expression.ArithmeticExpression;
import hwanglab.expression.ParsingException;
import hwanglab.expression.UnboundVariableException;

/**
 * A ProjectionOperator converts into DataObjects into other DataObjects.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class ProjectionOperator extends Operator<DataObject, DataObject> {

	/**
	 * The expressions to evaluate.
	 */
	protected ArithmeticExpression[] expressions;

	/**
	 * The attributes for storing the results of evaluating the expressions.
	 */
	protected String[] outputAttributes;

	/**
	 * Constructs an ProjectionOperator.
	 * 
	 * @param expressions
	 *            the expressions to evaluate.
	 * @param outputAttributes
	 *            the attributes for storing the results of evaluating the expressions.
	 * @throws ParsingException
	 *             if an error occurs while parsing the expressions.
	 */
	public ProjectionOperator(String[] expressions, String[] outputAttributes) throws ParsingException {
		this.expressions = new ArithmeticExpression[expressions.length];
		for (int i = 0; i < expressions.length; i++) {
			this.expressions[i] = new ArithmeticExpression(expressions[i]);
		}
		this.outputAttributes = outputAttributes;
	}

	@Override
	public boolean hasNext() {
		return input(0).hasNext();
	}

	@Override
	public DataObject next() {
		DataObject in = input(0).next();
		DataObject out = new DataObject();
		for (int i = 0; i < outputAttributes.length; i++) {
			try {
				out.update(outputAttributes[i], in.evaluate(expressions[i]));
			} catch (UnboundVariableException e) {
				e.printStackTrace();
			}
		}
		return out;
	}

}

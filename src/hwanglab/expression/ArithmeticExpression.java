package hwanglab.expression;

import java.io.PrintStream;
import java.util.HashMap;

import hwanglab.util.StringTokenizer;

/**
 * An ArithmeticExpression represents an arithmetic expression.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class ArithmeticExpression {

	/**
	 * The plus symbol.
	 */
	public static String plus = "+";

	/**
	 * The minus symbol.
	 */
	public static String minus = "-";

	/**
	 * The times symbol.
	 */
	public static String times = "*";

	/**
	 * The slash symbol.
	 */
	public static String slash = "/";

	/**
	 * The left parenthesis.
	 */
	public static String lparen = "(";

	/**
	 * The right parenthesis.
	 */
	public static String rparen = ")";

	/**
	 * The variables contained in this ArithmeticExpression.
	 */
	protected HashMap<String, Variable> variables = new HashMap<String, Variable>();

	/**
	 * The root note of the parse tree.
	 */
	protected Node root;

	/**
	 * Constructs an ArithmeticExpression.
	 * 
	 * @param expression
	 *            a string representing an algebraic expression.
	 * @throws ParsingException
	 *             if a parsing error occurs.
	 */
	public ArithmeticExpression(String expression) throws ParsingException {
		StringTokenizer tokenizer = new StringTokenizer(expression, plus + minus + times + lparen + rparen + slash, '\"', '#');
		tokenizer.next();
		root = expression(tokenizer);
	}

	/**
	 * Sets the value of the specified variable.
	 * 
	 * @param name
	 *            the name of the variable.
	 * @param val
	 *            the value.
	 * @throws UnregisteredVariableException
	 *             if an unregistered variable is referenced.
	 */
	public void setVariable(String name, Object val) throws UnregisteredVariableException {
		Variable v = variables.get(name);
		if (v == null)
			throw new UnregisteredVariableException();
		v.set(val);
	}

	/**
	 * Evaluates this ArithmeticExpression.
	 * 
	 * @return the result of evaluating this ArithmeticExpression.
	 * @throws UnboundVariableException
	 *             if this ArithmeticExpression contains a variable whose value is not set.
	 */
	public Object evaluate() throws UnboundVariableException {
		return root.evaluate();
	}

	/**
	 * Returns the variables contained in this ArithmeticExpression.
	 * 
	 * @return the variables contained in this ArithmeticExpression.
	 */
	public Iterable<Variable> variables() {
		return variables.values();
	}

	/**
	 * Prints this ArithmeticExpression.
	 * 
	 * @param out
	 *            a PrintStream.
	 */
	public void print(PrintStream out) {
		root.print(out, 0);
	}

	/**
	 * Parses the expression from the specified tokenizer.
	 * 
	 * @param tokenizer
	 *            a tokenizer.
	 * @return a parse tree representing an expression.
	 * @throws ParsingException
	 *             if an error occurs while parsing an expression.
	 */
	private Node expression(StringTokenizer tokenizer) throws ParsingException {
		boolean negation = false;
		if (tokenizer.isCurrentToken(plus) || tokenizer.isCurrentToken(minus)) {
			if (tokenizer.isCurrentToken(minus))
				negation = true;
			tokenizer.next();
		}
		Node node = negation ? new Negation(term(tokenizer)) : term(tokenizer);
		while (tokenizer.isCurrentToken(plus) || tokenizer.isCurrentToken(minus)) {
			String operation = tokenizer.currentToken();
			tokenizer.next();
			node = operation.equals(plus) ? new Addition(node, term(tokenizer))
					: new Subtraction(node, term(tokenizer));
		}
		return node;
	}

	/**
	 * Parses the term from the specified tokenizer.
	 * 
	 * @param tokenizer
	 *            a tokenizer.
	 * @return a parse tree representing a term.
	 * @throws ParsingException
	 *             if an error occurs while parsing a term.
	 */
	private Node term(StringTokenizer tokenizer) throws ParsingException {
		Node node = factor(tokenizer);
		while (tokenizer.isCurrentToken(times) || tokenizer.isCurrentToken(slash)) {
			String operation = tokenizer.currentToken();
			tokenizer.next();
			node = operation.equals(times) ? new Multiplication(node, term(tokenizer)) : new Division(node,
					term(tokenizer));
		}
		return node;
	}

	/**
	 * Parses the factor from the specified tokenizer.
	 * 
	 * @param tokenizer
	 *            a tokenizer.
	 * @return a parse tree representing a factor.
	 * @throws ParsingException
	 *             if an error occurs while parsing a factor.
	 */
	private Node factor(StringTokenizer tokenizer) throws ParsingException {
		if (tokenizer.isCurrentToken("cardinality")) {
			tokenizer.next();
			if (!tokenizer.isCurrentToken(lparen))
				throw new ParsingException();
			tokenizer.next();
			Node node = expression(tokenizer);
			if (!tokenizer.isCurrentToken(rparen))
				throw new ParsingException();
			tokenizer.next();
			return new CardinalityOperation(node);
		} else if (tokenizer.isCurrentToken(lparen)) {
			tokenizer.next();
			Node node = expression(tokenizer);
			if (!tokenizer.isCurrentToken(rparen))
				throw new ParsingException();
			tokenizer.next();
			return node;
		} else if (tokenizer.isCurrentTokenNumeric()) {
			LeafNode c = new LeafNode(StringTokenizer.str2Number(tokenizer.currentToken()));
			tokenizer.next();
			return c;
		} else {
			String name = tokenizer.currentToken();
			Variable v = variables.get(name);
			if (v == null) {
				v = new Variable(name);
				variables.put(v.name(), v);
			}
			tokenizer.next();
			return v;
		}
	}

}

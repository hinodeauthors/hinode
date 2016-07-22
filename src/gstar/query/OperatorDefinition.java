package gstar.query;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import hwanglab.util.StringTokenizer;

/**
 * An OperatorDefinition defines an Operator.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 */
public class OperatorDefinition implements java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -6835888131357438854L;

	/**
	 * The left bracket.
	 */
	private static final String LEFT_BRACKET = "[";

	/**
	 * The right bracket.
	 */
	private static final String RIGHT_BRACKET = "]";

	/**
	 * The comma.
	 */
	private static final String COMMA = ",";

	/**
	 * The Worker wildcard symbol.
	 */
	private static final Object WORKER_WILDCARD = "*";

	/**
	 * A symbol for representing the local Worker.
	 */
	private static final Object LOCAL_WORKER = "local";

	/**
	 * The ID of the Operator.
	 */
	protected OperatorID operatorID;

	/**
	 * The type of the Operator.
	 */
	protected String type;

	/**
	 * The IDs of the input Operators.
	 */
	protected Collection<OperatorID> inputOperators;

	/**
	 * The arguments needed to construct the Operator.
	 */
	protected Object[] arguments;

	/**
	 * Constructs an OperatorDefinition.
	 * 
	 * @param operatorName
	 *            the name of the Operator (unique on each Worker).
	 * @param workerID
	 *            the ID of the Worker to manage the Operator.
	 * @param type
	 *            the type of Operator.
	 * @param inputOperators
	 *            the IDs of the input Operators.
	 * @param arguments
	 *            the arguments needed to construct the Operator.
	 */
	public OperatorDefinition(String operatorName, int workerID, String type, Collection<OperatorID> inputOperators,
			Object[] arguments) {
		this.operatorID = new OperatorID(operatorName, workerID);
		this.type = type;
		this.inputOperators = inputOperators;
		this.arguments = arguments;
	}

	/**
	 * Constructs an OperatorDefinition using the specified tokenizer.
	 * 
	 * @param tokenizer
	 *            the tokenizer for parsing the definition of an operator.
	 * @throws ParsingException
	 *             if a parsing error occurs.
	 */
	public OperatorDefinition(StringTokenizer tokenizer) throws ParsingException {
		try {
			String operatorName = tokenizer.next();
			tokenizer.next(); // @
			String token = tokenizer.next();
			int workerID;
			if (token.equals(WORKER_WILDCARD))
				workerID = OperatorID.WORKER_WILDCARD;
			else if (token.equals("local"))
				workerID = OperatorID.LOCAL_WORKER;
			else
				workerID = Integer.parseInt(token);
			this.operatorID = new OperatorID(operatorName, workerID);
			tokenizer.next(); // =
			this.type = tokenizer.next();
			tokenizer.next(); // (
			tokenizer.next(); // LEFT_BRACKET
			this.inputOperators = getOperatorIDs(tokenizer);
			Vector<Object> arguments = new Vector<Object>();
			while (!(token = tokenizer.next()).equals(")")) { // , or )
				token = tokenizer.next();
				if (token.equals(LEFT_BRACKET)) {
					arguments.add(getCollection(tokenizer).toArray(new String[1]));
				} else
					arguments.add(token);
			}
			this.arguments = arguments.toArray();
		} catch (Exception e) {
			throw new ParsingException(e);
		}
	}

	/**
	 * Returns the ID of the Operator.
	 * 
	 * @return the ID of the Operator.
	 */
	public OperatorID operatorID() {
		return operatorID;
	}

	/**
	 * Returns the name of the Operator.
	 * 
	 * @return the name of the Operator.
	 */
	public String operatorName() {
		return operatorID.operatorName();
	}

	/**
	 * Returns the ID of the Worker that manages the Operator.
	 * 
	 * @return the ID of the Worker that manages the Operator.
	 */
	public int workerID() {
		return operatorID.workerID();
	}

	@Override
	public String toString() {
		return operatorID.toString();
	}

	/**
	 * Returns the type of the Operator.
	 * 
	 * @return the type of the Operator.
	 */
	public String type() {
		return type;
	}

	/**
	 * Returns the IDs of the input Operators.
	 * 
	 * @return the IDs of the input Operators.
	 */
	public Collection<OperatorID> inputOperators() {
		return inputOperators;
	}

	/**
	 * Returns the arguments needed to construct the Operator.
	 * 
	 * @return the arguments needed to construct the Operator.
	 */
	public Object[] arguments() {
		return arguments;
	}

	/**
	 * Returns a collection of OperatorIDs obtained from the specified String iterator.
	 * 
	 * @param i
	 *            an iterator over Strings.
	 * @return a collection of OperatorIDs obtained from the specified String iterator.
	 */
	protected Collection<OperatorID> getOperatorIDs(Iterator<String> i) {
		LinkedList<OperatorID> inputs = new LinkedList<OperatorID>();
		while (true) {
			String token = i.next();
			if (token.equals(RIGHT_BRACKET))
				return inputs;
			else if (token.equals(COMMA))
				token = i.next();
			i.next(); // @;
			String s = i.next();
			int workerID;
			if (s.equals(WORKER_WILDCARD))
				workerID = OperatorID.WORKER_WILDCARD;
			else if (s.equals(LOCAL_WORKER))
				workerID = OperatorID.LOCAL_WORKER;
			else
				workerID = Integer.parseInt(s);
			OperatorID input = new OperatorID(token, workerID);
			inputs.add(input);
		}
	}

	/**
	 * Returns a collection of Strings obtained from the specified String iterator.
	 * 
	 * @param i
	 *            an iterator over Strings.
	 * @return a collection of Strings obtained from the specified String iterator.
	 */
	protected Collection<String> getCollection(Iterator<String> i) {
		Vector<String> collection = new Vector<String>();
		String token;
		String argument = "";
		while (true) {
			token = i.next();
			if (token.equals(RIGHT_BRACKET)) {
				if (argument.length() > 0)
					collection.add(argument);
				return collection;
			}
			else if (token.equals(COMMA)) {
				if (argument.length() > 0)
					collection.add(argument);
				argument = "";
			}
			else 
				argument += token;
		}
	}

	/**
	 * A ParsingException represents a parsing error.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public class ParsingException extends Exception {

		/**
		 * The serial version UID.
		 */
		private static final long serialVersionUID = -7739391895010729791L;

		/**
		 * Constructs a ParsingException.
		 * 
		 * @param t
		 *            the cause of this ParsingException.
		 */
		public ParsingException(Throwable t) {
			super(t);
		}

	}

}

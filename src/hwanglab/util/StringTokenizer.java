package hwanglab.util;

import java.util.LinkedList;

/**
 * A StringTokenizer parses a string into tokens.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class StringTokenizer implements java.util.Iterator<String> {

	/**
	 * A string tokenizer.
	 */
	java.util.StringTokenizer tokenizer;

	/**
	 * The next token.
	 */
	private String next = null;

	/**
	 * The current token.
	 */
	private String current = null;

	/**
	 * Constructs a StringTokenizer.
	 * 
	 * @param str
	 *            a string to be parsed.
	 * @param delimiters
	 *            the delimiters.
	 * @param stringDelim
	 *            the delimiter for representing the beginning and end of each string object.
	 * @param commentDelim
	 *            the delimiter for representing the beginning of each comment.
	 */
	public StringTokenizer(String str, String delimiters, char stringDelim, char commentDelim) {
		tokenizer = new java.util.StringTokenizer(str, delimiters + stringDelim + commentDelim + " ", true);
		prepare();
	}

	/**
	 * Prepares the next token.
	 */
	protected void prepare() {
		current = next;
		while (true) {
			if (!tokenizer.hasMoreTokens()) { // if no tokens left
				next = null;
				return;
			}
			String token = tokenizer.nextToken();
			if (token.equals("\"")) { // beginning of a String.
				next = "";
				while ((token = tokenizer.nextToken()) != null && !token.equals("\"")) {
					next += token;
				}
				return;
			}
			if (token.equals("#")) { // beginning of the comment
				next = null;
				tokenizer = new java.util.StringTokenizer("");
				return;
			} else if (!token.equals(" ")) { // skip the blanks
				next = token;
				return;
			}
		}
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public String next() {
		String r = next;
		prepare();
		return r;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the remaining tokens.
	 * 
	 * @return the remaining tokens.
	 */
	public String[] remainder() {
		LinkedList<String> remainder = new LinkedList<String>();
		while (hasNext()) {
			remainder.add(next());
		}
		return remainder.toArray(new String[] {});
	}

	/**
	 * Returns the current token.
	 * 
	 * @return the current token.
	 */
	public String currentToken() {
		return current;
	}

	/**
	 * Determines whether or not the current token is the same as the specified string.
	 * 
	 * @param str
	 *            a string to compare with the current token.
	 * @return true if the current token is the same as the specified string; false otherwise.
	 */
	public boolean isCurrentToken(String str) {
		return (str.equals(current));
	}

	/**
	 * Determines whether or not the current token represents a number.
	 * 
	 * @return true if the current token represents a number; false otherwise.
	 */

	public boolean isCurrentTokenNumeric() {
		try {
			str2Number(current);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns the number represented by the specified string.
	 * 
	 * @param str
	 *            a string.
	 * @return the number represented by the specified string.
	 * @throws NumberFormatException
	 *             if the specified string does not represent a number.
	 */
	public static Number str2Number(String str) throws NumberFormatException {
		try {
			return new Integer(str);
		} catch (Exception e) {
			return new Double(str);
		}
	}

}

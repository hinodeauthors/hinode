package hwanglab.expression;

import java.io.PrintStream;

/**
 * A BinaryOperation represents a binary operation.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public abstract class BinaryOperation extends Node {

	/**
	 * The left child.
	 */
	protected Node left;

	/**
	 * The right child.
	 */
	protected Node right;

	/**
	 * Constructs a BinaryOperation.
	 * 
	 * @param left
	 *            the left child.
	 * @param right
	 *            the right child.
	 */
	public BinaryOperation(Node left, Node right) {
		this.left = left;
		this.right = right;
	}

	@Override
	protected void print(PrintStream out, int indentation) {
		super.print(out, indentation);
		left.print(out, indentation + 1);
		right.print(out, indentation + 1);
	}

}

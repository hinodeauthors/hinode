package gstar.query.operator;

import gstar.data.Vertex;
import hwanglab.expression.ParsingException;

/**
 * A VertexOperator outputs all of the Vertices in graphs that satisfy a given predicate.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class VertexOperator extends GraphOperator<Vertex, Vertex> {

	/**
	 * Constructs a VertexOperator.
	 * 
	 * @param patterns
	 *            the patterns of the IDs of the graphs to query.
	 * @throws ParsingException
	 *             if a parsing error occurs.
	 */
	public VertexOperator(String[] patterns) throws ParsingException {
		super(patterns);
	}

	@Override
	protected void init() {
		addInput(vertices());
	}

	@Override
	public Vertex next() {
		return input(0).next();
	}

	@Override
	public boolean hasNext() {
		return hasInputData();
	}

}

package gstar.query.operator;

import gstar.data.Edge;
import gstar.data.VID;
import gstar.data.Vertex;
import gstar.query.summary.Minimum;
import gstar.query.summary.Summary;
import hwanglab.expression.ParsingException;

/**
 * A SingleSourceShortestDistanceOperator finds the shortest distance from the specified source Vertex to every other
 * Vertex reachable from the source Vertex.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class SingleSourceShortestDistanceOperator extends BSPOperator<Double, Double> {

	/**
	 * The ID of the source Vertex.
	 */
	VID src;

	/**
	 * Constructs a SingleSourceShortestDistanceOperator.
	 * 
	 * @param patterns
	 *            the patterns of the IDs of the graphs to query.
	 * @param src
	 *            the ID of the source vertex.
	 * @param outputAttribute
	 *            the output attribute.
	 * @throws ParsingException
	 *             if a parsing error occurs.
	 */
	public SingleSourceShortestDistanceOperator(String[] patterns, String src, String outputAttribute)
			throws ParsingException {
		super(patterns, outputAttribute);
		this.src = new VID(src);
	}

	@Override
	protected void init() {
		cmbr.update(src, new Minimum<Double>(0.0), graphIDs);
	}

	@Override
	protected void compute(Vertex v, Summary<Double, Double> s) {
		for (Edge e : v.outgoingEdges()) {
			cmbr.update(e.otherEnd(), new Minimum<Double>(s.value() + e.weight()), v.graphIDs());
		}
	}

}

package gstar.query.operator;

import java.util.Iterator;

import gstar.data.Edge;
import gstar.data.Vertex;
import gstar.query.summary.PageRank;
import gstar.query.summary.Summary;
import hwanglab.expression.ParsingException;

/**
 * A PageRankOperator finds, for each vertex, the PageRank of that vertex and assigns that value to a specified
 * attribute of the vertex.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class PageRankOperator extends BSPOperator<Double, Double> {

	/**
	 * Constructs a PageRankOperator.
	 * 
	 * @param patterns
	 *            the patterns of the IDs of the graphs to query.
	 * @param outputAttribute
	 *            the output attribute.
	 * @throws ParsingException
	 *             if a parsing error occurs.
	 */
	public PageRankOperator(String[] patterns, String outputAttribute) throws ParsingException {
		super(patterns, outputAttribute);
	}

	@Override
	protected void init() {
		Iterator<Vertex> i = vertices();
		while (i.hasNext()) {
			Vertex v = i.next();
			PageRank s = new PageRank(1);
			cmbr.update(v.vertexID(), s, v.graphIDs());
		}
	}

	@Override
	protected void compute(Vertex v, Summary<Double, Double> s) {
		if (superstep() < 30) {
			double val = s.value() / v.outgoingEdges().size();
			for (Edge e : v.outgoingEdges())
				cmbr.update(e.otherEnd(), new PageRank(val), v.graphIDs());
		}
	}
}

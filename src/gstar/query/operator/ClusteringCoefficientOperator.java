package gstar.query.operator;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import gstar.data.Edge;
import gstar.data.VID;
import gstar.data.Vertex;
import gstar.query.summary.CCoeffSummary;
import hwanglab.expression.ParsingException;

/**
 * A ClusteringCoefficientOperator computes the clustering coefficient for each obtained Vertex.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class ClusteringCoefficientOperator extends BSPOperator<Vertex, Double> {

	/**
	 * Constructs a ClusteringCoefficientOperator.
	 * 
	 * @param patterns
	 *            the patterns of the IDs of the graphs to query.
	 * @param outputAttribute
	 *            the output attribute.
	 * @throws ParsingException
	 *             if a parsing error occurs.
	 */
	public ClusteringCoefficientOperator(String[] patterns, String outputAttribute) throws ParsingException {
		super(patterns, outputAttribute);
	}

	@Override
	public void init() {
		Iterator<Vertex> i = vertices();
		while (i.hasNext()) {
			Vertex v = i.next();
			Set<VID> neighbors = neighborIDs(v);
			if (neighbors.size() == 0)
				cmbr.update(v.vertexID(), new CCoeffSummary(neighbors), v.graphIDs());
			else
				for (VID n : neighbors)
					cmbr.update(v.vertexID(), new CCoeffSummary(neighbors), n, v.graphIDs());
		}
	}

	/**
	 * Returns the IDs of the adjacent vertices.
	 * 
	 * @return the IDs of the adjacent vertices.
	 */
	public Set<VID> neighborIDs(Vertex v) {
		TreeSet<VID> neighbors = new TreeSet<VID>();
		for (Edge edge : v.outgoingEdges()) {
			neighbors.add(edge.otherEnd());
		}
		return neighbors;
	}

}

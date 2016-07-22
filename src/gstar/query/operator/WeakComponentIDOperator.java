package gstar.query.operator;

import java.util.HashSet;
import java.util.Iterator;

import gstar.data.Edge;
import gstar.data.VID;
import gstar.data.Vertex;
import gstar.query.summary.Minimum;
import gstar.query.summary.Summary;
import hwanglab.expression.ParsingException;

/**
 * A WeakComponentIDOperator finds, for each weakly-connected component, the smallest ID among all of the Vertices
 * in the component and assigns that ID to the component_id attribute of the Vertices.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class WeakComponentIDOperator extends BSPOperator<VID, VID> {

	/**
	 * Constructs a StrongComponentIDOperator.
	 * 
	 * @param patterns
	 *            the patterns of the IDs of the graphs to query.
	 * @param outputAttribute
	 *            the output attribute.
	 * @throws ParsingException
	 *             if a parsing error occurs.
	 */
	public WeakComponentIDOperator(String[] patterns, String outputAttribute) throws ParsingException {
		super(patterns, outputAttribute);
	}

	@Override
	protected void init() {
		Iterator<Vertex> i = vertices();
		while (i.hasNext()) {
			Vertex v = i.next();
			Minimum<VID> s = new Minimum<VID>(v.vertexID());
			cmbr.update(v.vertexID(), s, v.graphIDs());
		}
	}

	@Override
	protected void compute(Vertex v, Summary<VID, VID> s) {
		HashSet<VID> neighbors = new HashSet<VID>();
		for (Edge e : v.outgoingEdges())
			neighbors.add(e.otherEnd());
		for (Edge e: v.incomingEdges())
			neighbors.add(e.otherEnd());
		for (VID neighbor: neighbors)
			cmbr.update(neighbor, s, v.graphIDs());
	}
}

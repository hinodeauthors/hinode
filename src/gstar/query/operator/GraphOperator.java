package gstar.query.operator;

import gstar.data.GID;
import gstar.data.VID;
import gstar.data.Vertex;
import hwanglab.expression.ParsingException;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A GraphOperator processes a set of specified graphs.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 * @param <I>
 *            the input type.
 * @param <O>
 *            the output type.
 */
public abstract class GraphOperator<I, O> extends Operator<I, O> {

	/**
	 * The IDs of the graphs to query.
	 */
	Set<GID> graphIDs;

	/**
	 * Constructs a GraphOperagor.
	 * 
	 * @param patterns
	 *            the patterns of the IDs of the graphs to query.
	 * @throws ParsingException
	 *             if a parsing error occurs.
	 */
	public GraphOperator(String[] patterns) throws ParsingException {
		graphIDs = graphs(patterns);
	}

	/**
	 * Returns the Vertices that correspond to the specified vertex ID from the graphs to query.
	 * 
	 * @param v
	 *            the Vertex ID.
	 * @return the Vertices that correspond to the specified vertex ID from the graphs to query.
	 */
	public Iterator<Vertex> vertices(VID v) {
		return vertices(v, graphIDs);
	}

	/**
	 * Returns the Vertices that correspond to the specified vertex and graph IDs.
	 * 
	 * @param v
	 *            the Vertex ID.
	 * @param g
	 *            the graph IDs.
	 * @return the Vertices that correspond to the specified vertex and graph IDs.
	 */
	public Iterator<Vertex> vertices(VID v, Set<GID> g) {
		return worker.graphManager().vertices(v, g);
	}
	
	/**
	 * Returns an iterator over the Vertices from the graphs to query.
	 * 
	 * @return an iterator over the Vertices from the graphs to query.
	 */
	protected Iterator<Vertex> vertices() {
		return worker.graphManager().vertices(graphIDs);
	}

	/**
	 * Returns the graph IDs that match the specified patterns.
	 * 
	 * @param patterns
	 *            the patterns of IDs of the graphs to query.
	 * @return the graph IDs that match the specified patterns.
	 * @throws ParsingException
	 *             if a parsing error occurs.
	 */
	protected Set<GID> graphs(String[] patterns) throws ParsingException {
		TreeSet<GID> t = new TreeSet<GID>();
		for (String pattern : patterns) {
			String[] parts = pattern.split(":");
			if (parts.length == 1) {
				t.add(new GID(Double.parseDouble(parts[0])));
			} else if (parts.length == 3) {
				double start = Double.parseDouble(parts[0]);
				double stepSize = Double.parseDouble(parts[1]);
				double end = Double.parseDouble(parts[2]);
				for (double g = start; g <= end; g += stepSize) {
					t.add(new GID(g));
				}
			} else
				throw new ParsingException();

		}
		return t;
	}

}

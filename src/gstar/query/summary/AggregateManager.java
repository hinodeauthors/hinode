package gstar.query.summary;

import gstar.data.GID;
import gstar.data.VID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * A AggregateManager manages a summary for each Vertex.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 * @param <V>
 *            The input type of the summary.
 * @param <F>
 *            The output type of the summary.
 */
public class AggregateManager<V, F> {

	/**
	 * The aggregates that this AggregateManager maintains.
	 */
	protected Map<VID, Aggregate<GID, V, F>> aggregates = new TreeMap<VID, Aggregate<GID, V, F>>();

	/**
	 * Updates this AggregateManager based on the specified Vertex and summary.
	 * 
	 * @param t
	 *            the ID of the target Vertex.
	 * @param s
	 *            the summary.
	 * @param g
	 *            the IDs of the related graphs.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void updateSummary(VID t, Summary<V, F> s, Set<GID> g) {
		Aggregate<GID, V, F> aggregate = aggregates.get(t);
		if (aggregate == null) {
			aggregate = new Aggregate<GID, V, F>();
			aggregates.put(t, aggregate);
		}
		aggregate.update(new Summary[] { s }, g);
	}

	/**
	 * Returns the summaries associated with the specified Vertex and graphs.
	 * 
	 * @param v
	 *            the ID of a Vertex.
	 * @param g
	 *            the IDs of graphs.
	 * @return the summaries associated with the specified Vertex and graphs.
	 */
	public Iterable<Entry<ArrayList<Summary<V, F>>, Set<GID>>> summaries(VID v, Set<GID> g) {
		HashMap<ArrayList<Summary<V, F>>, Set<GID>> result = new HashMap<ArrayList<Summary<V, F>>, Set<GID>>();
		try {
			Aggregate<GID, V, F> aggr = aggregates.get(v);
			if (aggr != null) {
				return aggr.summaries(g);
			}
			result.put(null, g);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.entrySet();
	}

	/**
	 * Returns the summaries associated with the specified Vertex.
	 * 
	 * @param v
	 *            the ID of a Vertex.
	 * @return the summaries associated with the specified Vertex.
	 */
	public Set<Entry<ArrayList<Summary<V, F>>, Set<GID>>> summaries(VID v) {
		HashMap<ArrayList<Summary<V, F>>, Set<GID>> result = new HashMap<ArrayList<Summary<V, F>>, Set<GID>>();
		try {
			Aggregate<GID, V, F> aggr = aggregates.get(v);
			if (aggr != null) {
				return aggr.summaries();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.entrySet();
	}

	/**
	 * Returns the IDs of the Vertices that are registered in this AggregateManager.
	 * 
	 * @return the IDs of the Vertices that are registered in this AggregateManager.
	 */
	public Set<VID> vertexIDs() {
		return aggregates.keySet();
	}

	/**
	 * Removes all of the entries in this AggregateManager.
	 */
	public void clear() {
		aggregates = new TreeMap<VID, Aggregate<GID, V, F>>();
	}

	/**
	 * Determines whether or not this AggregateManager is empty.
	 * 
	 * @return true if this AggregateManager is empty; false otherwise.
	 */
	public boolean isEmpty() {
		return aggregates.size() == 0;
	}

}

package gstar.query.summary;

import gstar.data.GID;
import gstar.data.VID;
import gstar.data.Vertex;
import gstar.query.operator.BSPOperator;
import gstar.query.operator.SummaryMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A Combiner associates summaries with vertices. It also is an iterator over Vertex objects that include the final
 * values obtained from the associated summaries.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 * @param <V>
 *            The input type of the summary.
 * @param <F>
 *            The output type of the summary.
 */
public class Combiner<V, F> extends AggregateManager<V, F> implements Iterator<Vertex> {

	/**
	 * The attribute for storing the final summary values.
	 */
	protected String outputAttribute;

	/**
	 * The Operator.
	 */
	protected BSPOperator<?, ?> operator;

	/**
	 * A buffer that temporarily keeps output data.
	 */
	protected LinkedList<Vertex> outputBuffer = new LinkedList<Vertex>();

	/**
	 * Constructs an Combiner.
	 * 
	 * @param outputAttribute
	 *            the name of the output attribute.
	 * @param operator
	 *            the related operator.
	 */
	public Combiner(String outputAttribute, BSPOperator<?, ?> operator) {
		this.outputAttribute = outputAttribute;
		this.operator = operator;
	}

	/**
	 * Updates this Combiner based on the specified summary.
	 * 
	 * @param t
	 *            the ID of the target vertex (i.e., the vertex to be associated with the summary).
	 * @param s
	 *            the summary based on which this Combiner is updated.
	 * @param g
	 *            the IDs of the related graphs.
	 */
	public void update(VID t, Summary<V, F> s, Set<GID> g) {
		operator.router().enqueue(new SummaryMessage<V, F>(t, s, null, g));
	}

	/**
	 * Updates this Combiner based on the specified summary.
	 * 
	 * @param t
	 *            the ID of the target vertex (i.e., the vertex to be associated with the summary).
	 * @param g
	 *            the IDs of the graphs that contain the target vertex.
	 * @param s
	 *            the summary based on which this Combiner is updated.
	 * @param i
	 *            the identifier of the vertex to visit before the summary is applied to the target vertex.
	 */
	public void update(VID t, Summary<V, F> s, VID i, Set<GID> g) {
		operator.router().enqueue(new SummaryMessage<V, F>(t, s, i, g));
	}

	@Override
	public boolean hasNext() {
		if (outputBuffer.size() > 0) {
			return true;
		}
		prepareOutput();
		return outputBuffer.size() > 0;
	}

	@Override
	public Vertex next() {
		Vertex v = outputBuffer.poll();
		if (outputBuffer.size() == 0) {
			prepareOutput();
		}
		return v;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Prepares output data.
	 */
	protected void prepareOutput() {
		if (aggregates.size() > 0) {
			Entry<VID, Aggregate<GID, V, F>> entry = aggregates.entrySet().iterator().next();
			for (Entry<ArrayList<F>, Set<GID>> value : entry.getValue().values()) {
				Iterator<Vertex> i = operator.vertices(entry.getKey(), value.getValue());
				while (i.hasNext()) {
					Vertex v = i.next();
					v.update(outputAttribute, value.getKey().get(0));
					outputBuffer.add(v);
				}
			}
			aggregates.remove(entry.getKey());
		}
	}

}

package gstar.query.operator;

import gstar.data.GID;
import gstar.data.VID;
import gstar.data.Vertex;
import gstar.query.summary.Combiner;
import gstar.query.summary.Summary;
import gstar.query.summary.AggregateManager;
import hwanglab.expression.ParsingException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * A BSPOperator performs BSP operations.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 * @param <V>
 *            The input type of the summary.
 * @param <F>
 *            The output type of the summary.
 */
public class BSPOperator<V, F> extends GraphOperator<Vertex, Vertex> {

	/**
	 * The Combiner.
	 */
	protected Combiner<V, F> cmbr;

	/**
	 * The MessageRouter for this BSPOperator.
	 */
	MessageRouter<V, F> router = new MessageRouter<V, F>(this);

	/**
	 * A flag indicating whether or not the BSP operation is completed.
	 */
	protected boolean bspCompleted = false;

	/**
	 * The current superstep.
	 */
	protected int superstep = 0;

	/**
	 * Constructs a BSPOperator.
	 * 
	 * @param patterns
	 *            the patterns of the IDs of the graphs to query.
	 * @param outputAttribute
	 *            the output attribute.
	 * @throws ParsingException
	 *             if a parsing error occurs.
	 */
	protected BSPOperator(String[] patterns, String outputAttribute) throws ParsingException {
		super(patterns);
		cmbr = new Combiner<V, F>(outputAttribute, this);
	}

	@Override
	public boolean hasNext() {
		return cmbr.hasNext();
	}

	@Override
	public Vertex next() {
		return cmbr.next();
	}

	/**
	 * Returns the current superstep.
	 * 
	 * @return the current superstep.
	 */
	public int superstep() {
		return superstep;
	}

	/**
	 * Performs a computational task.
	 * 
	 * @param vertex
	 *            the Vertex to process.
	 * @param s
	 *            the Summary for the vertex.
	 */
	protected void compute(Vertex vertex, Summary<V, F> s) {
	}

	/**
	 * Shuts down this BSPOperator.
	 */
	public void shutdown() {
		router.shutdown();
	}

	/**
	 * Returns the MessageRouter for this BSPOperator.
	 * 
	 * @return the MessageRouter for this BSPOperator.
	 */
	public MessageRouter<V, F> router() {
		return router;
	}

	/**
	 * Performs the BSP operation.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void bsp() {
		try {
			init();
			vote(superstep++, false);
			while (true) {
				AggregateManager<V, F> aggregates = (AggregateManager<V, F>) router.resetAggregateManager();
				boolean voteForCompletion = true;
				if (!aggregates.isEmpty()) {
					process(aggregates);
					voteForCompletion = false;
				}
				if (vote(superstep++, voteForCompletion))
					return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Requests the QueryCoordinator to make a decision on the BSP operation.
	 * 
	 * @param superStepNumber
	 *            the super step number.
	 * @param voteForCompletion
	 *            a flag indicating whether or not this Operator votes for completion.
	 * @throws Exception
	 *             if an error occurs.
	 */
	protected boolean vote(int superStepNumber, boolean voteForCompletion) throws Exception {
		router.waitUntilNoOutgoingMessages();
		worker.master().requestsBSPDecision(name(), worker.workerID(), superStepNumber, voteForCompletion);
		wait();
		return bspCompleted;
	}

	/**
	 * Starts a super step.
	 */
	public void startSuperStep() {
		synchronized (this) {
			notify();
		}
	}

	/**
	 * Finalizes the BSP operation.
	 */
	public synchronized void finalizeBSP() {
		bspCompleted = true;
		notify();
	}

	/**
	 * Processes the received Vertex-To-Aggregate map.
	 * 
	 * @param vertex2Aggregate
	 *            a Vertex-To-Aggregate map.
	 */
	protected void process(AggregateManager<V, F> vertex2Aggregate) {
		Set<VID> temp = new TreeSet<VID>();
		temp.addAll(vertex2Aggregate.vertexIDs());
		for (VID v : temp) {
			for (final Entry<ArrayList<Summary<V, F>>, Set<GID>> e : vertex2Aggregate.summaries(v)) {
				Iterator<Vertex> i = vertices(v, e.getValue());
				while (i.hasNext()) {
					Vertex t = i.next();
					BSPOperator.this.process(t, e.getKey().get(0));
				}
			}
		}
//		vertex2Aggregate.clear();
	}

	/**
	 * Processes the specified Summary for the specified Vertex.
	 * 
	 * @param v
	 *            a Vertex.
	 * @param s
	 *            a Summary.
	 */
	protected void process(Vertex v, Summary<V, F> s) {
		for (Entry<ArrayList<Summary<V, F>>, Set<GID>> t : cmbr.summaries(v.vertexID(), v.graphIDs())) {
			if (t.getKey() == null || t.getKey().get(0).update((Summary<V, F>) s)) {
				cmbr.updateSummary(v.vertexID(), (Summary<V, F>) s, t.getValue());
				setGraphIDs(v, t.getValue());
				compute(v, s);
			}
		}
	}

}

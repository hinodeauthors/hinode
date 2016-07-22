package gstar.query.operator;

import gstar.Worker;
import gstar.data.GID;
import gstar.data.GraphProperties;
import gstar.data.Vertex;
import gstar.statistics.OperatorStatistics;
import hwanglab.data.DataObject;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * An Operator processes DataObjects and produces DataObjects.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 * @param <I>
 *            the input type.
 * @param <O>
 *            the output type.
 */
public abstract class Operator<I, O> {

	/**
	 * The Worker that runs this Operator.
	 */
	protected Worker worker;

	/**
	 * The inputs to this Operator.
	 */
	protected Vector<Iterator<I>> inputs = new Vector<Iterator<I>>();

	/**
	 * A flag indicating whether or not this Operator is initialized.
	 */
	boolean initialized = false;

	/**
	 * The IDs of the Workers to collaborate with.
	 */
	protected int[] workerIDs;

	/**
	 * The statistics about this Operator.
	 */
	protected OperatorStatistics statistics;

	/**
	 * The properties of related graphs;
	 */
	protected Map<GID, GraphProperties> graphProperties;

	/**
	 * Relates the specified Vertex with the specified graphs.
	 * 
	 * @param v
	 *            a Vertex.
	 * @param graphIDs
	 *            the IDs of the graphs to associate with the Vertex.
	 */
	public static void setGraphIDs(Vertex v, Set<GID> graphIDs) {
		v.update("graph.id", graphIDs);
	}

	/**
	 * Configures this Operator.
	 * 
	 * @param worker
	 *            the Worker that runs this Operator.
	 * @param statistics
	 *            the statistics about this Operator.
	 * @param workerIDs
	 *            the IDs of the Workers to collaborate with.
	 * @param graphProperties
	 *            the properties of related graphs.
	 */
	public void set(Worker worker, OperatorStatistics statistics, int[] workerIDs,
			Map<GID, GraphProperties> graphProperties) {
		this.worker = worker;
		this.statistics = statistics;
		this.workerIDs = workerIDs;
		this.graphProperties = graphProperties;
	}

	/**
	 * Returns the name of this Operator.
	 * 
	 * @return the name of this Operator.
	 */
	public String name() {
		return statistics.operatorName();
	}

	@Override
	public String toString() {
		return name() + "@" + worker;
	}

	/**
	 * Returns the Worker that runs this Operator.
	 * 
	 * @return the Worker that runs this Operator.
	 */
	public Worker worker() {
		return worker;
	}

	/**
	 * Returns the IDs of the Workers to collaborate with.
	 * 
	 * @return the IDs of the Workers to collaborate with.
	 */
	public int[] workerIDs() {
		return workerIDs;
	}

	/**
	 * Adds an input.
	 * 
	 * @param input
	 *            the input iterator to add.
	 */
	public void addInput(final Iterator<?> input) {

		inputs.add(new Iterator<I>() {

			@SuppressWarnings("unchecked")
			Iterator<I> iterator = (Iterator<I>) input;

			int inputPort = inputs.size();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public I next() {
				if (statistics != null)
					statistics.increaseProcessed(inputPort);
				return iterator.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		});
	}

	/**
	 * Returns an Iterator over all of the output DataObjects produced by this Operator.
	 * 
	 * @return an Iterator over all of the output DataObjects produced by this Operator.
	 */
	public Iterator<O> iterator() {
		return new Iterator<O>() {

			@SuppressWarnings("rawtypes")
			@Override
			public boolean hasNext() {
				if (!initialized) {
					if (Operator.this instanceof BSPOperator) {
						((BSPOperator) Operator.this).bsp();
					} else
						init();
					initialized = true;
				}
				boolean hasNext = Operator.this.hasNext();
				if (!hasNext) {
					worker.queryEngine().addCompletedOperator(Operator.this);
					if (statistics != null)
						statistics.setCompletionTime(System.currentTimeMillis());
				}
				return hasNext;
			}

			@Override
			public O next() {
				if (statistics != null)
					statistics.increaseProduced(0);
				return Operator.this.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	/**
	 * Initializes this Operator.
	 */
	protected void init() {
	}

	/**
	 * Determines whether or not this Operator has a next output DataObject.
	 * 
	 * @return true if this Operator has a next output DataObject; false otherwise.
	 */
	public abstract boolean hasNext();

	/**
	 * Returns the next output DataObject.
	 * 
	 * @return the next output DataObject.
	 */
	abstract public O next();

	/**
	 * Returns statistics about this Operator.
	 * 
	 * @return statistics about this Operator.
	 */
	public OperatorStatistics statistics() {
		return statistics;
	}

	/**
	 * Returns the input at the specified index.
	 * 
	 * @param index
	 *            the index of an input.
	 * @return the input at the specified index.
	 */
	protected Iterator<I> input(int index) {
		return inputs.elementAt(index);
	}

	/**
	 * Determines whether or not this Operator has input data.
	 * 
	 * @return true if this Operator has input data; false otherwise.
	 */
	protected boolean hasInputData() {
		for (Iterator<?> input : inputs) {
			if (input.hasNext())
				return true;
		}
		return false;
	}

	/**
	 * Constructs Comparator that uses the specified attributes.
	 * 
	 * @param attributes
	 *            the attributes.
	 * @return a Comparator that uses the specified attributes.
	 */
	protected Comparator<DataObject> comparator(final String[] attributes) {
		final int[] signs = new int[attributes.length]; // array storing sorting order (-1: descending, 1: ascending)
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].contains(":desc")) {
				attributes[i] = attributes[i].replaceAll(":desc", "");
				signs[i] = -1;
			} else {
				if (attributes[i].contains(":asc")) {
					attributes[i] = attributes[i].replaceAll(":asc", "");
				}
				signs[i] = 1;
			}
		}
		return new Comparator<DataObject>() {
			@Override
			public int compare(DataObject o1, DataObject o2) {
				for (int i = 0; i < attributes.length; i++) {
					@SuppressWarnings("unchecked")
					int c = signs[i]
							* ((Comparable<Object>) o1.value(attributes[i])).compareTo(o2.value(attributes[i]));
					if (c != 0)
						return c;
				}
				return 0;
			}
		};
	}
}

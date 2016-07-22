package gstar.query;

import gstar.Worker;
import gstar.data.GID;
import gstar.data.GraphProperties;
import gstar.query.operator.BSPOperator;
import gstar.query.operator.Operator;
import gstar.query.operator.SummaryMessage;
import gstar.statistics.OperatorStatistics;
import hwanglab.data.DataObject;
import hwanglab.util.Arrays;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A QueryEngine executes Operators for processing data.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class QueryEngine {

	/**
	 * The Operators that this QueryEngine manages.
	 */
	TreeMap<String, Operator<?, ?>> operators = new TreeMap<String, Operator<?, ?>>();

	/**
	 * The names of the completed operators.
	 */
	Set<String> completedOperators = new HashSet<String>();

	/**
	 * The Worker that runs this QueryEngine.
	 */
	protected Worker worker;

	/**
	 * Constructs a QueryEngine.
	 * 
	 * @param worker
	 *            the Worker that runs this QueryEngine.
	 */
	public QueryEngine(Worker worker) throws IOException {
		this.worker = worker;
	}

	/**
	 * Constructs an Operator according to the specified OperatorDefinition.
	 * 
	 * @param definition
	 *            the definition of the Operator to create.
	 * @param inputOperators
	 *            the IDs of the input Operators.
	 * @param workerIDs
	 *            the IDs of the Workers to collaborate with.
	 * @param graphProperties
	 *            the properties of related graphs.
	 * @throws InstantiationException
	 *             if cannot construct an Operator.
	 */
	public void createOperator(OperatorDefinition definition, Collection<OperatorID> inputOperators, int[] workerIDs,
			Map<GID, GraphProperties> graphProperties) throws InstantiationException {
		Constructor<?>[] constructors;
		try {
			constructors = Class.forName("gstar.query.operator." + definition.type()).getConstructors();
			for (Constructor<?> constructor : constructors) {
				try {
					Operator<?, ?> operator = (Operator<?, ?>) constructor.newInstance(definition.arguments());
					synchronized (operators) {
						operators.put(definition.operatorName(), operator);
					}
					OperatorStatistics statistics = new OperatorStatistics(new OperatorID(definition.operatorName(),
							worker.workerID()), operator.getClass());
					operator.set(worker, statistics, workerIDs, graphProperties);
					connect(inputOperators, operator);
					return;
				} catch (java.lang.IllegalArgumentException e) {
					// e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
		}
		throw new InstantiationException("cannot construct a(n) " + definition.type()
				+ Arrays.toString(definition.arguments(), "(", ")", ", "));
	}

	/**
	 * Obtains an iterator from the specified Operator.
	 * 
	 * @param operatorName
	 *            the name of the Operator.
	 * @return an iterator over the output data of the Operator.
	 * @throws NoOperatorException
	 *             if the desired Operator cannot be found.
	 */
	@SuppressWarnings("unchecked")
	public Iterator<DataObject> iterator(String operatorName) throws NoOperatorException {
		Operator<?, ?> o = operators.get(operatorName);
		if (o == null)
			throw new NoOperatorException();
		return (Iterator<DataObject>) o.iterator();
	}

	/**
	 * Handles the specified SummaryMessages.
	 * 
	 * @param operatorName
	 *            the name of the Operator to handle the SummaryMessages.
	 * @param summaryMessages
	 *            the SummaryMessages to handle.
	 */
	public void handle(String operatorName, Collection<SummaryMessage<?, ?>> summaryMessages) {
		((BSPOperator<?, ?>) operators.get(operatorName)).router().handle(summaryMessages);
	}

	/**
	 * Starts a new super step for the specified Operator.
	 * 
	 * @param operatorName
	 *            the name of the Operator.
	 */
	public void startSuperStep(String operatorName) {
		((BSPOperator<?, ?>) operators.get(operatorName)).startSuperStep();
	}

	/**
	 * Finalize the BSP operation for the specified Operator.
	 * 
	 * @param operatorName
	 *            the name of the Operator.
	 */
	public void finalizeBSP(String operatorName) {
		((BSPOperator<?, ?>) operators.get(operatorName)).finalizeBSP();
	}

	/**
	 * Adds a completed Operator.
	 * 
	 * @param o
	 *            a completed Operator.
	 */
	public void addCompletedOperator(Operator<?, ?> o) {
		completedOperators.add(o.name());
	}

	/**
	 * Removes the completed Operators.
	 */
	public void removeCompletedOperators() {
		synchronized (operators) {
			for (String operatorName : completedOperators) {
				Operator<?, ?> o = operators.remove(operatorName);
				if (o instanceof BSPOperator) {
					((BSPOperator<?, ?>) o).shutdown();
				}
//				System.out.println(worker + " has removed operator " + operatorName + ".");
			}
		}
		System.gc();
	}

	/**
	 * Shuts down this QueryEngine.
	 */
	public void shutdown() {
		for (Operator<?, ?> o : operators.values()) {
			if (o instanceof BSPOperator) {
				((BSPOperator<?, ?>) o).shutdown();
			}
		}
	}

	/**
	 * Returns the current statistics of the Operators.
	 * 
	 * @return the current statistics of the Operators.
	 */
	public Collection<OperatorStatistics> operatorStatistics() {
		Collection<OperatorStatistics> operatorStatistics = new LinkedList<OperatorStatistics>();
		synchronized (operators) {
			for (Operator<?, ?> o : operators.values()) {
				operatorStatistics.add(o.statistics());
			}
		}
		return operatorStatistics;
	}

	/**
	 * Adds input connections to the specified Operator.
	 * 
	 * @param inputOperators
	 *            the input Operators.
	 * @param operator
	 *            to Operator to have input connections.
	 * @throws Exception
	 *             if an error occurs.
	 */
	protected void connect(Collection<OperatorID> inputOperators, Operator<?, ?> operator) throws Exception {
		for (OperatorID i : inputOperators) {
			Iterator<?> input = worker.worker(i.workerID()).iterator(i.operatorName());
			operator.statistics().addInput(i);
			operator.addInput(input);
		}
	}

	/**
	 * A NoOperatorException is thrown if an Operator cannot be found.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public static class NoOperatorException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = -5178832249986108645L;

	}

}

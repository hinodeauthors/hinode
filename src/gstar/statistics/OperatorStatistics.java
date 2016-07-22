package gstar.statistics;

import java.io.PrintStream;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;
import gstar.query.OperatorID;

/**
 * An OperatorStatistics instance stores statistics about an Operator.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class OperatorStatistics implements Comparable<OperatorStatistics>, java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -9117972580531542679L;

	/**
	 * The ID of the Operator.
	 */
	protected OperatorID operatorID;

	/**
	 * The type of the Operator.
	 */
	protected Class<?> type;

	/**
	 * The input Operators.
	 */
	protected Vector<OperatorID> inputOperators = new Vector<OperatorID>();

	/**
	 * The number of DataObjects that are processed.
	 */
	protected HashMap<Integer, Long> processed = new HashMap<Integer, Long>();

	/**
	 * The completion time of the associated Operator.
	 */
	protected Long completionTime = null;

	/**
	 * The number of DataObjects that are produced.
	 */
	protected TreeMap<Integer, Long> produced = new TreeMap<Integer, Long>();

	/**
	 * The number of SummaryMessages that are processed.
	 */
	protected int processedSummaryMessages = 0;

	/**
	 * Constructs an OperatorStatistics.
	 * 
	 * @param operatorID
	 *            the ID of the Operator.
	 * @param type
	 *            the type of the Operator.
	 */
	public OperatorStatistics(OperatorID operatorID, Class<?> type) {
		this.operatorID = operatorID;
		this.type = type;
	}

	/**
	 * Returns the ID of the Operator.
	 * 
	 * @return the ID of the Operator.
	 */
	public OperatorID opID() {
		return operatorID;
	}

	/**
	 * Returns the name of the associated Operator.
	 * 
	 * @return the name of the associated Operator.
	 */
	public String operatorName() {
		return operatorID.operatorName();
	}

	/**
	 * Returns the ID of the Worker that manages the associated Operator.
	 * 
	 * @return the ID of the Worker that manages the associated Operator.
	 */
	public int workerID() {
		return operatorID.workerID();
	}

	/**
	 * Returns the type of the Operator.
	 * 
	 * @return the type of the Operator.
	 */
	public Class<?> operatorType() {
		return type;
	}

	/**
	 * Adds the ID of an input Operators.
	 * 
	 * @param operatorID
	 *            the ID of the input Operator.
	 */
	public void addInput(OperatorID operatorID) {
		inputOperators.add(operatorID);
	}

	/**
	 * Returns the IDs of the input Operators.
	 * 
	 * @return the IDs of the input Operators.
	 */
	public Vector<OperatorID> inputOperators() {
		return inputOperators;
	}

	/**
	 * Increases the number of processed DataObjects.
	 * 
	 * @param inputPort
	 *            the input port.
	 */
	public void increaseProcessed(int inputPort) {
		Long count = processed.get(inputPort);
		if (count == null)
			processed.put(inputPort, new Long(1));
		else
			processed.put(inputPort, count + 1);
	}

	/**
	 * Returns the number of DataObjects processed by the associated Operator.
	 * 
	 * @return the number of DataObjects processed by the associated Operator.
	 */
	public long processed() {
		long totalCount = 0;
		for (long count : this.processed.values())
			totalCount += count;
		return totalCount;
	}

	/**
	 * Increases the number of produced DataObjects.
	 */
	public void increaseProduced(int outputPort) {
		Long count = produced.get(outputPort);
		if (count == null)
			produced.put(outputPort, 1L);
		else
			produced.put(outputPort, count + 1);
	}

	/**
	 * Increases the number of SummaryMessages that are processed.
	 * 
	 * @param count
	 *            the number of SummaryMessages that are newly processed.
	 */
	public void increaseSummaryMessages(int count) {
		processedSummaryMessages += count;
	}

	/**
	 * Returns the number of SummaryMessages that are processed.
	 * 
	 * @return the number of SummaryMessages that are processed.
	 */
	public int processedSummaryMessages() {
		return processedSummaryMessages;
	}

	/**
	 * Sets the time when the associated Operator is completed.
	 * 
	 * @param completionTime
	 *            the time when the associated Operator is completed.
	 */
	public void setCompletionTime(long completionTime) {
		this.completionTime = completionTime;
	}

	/**
	 * Determines whether or not the associated Operator has completed its execution.
	 * 
	 * @return true if the associated Operator has completed its execution; false otherwise.
	 */
	public boolean completed() {
		return completionTime != null;
	}

	/**
	 * Prints out this OperatorStatistics.
	 * 
	 * @param out
	 *            the PrintStream to use.
	 */
	public void print(PrintStream out) {
		out.println(operatorID + " (" + (completed() ? "completed" : "running") + ") - input operators: "
				+ inputOperators + ", input record counts: " + processed
				+ (processedSummaryMessages > 0 ? ", summary message count: " + processedSummaryMessages : "")
				+ ", output record count: " + produced);
	}

	@Override
	public int hashCode() {
		return operatorID.hashCode();
	}

	@Override
	public int compareTo(OperatorStatistics other) {
		return operatorID.compareTo(other.operatorID);
	}

	@Override
	public boolean equals(Object other) {
		return compareTo((OperatorStatistics) other) == 0;
	}

	@Override
	public String toString() {
		return operatorID.toString();
	}

}

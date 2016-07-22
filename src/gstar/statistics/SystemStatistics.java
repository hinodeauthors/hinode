package gstar.statistics;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * A SystemStatistics instance stores statistics about the whole system.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class SystemStatistics implements java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -2022022898852555557L;

	/**
	 * The statistics about Operators.
	 */
	protected Set<OperatorStatistics> operatorStatistics = new TreeSet<OperatorStatistics>();

	/**
	 * The statistics about Workers.
	 */
	protected Set<WorkerStatistics> workerStatistics = new TreeSet<WorkerStatistics>();

	/**
	 * Updates this SystemStatistics based on the specified OperatorStatistics.
	 * 
	 * @param operatorStatistics
	 *            the OperatorStatistics to consume.
	 */
	public void update(Collection<OperatorStatistics> operatorStatistics) {
		this.operatorStatistics.addAll(operatorStatistics);
	}

	/**
	 * Updates this SystemStatistics based on the the specified WorkerStatistics.
	 * 
	 * @param workerStatistics
	 *            the WorkerStatistics to consume.
	 */
	public void update(WorkerStatistics workerStatistics) {
		this.workerStatistics.add(workerStatistics);
	}

	/**
	 * Returns the statistics about Operators.
	 * 
	 * @return the statistics about Operators.
	 */
	public Set<OperatorStatistics> operatorStatistics() {
		return operatorStatistics;
	}

	/**
	 * Returns the statistics about Workers.
	 * 
	 * @return the statistics about Workers.
	 */
	public Set<WorkerStatistics> workerStatistics() {
		return workerStatistics;
	}

	/**
	 * Updates this SystemStatistics.
	 * 
	 * @param statistics
	 *            the SystemStatistics to consume.
	 */
	public synchronized void update(SystemStatistics statistics) {
		operatorStatistics.addAll(statistics.operatorStatistics);
		workerStatistics.addAll(statistics.workerStatistics);
	}

	/**
	 * Prints out this SystemStatistics.
	 * 
	 * @param out
	 *            the PrintStream.
	 */
	public void print(PrintStream out) {
		out.println("operator statistics: ");
		for (OperatorStatistics o : operatorStatistics) {
			o.print(out);
		}
		out.println();
		out.println("worker statistics: ");
		for (WorkerStatistics s : workerStatistics) {
			s.print(out);
		}
		out.println();
	}

	/**
	 * Returns the total number of disk seeks across all Workers in the system.
	 * 
	 * @return the total number of disk seeks across all Workers in the system.
	 */
	public long diskSeeks() {
		long diskSeeks = 0;
		for (WorkerStatistics s : workerStatistics) {
			diskSeeks += s.diskSeeks();
		}
		return diskSeeks;
	}

}

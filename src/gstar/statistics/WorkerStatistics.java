package gstar.statistics;

import hwanglab.data.storage.StorageManager;
import hwanglab.util.JVMMonitor;

import java.io.PrintStream;

/**
 * A WorkerStatistics instance stores statistics about a Worker.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class WorkerStatistics implements Comparable<WorkerStatistics>, java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 4351937873844479470L;

	/**
	 * The ID of the Worker.
	 */
	int workerID;

	/**
	 * The current CPU utilization.
	 */
	double cpuUtlization = 0;

	/**
	 * The current memory utilization.
	 */
	long memoryUsed = 0;

	/**
	 * The number of disk seeks.
	 */
	long diskSeeks = 0;

	/**
	 * The ratio of the graph data cached in the memory.
	 */
	double cachingRatio = 0;

	/**
	 * Constructs a WorkerStatistics.
	 * 
	 * @param workerID
	 *            the ID of the Worker.
	 * @param monitor
	 *            a monitor for the Worker.
	 * @param storageManager
	 *            the buffer manager.
	 */
	public WorkerStatistics(int workerID, JVMMonitor monitor, StorageManager storageManager) {
		this.workerID = workerID;
		update(monitor.cpuUtilization(), monitor.memoryUsed(), storageManager == null ? 0 : storageManager.diskSeeks(),
				storageManager == null ? 0 : storageManager.actualCachingRatio());
	}

	/**
	 * Returns the ID of the Worker.
	 * 
	 * @return the ID of the Worker.
	 */
	public int workerID() {
		return workerID;
	}

	/**
	 * Updates this WorkerStatistics.
	 * 
	 * @param cpuUtilization
	 *            the current CPU utilization.
	 * @param memoryUsed
	 *            the amount of memory used.
	 * @param diskSeeks
	 *            the number of disk seeks.
	 * @param cachingRatio
	 *            the ratio of the graph data cached in the memory.
	 */
	public void update(double cpuUtilization, long memoryUsed, long diskSeeks, double cachingRatio) {
		this.cpuUtlization = cpuUtilization;
		this.memoryUsed = memoryUsed;
		this.diskSeeks = diskSeeks;
		this.cachingRatio = cachingRatio;
	}

	/**
	 * Returns the current CPU utilization of the corresponding Worker.
	 * 
	 * @return the current CPU utilization of the corresponding Worker.
	 */
	public double cpuUtilization() {
		return cpuUtlization;
	}

	/**
	 * Returns the the amount of memory used on the corresponding Worker.
	 * 
	 * @return the the amount of memory used on the corresponding Worker.
	 */
	public double memoryUsed() {
		return memoryUsed;
	}

	/**
	 * Returns the number of disk seeks.
	 * 
	 * @return the number of disk seeks.
	 */
	public long diskSeeks() {
		return this.diskSeeks;
	}

	/**
	 * Prints out this WorkerStatistics.
	 * 
	 * @param out
	 *            the output stream to use.
	 */
	public void print(PrintStream out) {
		out.println("worker " + workerID + " - cpu utilization: " + String.format("%4.2f%%", 100.0 * cpuUtlization)
				+ ", memory used: " + memoryUsed / 1024 / 1024 + "MBs" + ", disk seeks: " + diskSeeks
				+ ", actual caching ratio: " + cachingRatio);
	}

	@Override
	public int hashCode() {
		return workerID;
	}

	@Override
	public int compareTo(WorkerStatistics other) {
		return Math.min(1, Math.max(-1, workerID - other.workerID));
	}

}

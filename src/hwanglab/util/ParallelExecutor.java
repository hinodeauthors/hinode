package hwanglab.util;

import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A ParallelExecutor can perform multiple tasks in parallel.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class ParallelExecutor {

	/**
	 * The ExecutorService to use.
	 */
	protected ExecutorService executor = java.util.concurrent.Executors.newCachedThreadPool();

	/**
	 * The exceptions occurred while running the registered Tasks.
	 */
	protected LinkedList<Exception> exceptions = new LinkedList<Exception>();

	/**
	 * Each Task represents a task.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public static interface Task {

		/**
		 * A method that implements the task to carry out. This method is invoked automatically.
		 * 
		 * @throws Exception
		 *             if an error occurs.
		 */
		public void run() throws Exception;

	};

	/**
	 * Adds a Task to this ParallelExecutor.
	 * 
	 * @param task
	 *            the Task to add.
	 */
	public void add(final Task task) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					task.run();
				} catch (Exception e) {
					exceptions.add(e);
				}
			}

		};
		executor.submit(runnable);
	}

	/**
	 * Starts all of the added Runnable tasks and then waits until the tasks complete.
	 * 
	 * @throws ParalleExecutionException
	 *             if any of the Tasks throws an Exception.
	 * @throws InterruptedException
	 *             if interrupted while waiting.
	 */
	public void run() throws ParalleExecutionException, InterruptedException {
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		if (exceptions.size() > 0)
			throw new ParalleExecutionException(exceptions);
	}

	/**
	 * A ParalleExecutionException is thrown if any of the Tasks throws an Exception.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public static class ParalleExecutionException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = -2933868962247400139L;

		/**
		 * The causes of this ParalleExecutionException.
		 */
		protected Collection<Exception> causes;

		/**
		 * Constructs a ParalleExecutionException.
		 * 
		 * @param causes
		 *            the causes of the ParalleExecutionException.
		 */
		public ParalleExecutionException(Collection<Exception> causes) {
			this.causes = causes;
		}

		/**
		 * Returns the causes of this ParalleExecutionException.
		 * 
		 * @return the causes of this ParalleExecutionException.
		 */
		public Iterable<Exception> causes() {
			return causes;
		}

		/**
		 * Returns a cause of this ParalleExecutionException.
		 * 
		 * @return a cause of this ParalleExecutionException.
		 */
		public Throwable getCause() {
			return causes.iterator().next();
		}

		@Override
		public void printStackTrace(PrintStream s) {
			for (Exception e : causes) {
				e.printStackTrace(s);
			}
		}

	}

}

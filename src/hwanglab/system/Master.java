package hwanglab.system;

import hwanglab.util.ParallelExecutor;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * A Master manages the whole system.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Master extends Host implements MasterInterface {

	/**
	 * The system configuration.
	 */
	protected Configuration configuration;

	/**
	 * The Workers.
	 */
	protected TreeMap<Integer, WorkerInterface> workers = new TreeMap<Integer, WorkerInterface>();

	/**
	 * The Worker interface.
	 */
	private Class<? extends WorkerInterface> workerInterface;

	/**
	 * Constructs a Master according to the specified configuration.
	 * 
	 * @param configuration
	 *            the system configuration.
	 * @param workerInterface
	 *            the Worker interface.
	 * @param workerClass
	 *            the Worker implementation class.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Master(Configuration configuration, Class<? extends WorkerInterface> workerInterface,
			Class<? extends Worker> workerClass) throws Exception {
		super("127.0.0.1:" + configuration.masterPort(), createLookupService(configuration, workerClass));
		this.configuration = configuration;
		this.workerInterface = workerInterface;
		waitForWorkers();
		introduceWorkers();
	}

	/**
	 * Introduces Workers to each other Worker.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	protected void introduceWorkers() throws Exception {
		final TreeMap<Integer, String> workerAddresses = new TreeMap<Integer, String>();
		for (Map.Entry<Integer, WorkerInterface> e : workers.entrySet()) {
			workerAddresses.put(e.getKey(), e.getValue().hostAddress());
		}
		ParallelExecutor executor = new ParallelExecutor();
		for (final WorkerInterface worker : workers()) {
			executor.add(new ParallelExecutor.Task() {
				@Override
				public void run() throws Exception {
					worker.introduceWorkers(workerAddresses);
				}
			});
		}
		executor.run();
	}

	@Override
	public String toString() {
		return getClass().getName();
	}

	@Override
	public synchronized Configuration registerWorker(int workerID, String workerAddress) throws Exception {
		if (workerID >= configuration.numberWorkers)
			throw new InvalidWorkerRegistrationException(workerID, configuration.numberWorkers());
		if (workers.get(workerID) != null) {
			throw new DuplicateWorkerRegistrationException(workerID);
		}
		workers.put(workerID, (WorkerInterface) lookupService.lookup(workerAddress, workerInterface));
		notify();
		System.out.println(this + " registered Worker " + workerID + ".");
		return configuration;
	}

	/**
	 * A DuplicateWorkerRegistrationException is thrown if the Master attempts to register multiple Workers with the
	 * same ID.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 * 
	 */
	public static class DuplicateWorkerRegistrationException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = -4393760748011081678L;

		/**
		 * Constructs a DuplicateWorkerRegistration.
		 * 
		 * @param workerID
		 *            the ID of the Worker.
		 */
		public DuplicateWorkerRegistrationException(int workerID) {
			super("Worker " + workerID + " is already registered!");
		}

	}

	/**
	 * A InvalidWorkerRegistrationException is thrown if the Master attempts to register an inappropriate Worker.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 * 
	 */
	public static class InvalidWorkerRegistrationException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = -8059074153370941331L;

		/**
		 * Constructs a DuplicateWorkerRegistration.
		 * 
		 * @param workerID
		 *            the ID of the Worker.
		 * @param numberWorkers
		 *            the number of Workers.
		 */
		public InvalidWorkerRegistrationException(int workerID, int numberWorkers) {
			super("Worker ID " + workerID + " must be smaller than " + numberWorkers + "!");
		}

	}

	@Override
	public void shutdown() {
		for (WorkerInterface worker : workers()) {
			try {
				worker.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(this + " has shut down all Workers.");
		super.shutdown();
	}

	/**
	 * Returns the Workers managed by this Master.
	 * 
	 * @return the Workers managed by this Master.
	 */
	public Collection<WorkerInterface> workers() {
		return workers.values();
	}

	/**
	 * Waits until contacted by an appropriate number of Workers as specified in the configuration.
	 */
	protected synchronized void waitForWorkers() {
		System.out.println(this + " is waiting to be contacted by " + configuration.numberWorkers() + " Workers.");
		while (workers.size() < configuration.numberWorkers) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println(this + " registered " + configuration.numberWorkers() + " Workers.");
	}

	/**
	 * Constructs a LookupService.
	 * 
	 * @param configuration
	 *            the system configuration.
	 * @param workerClass
	 *            the Worker implementation class.
	 * @return a LookupService.
	 * @throws Exception
	 *             if an error occurs.
	 */
	private static LookupServiceInterface createLookupService(Configuration configuration,
			Class<? extends Worker> workerClass) throws Exception {
		if (configuration.mode == Configuration.ExecutionMode.SIMULATION) {
			return new hwanglab.system.simulation.LookupService(configuration, workerClass);
		} else if (configuration.mode == Configuration.ExecutionMode.REAL_EXECUTION) {
			return new hwanglab.system.real.LookupService(configuration.masterPort());
		} else
			throw new UnsupportedOperationException();
	}

	/**
	 * Starts the Master.
	 * 
	 * @param args
	 *            the arguments.
	 * @throws NumberFormatException
	 *             if an invalid port number is specified.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public static void main(String[] args) throws NumberFormatException, Exception {
		Configuration config = new Configuration();
		config.update(args);
		config.update("-real-execution"); // forces real execution
		Master master = new Master(config, WorkerInterface.class, Worker.class);
		master.shutdown();
	}
}

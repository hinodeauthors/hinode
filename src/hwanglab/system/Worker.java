package hwanglab.system;

import hwanglab.net.CommunicationException;
import hwanglab.net.LookupException;

import java.util.Map;
import java.util.TreeMap;

/**
 * A Worker performs tasks assigned by the Master.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Worker extends Host implements WorkerInterface {

	/**
	 * The ID of this Worker.
	 */
	protected int workerID;

	/**
	 * The Workers.
	 */
	protected TreeMap<Integer, WorkerInterface> workers = new TreeMap<Integer, WorkerInterface>();

	/**
	 * The Master.
	 */
	protected MasterInterface master;

	/**
	 * The addresses of the Workers.
	 */
	protected Map<Integer, String> workerAddresses;

	/**
	 * Constructs a Worker.
	 * 
	 * @param workerID
	 *            the ID of the Worker.
	 * @param workerAddress
	 *            the address of the Worker in the form of [IP address]:[port number].
	 * @param masterAddress
	 *            the address of the Master in the form of [IP address]:[port number].
	 * @param masterInterface
	 *            the interface the Master interface.
	 * @param lookupService
	 *            the LookupService for accessing the Master and the Workers.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Worker(int workerID, String workerAddress, String masterAddress,
			Class<? extends MasterInterface> masterInterface, LookupServiceInterface lookupService) throws Exception {
		super(workerAddress, lookupService);
		this.workerID = workerID;
		this.master = lookupService.lookup(masterAddress, masterInterface);
		initialize(master.registerWorker(workerID, address));
	}

	/**
	 * Returns the ID of this Worker.
	 * 
	 * @return the ID of this Worker.
	 */
	public int workerID() {
		return this.workerID;
	}

	/**
	 * Initializes this Worker based on the configuration received from the Master.
	 * 
	 * @param configuration
	 *            the configuration from the Master.
	 */
	protected void initialize(Configuration configuration) throws Exception {
	}

	@Override
	public synchronized void shutdown() {
		super.shutdown();
	}

	@Override
	public String toString() {
		return getClass().getName() + "(" + workerID + ")";
	}

	@Override
	public void introduceWorkers(Map<Integer, String> workerAddresses) {
		this.workerAddresses = workerAddresses;
	}

	/**
	 * Registers a Worker.
	 * 
	 * @param workerID
	 *            the ID of the Worker.
	 * @param workerAddress
	 *            the address of the Worker.
	 * @return a Worker.
	 * @throws LookupException
	 *             if the Worker cannot be looked up.
	 * @throws CommunicationException
	 *             if the communication fails.
	 */
	protected WorkerInterface registerWorker(Integer workerID, String workerAddress) throws LookupException,
			CommunicationException {
		WorkerInterface w = null;
		Class<?> workerInterface = getClass().getInterfaces()[0];
		if (workerID == this.workerID)
			w = this;
		else
			w = (WorkerInterface) lookupService.lookup(workerAddress, workerInterface);
		workers.put(workerID, w);
		return w;
	}

	/**
	 * Returns the specified Worker.
	 * 
	 * @param workerID
	 *            the ID of the Worker.
	 * @return the specified Worker.
	 * @throws LookupException
	 *             if the Worker cannot be looked up.
	 * @throws CommunicationException
	 *             if the communication fails.
	 */
	protected WorkerInterface worker(int workerID) throws LookupException, CommunicationException {
		WorkerInterface worker = workers.get(workerID);
		if (worker != null)
			return worker;
		else
			return registerWorker(workerID, workerAddresses.get(workerID));
	}

	/**
	 * Starts a Worker.
	 * 
	 * @param args
	 *            the arguments.
	 * @throws NumberFormatException
	 *             if an invalid port number is specified.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public static void main(String[] args) throws NumberFormatException, Exception {
		int port = Integer.parseInt(args[1].split(":")[1]);
		new Worker(Integer.parseInt(args[0]), args[1], args[2], MasterInterface.class,
				new hwanglab.system.real.LookupService(port));
	}
}

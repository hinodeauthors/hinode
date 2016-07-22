package hwanglab.system;

/**
 * MasterInterface is the interface for the Master.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public interface MasterInterface extends HostInterface {

	/**
	 * Registers the specified Worker.
	 * 
	 * @param workerID
	 *            the ID of the Worker.
	 * @param workerAddress
	 *            the address of the Worker in the form of [IP address]:[port number].
	 * @return the system configuration.
	 * @throws Exception
	 *             if an error occurs.
	 */
	Configuration registerWorker(int workerID, String workerAddress) throws Exception;

	/**
	 * Shuts down the whole system (i.e., the Master and all Workers).
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	@Override
	void shutdown() throws Exception;

}

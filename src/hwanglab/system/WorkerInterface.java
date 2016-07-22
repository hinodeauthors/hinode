package hwanglab.system;

import java.util.Map;

/**
 * WorkerInterface is the interface for Workers.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public interface WorkerInterface extends HostInterface {

	/**
	 * Introduces the specified Workers to this Worker.
	 * 
	 * @param workerAddresses
	 *            the host addresses of the Workers.
	 * @throws Exception
	 *             if an error occurs.
	 */
	void introduceWorkers(Map<Integer, String> workerAddresses) throws Exception;

}

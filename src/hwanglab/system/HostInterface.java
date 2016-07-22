package hwanglab.system;

/**
 * HostInterface is the interface for Hosts.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public interface HostInterface {

	/**
	 * Returns the address of this Host in the form of [IP address]:[port number].
	 * 
	 * @return the address of this Host in the form of [IP address]:[port number].
	 */
	public String hostAddress();

	/**
	 * Shuts down this Host.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	void shutdown() throws Exception;

}

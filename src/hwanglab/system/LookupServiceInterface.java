package hwanglab.system;

import hwanglab.net.CommunicationException;
import hwanglab.net.LookupException;

/**
 * A LookupService is the interface for LookupServices.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public interface LookupServiceInterface {

	/**
	 * Returns a stub for accessing a remote Host.
	 * 
	 * @param hostAddress
	 *            the address of the Host in the form of [IP address]:[port number].
	 * @param hostType
	 *            the type of the Host (must be an interface).
	 * @return a stub for accessing a remote Host.
	 * @throws LookupException
	 *             if the Host cannot be looked up.
	 * @throws CommunicationException
	 *             if the communication fails.
	 */
	<H extends HostInterface> H lookup(String hostAddress, Class<?> hostType) throws LookupException,
			CommunicationException;

	/**
	 * Shuts down this LookupService.
	 */
	void shutdown();

	/**
	 * Registers the specified Host.
	 * 
	 * @param host
	 *            the Host to register.
	 */
	void register(HostInterface host);

}

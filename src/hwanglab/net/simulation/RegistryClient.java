package hwanglab.net.simulation;

import hwanglab.net.CommunicationException;

/**
 * A RegistryClient can connect to a Registry and obtain a stub for accessing an object managed by the Registry.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class RegistryClient extends hwanglab.net.RegistryClient {

	/**
	 * The Registry to connect to in the simulation mode.
	 */
	Registry registry;

	/**
	 * Constructs a RegistryClient.
	 * 
	 * @param registry
	 *            the Registry to connect to.
	 */
	public RegistryClient(hwanglab.net.Registry registry) {
		this.registry = (Registry) registry;
	}

	@Override
	public synchronized Object sendToRegistry(Object o) throws CommunicationException {
		try {
			byte[] byteArray = Registry.toByteArray(o);
			bytesSent += byteArray.length;
			byteArray = registry.handle(byteArray);
			bytesReceived += byteArray.length;
			return Registry.toObject(byteArray);
		} catch (Throwable t) {
			throw new CommunicationException(t);
		}
	}

}

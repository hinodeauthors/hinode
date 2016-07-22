package hwanglab.system.real;

import hwanglab.net.CommunicationException;
import hwanglab.net.LookupException;
import hwanglab.net.real.Registry;
import hwanglab.system.HostInterface;
import hwanglab.system.LookupServiceInterface;

/**
 * A LookupService creates subs for accessing the Master and Workers for the real execution mode.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class LookupService implements LookupServiceInterface {

	/**
	 * The Registry for this LookupService.
	 */
	protected Registry registry;

	/**
	 * Constructs a LookupService.
	 * 
	 * @param port
	 *            the port number for the LookupService.
	 * @throws Exception
	 *             if a LookupService cannot be constructed.
	 */
	public LookupService(int port) throws Exception {
		this.registry = new hwanglab.net.real.Registry(port);
	}

	@Override
	public <H extends HostInterface> H lookup(String hostAddress, Class<?> hostType) throws LookupException,
			CommunicationException {
		String[] parts = hostAddress.split(":");
		return new hwanglab.net.real.RegistryClient(parts[0], Integer.parseInt(parts[1])).lookup("", hostType);
	}

	@Override
	public void shutdown() {
		new Thread() {
			public void run() {
				registry.shutdown();
			}
		}.start();
	}

	@Override
	public void register(HostInterface host) {
		registry.register("", host);
	}

}

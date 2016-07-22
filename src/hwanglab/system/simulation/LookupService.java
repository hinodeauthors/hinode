package hwanglab.system.simulation;

import hwanglab.net.CommunicationException;
import hwanglab.net.LookupException;
import hwanglab.system.Configuration;
import hwanglab.system.HostInterface;
import hwanglab.system.LookupServiceInterface;
import hwanglab.system.Worker;

import java.util.TreeMap;

/**
 * A LookupService creates subs for accessing the Master and Workers for the simulation mode.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class LookupService implements LookupServiceInterface {

	/**
	 * The Workers.
	 */
	protected TreeMap<String, HostInterface> hosts = new TreeMap<String, HostInterface>();

	/**
	 * Constructs a LookupService.
	 * 
	 * @param configuration
	 *            the system configuration.
	 * @param workerClass
	 *            the Worker implementation class.
	 */
	public LookupService(final Configuration configuration, final Class<? extends Worker> workerClass) {
		new Thread() {
			public void run() {
				for (int i = 0; i < configuration.numberWorkers(); i++) {
					while (true) {
						try {
							workerClass.getConstructor(int.class, String.class, String.class, LookupServiceInterface.class)
									.newInstance(i, "" + i + ":" + configuration.masterPort(),
											"127.0.0.1:" + configuration.masterPort(), LookupService.this);
							break;
						} catch (Exception e) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e1) {
							}
//							e.printStackTrace();
						}
					}
				}
			}
		}.start();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <H extends HostInterface> H lookup(String hostAddress, Class<?> hostType) throws LookupException,
			CommunicationException {
		return (H) hosts.get(hostAddress);
	}

	@Override
	public void shutdown() {
		// no operations needed.
	}

	@Override
	public synchronized void register(HostInterface host) {
		hosts.put(host.hostAddress(), host);
	}

}

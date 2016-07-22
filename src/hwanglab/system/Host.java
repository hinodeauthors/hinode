package hwanglab.system;

import java.net.UnknownHostException;

/**
 * A Host represents either a Worker or the Master of the system.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Host implements HostInterface {

	/**
	 * The address of this Host in the form of [IP address]:[port number].
	 */
	protected String address;

	/**
	 * The LookupSerivice.
	 */
	protected LookupServiceInterface lookupService;

	/**
	 * The start time of this Host.
	 */
	protected long startTime = System.currentTimeMillis();

	/**
	 * Constructs a Host.
	 * 
	 * @param hostAddress
	 *            the address of the Host in the form of [IP address]:[port number].
	 * @param lookupService
	 *            the LookupService to use.
	 */
	public Host(String hostAddress, LookupServiceInterface lookupService) {
		if (hostAddress.split(":").length < 2) {
			try {
				hostAddress = java.net.InetAddress.getLocalHost().getHostAddress() + ":" + hostAddress;
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		this.address = hostAddress;
		this.lookupService = lookupService;
		lookupService.register(this);
	}

	@Override
	public String hostAddress() {
		return address;
	}

	@Override
	public void shutdown() {
		lookupService.shutdown();
	}

	/**
	 * Returns the current time.
	 * 
	 * @return the current time.
	 */
	public long getCurrentTime() {
		return System.currentTimeMillis() - startTime;
	}

}

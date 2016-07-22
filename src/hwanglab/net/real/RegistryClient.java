package hwanglab.net.real;

import hwanglab.net.CommunicationException;

/**
 * A RegistryClient can connect to a Registry and obtain a stub for accessing an object managed by the Registry.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class RegistryClient extends hwanglab.net.RegistryClient {

	/**
	 * The address of the Registry.
	 */
	protected String address;

	/**
	 * The port number of the Registry.
	 */
	protected int port;

	/**
	 * The socket used for communication.
	 */
	protected java.net.Socket socket = null;

	/**
	 * The input stream.
	 */
	protected java.io.ObjectInputStream inputStream = null;

	/**
	 * The output stream.
	 */
	protected java.io.ObjectOutputStream outputStream = null;

	/**
	 * Constructs a RegistryClient.
	 * 
	 * @param address
	 *            the address of the Registry.
	 * @param port
	 *            the port number of the Registry.
	 */
	public RegistryClient(String address, int port) {
		this.address = address;
		this.port = port;
		connect();
	}

	@Override
	public synchronized Object sendToRegistry(Object object) throws CommunicationException {
		try {
			byte[] byteArray = hwanglab.net.Registry.toByteArray(object);
			bytesSent += byteArray.length;
			outputStream.writeInt(byteArray.length);
			outputStream.write(byteArray);
			outputStream.flush();

			int length = inputStream.readInt();
			byteArray = new byte[length];
			bytesReceived += byteArray.length;
			inputStream.readFully(byteArray);
			object = hwanglab.net.Registry.toObject(byteArray);
			return object;
		} catch (Throwable t) {
			throw new CommunicationException(t);
		}
	}

	@Override
	public String toString() {
		return "[->" + address + ":" + port + "]";
	}

	/**
	 * Connects to the Registry.
	 */
	protected void connect() {
		for (;;) {
			try {
				socket = new java.net.Socket(address, port);
				outputStream = new java.io.ObjectOutputStream(socket.getOutputStream());
				inputStream = new java.io.ObjectInputStream(socket.getInputStream());
				return;
			} catch (Exception e) {
				System.err.println(e);
				try { // if failed, sleep for a short while.
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
		}
	}

}

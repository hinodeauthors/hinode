package hwanglab.net.real;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A Registry maintains a collection of objects that can be used by remote clients.
 * 
 * @author Jeong-Hyon Hwang (jhhwang@cs.albany.edu)
 */
public class Registry extends hwanglab.net.Registry {

	/**
	 * The server socket.
	 */
	protected java.net.ServerSocket socket;

	/**
	 * The threads that communicate with clients.
	 */
	java.util.Vector<CommunicationThread> communicationThreads = new java.util.Vector<CommunicationThread>();

	/**
	 * A CommunicationThread communicates with a client.
	 * 
	 * @author Jeong-Hyon Hwang (jhhwang@cs.albany.edu)
	 */
	protected class CommunicationThread extends Thread {

		/**
		 * A socket for communication with the client.
		 */
		java.net.Socket socket;

		/**
		 * Constructs a CommunicationThread.
		 * 
		 * @param socket
		 *            the socket to communicate with a client.
		 */
		public CommunicationThread(java.net.Socket socket) {
			this.socket = socket;
			start();
		}

		/**
		 * Keeps reading and handling data from the client.
		 */
		public void run() {
			try {
				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
				for (;;) {
					int length = inputStream.readInt();
					byte[] byteArray = new byte[length];
					Registry.this.bytesReceived += byteArray.length;
					inputStream.readFully(byteArray);
					Object object = Registry.toObject(byteArray);

					object = handle(object);
					byteArray = Registry.toByteArray(object);
					Registry.this.bytesSent += byteArray.length;
					outputStream.writeInt(byteArray.length);
					outputStream.write(byteArray);
					outputStream.flush();
				}
			} catch (Exception e) {
				synchronized (communicationThreads) {
					communicationThreads.remove(this);
				}
			}
		}
	}

	/**
	 * Constructs a Registry.
	 * 
	 * @param port
	 *            the communication port.
	 * @throws Exception
	 *             if it cannot create a server socket.
	 */
	public Registry(int port) throws Exception {
		super();
		socket = new java.net.ServerSocket(port);
		new Thread() {
			public void run() {
				for (;;) {
					try {
						CommunicationThread r = new CommunicationThread(socket.accept());
						synchronized (communicationThreads) {
							communicationThreads.add(r);
						}
					} catch (Exception e) {
						if (!(e instanceof java.net.SocketException))
							e.printStackTrace();
					}
					if (socket.isClosed()) { // if there has been a request for shutdown
						for (Object o : communicationThreads.toArray()) {
							try { // terminate the communication threads
								((CommunicationThread) o).socket.close();
							} catch (Exception e) {
							}
						}
						return;
					}
				}
			}
		}.start();
	}

	@Override
	public void shutdown() {
		try {
			socket.close();
		} catch (Exception e) {
		}
	}

	@Override
	public String toString() {
		return "[" + getLocalHostName() + ":" + socket.getLocalPort() + "]";
	}

	/**
	 * Returns the name of the host that runs this Registry.
	 * 
	 * @return the name of the host that runs this Registry.
	 */
	protected String getLocalHostName() {
		try {
			return java.net.InetAddress.getLocalHost().getCanonicalHostName();
		} catch (Exception e) {
			return "?";
		}
	}

}

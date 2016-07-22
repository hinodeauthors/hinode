package hwanglab.net.simulation;

import java.io.IOException;

/**
 * A Registry maintains a collection of objects that can be used by remote clients.
 * 
 * @author Jeong-Hyon Hwang (jhhwang@cs.albany.edu)
 */
public class Registry extends hwanglab.net.Registry {

	/**
	 * Constructs a Registry.
	 */
	public Registry() {
		super();
	}

	/**
	 * Handles the specified byte array.
	 * 
	 * @param byteArray
	 *            the byte array to handle.
	 * @return the result of handling the specified byte array.
	 * @throws ClassNotFoundException
	 *             if an appropriate class cannot be found.
	 * @throws IOException
	 *             if an error occurs.
	 */
	public byte[] handle(byte[] byteArray) throws IOException, ClassNotFoundException {
		bytesReceived += byteArray.length;
		Object result = handle(toObject(byteArray));
		byteArray = toByteArray(result);
		bytesSent += byteArray.length;
		return byteArray;
	}

}

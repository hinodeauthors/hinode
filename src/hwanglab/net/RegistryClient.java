package hwanglab.net;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.LinkedList;

import hwanglab.net.message.IteratorProxy;
import hwanglab.net.message.LookupRequest;
import hwanglab.net.message.MethodInvocationRequest;
import hwanglab.net.message.RequestForPrefetchedObjects;

/**
 * A RegistryClient can connect to a Registry and obtain a stub for accessing an object managed by the Registry.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public abstract class RegistryClient {

	/**
	 * The maximum interval between times when this RegistryClient checks availability of objects that the Registry
	 * proactively retrieved using an iterator.
	 */
	public static int MAXIMUM_BACKOFF_INTERVAL = 1000;

	/**
	 * The number of bytes received so far.
	 */
	protected long bytesReceived = 0;

	/**
	 * The number of bytes sent so far.
	 */
	protected long bytesSent = 0;

	/**
	 * Returns a stub for accessing an object managed by the Registry.
	 * 
	 * @param objectID
	 *            the ID of the object.
	 * @param objectType
	 *            the type of the object (must be an interface).
	 * @return a stub for accessing an object managed by the Registry.
	 * @throws CommunicationException
	 *             if a communication error occurs.
	 * @throws LookupException
	 *             if the Registry has no object with the specified ID and type.
	 */
	public <T> T lookup(String objectID, Class<?> objectType) throws LookupException, CommunicationException {
		if (!objectType.isInterface())
			throw new LookupException("" + objectType + " is not an interface.");
		if ((Boolean) sendToRegistry(new LookupRequest(objectID, objectType))) {
			// if the remote object is of the specified interface type
			return createStub(objectID, objectType);
		} else {
			throw new LookupException("The remote object is not of the " + objectType + " type.");
		}
	}

	/**
	 * Constructs a stub for accessing an object managed by the Registry.
	 * 
	 * @param objectID
	 *            the ID of the object.
	 * @param objectType
	 *            the type of the object (must be an interface).
	 * @return a stub for accessing an object managed by the Registry.
	 */
	@SuppressWarnings("unchecked")
	protected <T> T createStub(final String objectID, Class<?> objectType) {
		return (T) Proxy.newProxyInstance(objectType.getClassLoader(), new Class[] { objectType },
				new InvocationHandler() {

					public Object invoke(Object object, Method method, Object[] args) throws Throwable {
						Object invocationResult;
						try {
							// obtain the result of invoking the specified method on the Registry
							invocationResult = sendToRegistry(new MethodInvocationRequest(objectID, method, args));
						} catch (CommunicationException t) { // if the communication fails
							throw new MethodInvocationException(t);
						}
						if (invocationResult instanceof MethodInvocationException) {
							// if received a MethodInvocationException from the Registry
							throw (MethodInvocationException) invocationResult;
						}
						if (invocationResult instanceof IteratorProxy) { // if the invocation returned an iterator
							return createProxyIterator(((IteratorProxy) invocationResult).iteratorID());
						}
						return invocationResult;
					}
				});
	}

	/**
	 * Constructs a proxy Iterator.
	 * 
	 * @param iteratorID
	 *            the ID of the corresponding iterator on the Registry.
	 * @return a proxy Iterator.
	 */
	protected Iterator<Object> createProxyIterator(final int iteratorID) {

		return new Iterator<Object>() {

			/**
			 * The remaining objects from the Registry.
			 */
			LinkedList<Object> objectsFromRegsitry = null;

			/**
			 * A flag indicating whether or not this Iterator started getting data.
			 */
			protected boolean initialized = false;

			@Override
			public boolean hasNext() {
				initialize();
				return objectsFromRegsitry != null && objectsFromRegsitry.size() > 0;
			}

			@Override
			public Object next() {
				initialize();
				Object o = objectsFromRegsitry.remove(0);
				if (objectsFromRegsitry.size() == 0) { // if no objects from the Registry remain
					objectsFromRegsitry = getObjectsFromRegistry(); // get objects from the Registry
				}
				return o;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			/**
			 * Initializes this Iterator.
			 */
			protected void initialize() {
				if (!initialized) {
					initialized = true;
					objectsFromRegsitry = getObjectsFromRegistry();
				}
			}

			/**
			 * Gets objects from the Registry.
			 * 
			 * @return objects from the Registry; null if no more objects will be available from the Registry.
			 */
			protected LinkedList<Object> getObjectsFromRegistry() {
				long backoffInterval = 1;
				while (true) {
					try {
						@SuppressWarnings("unchecked")
						LinkedList<Object> objectsFromRepository = (LinkedList<Object>) sendToRegistry(new RequestForPrefetchedObjects(
								iteratorID));
						if (objectsFromRepository == null || objectsFromRepository.size() > 0)
							return objectsFromRepository;
						backoffInterval = Math.min(backoffInterval * 2, MAXIMUM_BACKOFF_INTERVAL);
						Thread.sleep(backoffInterval);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	/**
	 * Returns the number of bytes that this RegistryClient has sent.
	 * 
	 * @return the number of bytes that this RegistryClient has sent.
	 */
	public long bytesSent() {
		return bytesSent;
	}

	/**
	 * Returns the number of bytes that this RegistryClient has received.
	 * 
	 * @return the number of bytes that this RegistryClient has received.
	 */
	public long bytesReceived() {
		return bytesReceived;
	}

	/**
	 * Sends the specified object to the Registry.
	 * 
	 * @param object
	 *            the object to send to the Registry.
	 * @throws CommunicationException
	 *             if the communication fails.
	 */
	protected abstract Object sendToRegistry(Object object) throws CommunicationException;

}

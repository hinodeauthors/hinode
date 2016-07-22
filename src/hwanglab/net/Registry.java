package hwanglab.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import hwanglab.net.message.IteratorProxy;
import hwanglab.net.message.LookupRequest;
import hwanglab.net.message.MethodInvocationRequest;
import hwanglab.net.message.RequestForPrefetchedObjects;

/**
 * A Registry maintains a collection of objects that can be used by remote clients.
 * 
 * @author Jeong-Hyon Hwang (jhhwang@cs.albany.edu)
 */
public class Registry {

	/**
	 * The objects managed by this Registry.
	 */
	HashMap<String, Object> objects = new HashMap<String, Object>();

	/**
	 * The threads that prefetch data using an iterator.
	 */
	protected HashMap<Integer, Prefetcher> prefetchers = new HashMap<Integer, Prefetcher>();

	/**
	 * The number of iterators that this Registry has handled so far.
	 */
	protected int iteratorCount = 0;

	/**
	 * The number of bytes received so far.
	 */
	protected long bytesReceived = 0;

	/**
	 * The number of bytes sent so far.
	 */
	protected long bytesSent = 0;

	/**
	 * A Prefetcher prefetches data using an iterator.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public class Prefetcher extends Thread {

		/**
		 * The maximum number of data objects that can be buffered after prefetched.
		 */
		protected static final int MAX_BUFFER_SIZE = 10000;

		/**
		 * The prefetched data.
		 */
		protected LinkedList<Object> prefetched = new LinkedList<Object>();

		/**
		 * The iterator from which objects are prefetched.
		 */
		protected Iterator<?> iterator;

		/**
		 * A flag indicating whether or not this Prefetcher started using the input iterator.
		 */
		protected boolean started = false;

		/**
		 * Constructs a Prefetcher.
		 * 
		 * @param iterator
		 *            an iterator.
		 */
		public Prefetcher(Iterator<?> iterator) {
			this.iterator = iterator;
		}

		@Override
		public void run() {
			while (iterator.hasNext()) {
				synchronized (this) {
					prefetched.add(iterator.next());
					if (prefetched.size() > MAX_BUFFER_SIZE) { // if too many objects are buffered
						try {
							wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			iterator = null; // indicate the end of the input stream
		}

		/**
		 * Returns the prefetched objects.
		 * 
		 * @return the prefetched objects.
		 */
		public synchronized LinkedList<Object> prefetchedObjects() {
			if (!started) {
				started = true;
				start();
			}
			LinkedList<Object> old = prefetched;
			prefetched = new LinkedList<Object>();
			notify();
			if (old.size() == 0 && iterator == null)
				return null;
			else
				return old;
		}

	}

	/**
	 * Adds an object to this Registry.
	 * 
	 * @param objectID
	 *            the identifier of the object.
	 * @param object
	 *            the object to add.
	 */
	public void register(String objectID, Object object) {
		objects.put(objectID, object);
	}

	/**
	 * Removes the specified object.
	 * 
	 * @param objectID
	 *            the identifier of the object.
	 */
	public void remove(String objectID) {
		objects.remove(objectID);
	}

	/**
	 * Handles the specified object.
	 * 
	 * @param object
	 *            the object to handle.
	 * @return the result of handling the specified object.
	 */
	public Object handle(Object object) {
		if (object instanceof LookupRequest) { // lookup request
			object = handle((LookupRequest) object);
		} else if (object instanceof MethodInvocationRequest) { // method invocation
			object = handle((MethodInvocationRequest) object);
		} else if (object instanceof RequestForPrefetchedObjects) { // data request
			object = handle((RequestForPrefetchedObjects) object);
		}
		return object;
	}

	/**
	 * Handles the specified LookupRequest.
	 * 
	 * @param lookupRequest
	 *            the LookupRequest.
	 * @return true if the lookup request succeeds (i.e., the object to access has the type specified by the client);
	 *         false otherwise.
	 */
	protected boolean handle(LookupRequest lookupRequest) {
		String objectID = lookupRequest.objectID();
		Class<?> objectType = lookupRequest.objectType();
		Object object = objects.get(objectID);
		return objectType.isInstance(object); // true if the object to access has the type specified by the client.
	}

	/**
	 * Handles the specified MethodInvocationRequest.
	 * 
	 * @param r
	 *            the MethodInvocationRequest to process.
	 * @return the result of invocation; a MethodInvocationException if the method invocation fails.
	 */
	protected Object handle(MethodInvocationRequest r) {
		Object o = objects.get(r.objectID());
		try {
			Class<?> c = o.getClass();
			Object result = c.getMethod(r.methodName(), r.parameterTypes()).invoke(o, r.args());
			if (result instanceof Iterator) { // if the result is an iterator
				return createIteratorProxy((Iterator<?>) result);
			}
			return result; // return the result
		} catch (Throwable t) {
			return new MethodInvocationException(t);
		}
	}

	/**
	 * Handles the specified request for prefetched objects.
	 * 
	 * @param request
	 *            a request for prefetched objects.
	 * @return the prefetched objects.
	 */
	protected LinkedList<Object> handle(RequestForPrefetchedObjects request) {
		LinkedList<Object> r = prefetchers.get(request.iteratorID()).prefetchedObjects();
		if (r == null)
			prefetchers.remove(request.iteratorID());
		return r;
	}

	/**
	 * Creates a proxy for the specified iterator.
	 * 
	 * @param iterator
	 *            the iterator for which a proxy is constructed.
	 * @return a proxy for the specified iterator.
	 */
	protected IteratorProxy createIteratorProxy(Iterator<?> iterator) {
		synchronized (prefetchers) {
			IteratorProxy proxy = new IteratorProxy(iteratorCount);
			prefetchers.put(iteratorCount++, new Prefetcher(iterator));
			return proxy;
		}
	}

	/**
	 * Returns a byte array that represents the specified object.
	 * 
	 * @param object
	 *            the object for which a byte array is constructed.
	 * @return a byte array that represents the specified object.
	 * @throws IOException
	 *             if an error occurs when the object is written to a byte array.
	 */
	public static byte[] toByteArray(Object object) throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(b);
		out.writeObject(object);
		out.flush();
		return b.toByteArray();
	}

	/**
	 * Returns an object constructed from the specified byte array.
	 * 
	 * @param b
	 *            the byte array to use.
	 * @return an object constructed from the specified byte array.
	 * @throws IOException
	 *             if an error occurs when the object is constructed from the byte array.
	 * @throws ClassNotFoundException
	 *             if the class of a serialized object cannot be found.
	 */
	public static Object toObject(byte[] b) throws IOException, ClassNotFoundException {
		return new ObjectInputStream(new ByteArrayInputStream(b)).readObject();
	}

	/**
	 * Shuts down this Registry.
	 */
	public void shutdown() {
	}

	/**
	 * Returns the number of bytes sent so far.
	 * 
	 * @return the number of bytes sent so far.
	 */
	public long bytesSent() {
		return bytesSent;
	}

	/**
	 * Returns the number of bytes received so far.
	 * 
	 * @return the number of bytes received so far.
	 */
	public long bytesReceived() {
		return bytesReceived;
	}

}

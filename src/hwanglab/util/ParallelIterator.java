package hwanglab.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A ParallelIterator is an iterator over objects obtained from multiple iterators in parallel.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 * @param <T>
 *            the type of the objects.
 */
public class ParallelIterator<T> implements Iterator<T> {

	/**
	 * The objects obtained from the input iterators.
	 */
	protected LinkedList<T> buffer = new LinkedList<T>();

	/**
	 * The threads that obtain objects from input iterators.
	 */
	HashSet<Thread> readerThreads = new HashSet<Thread>();

	/**
	 * A flag indicating whether or not this ParallelIterator has started reader threads.
	 */
	protected boolean started = false;

	/**
	 * Constructs a ParallelIterator that iterates over the objects obtained from the specified iterators.
	 * 
	 * @param iterators
	 *            the iterators from which objects will be obtained.
	 */
	public ParallelIterator(Collection<Iterator<T>> iterators) {
		for (final Iterator<T> i : iterators) {
			Thread t = new Thread() { // create a Thread that inserts data from i to the buffer
				public void run() {
					try {
						while (i.hasNext()) { // TODO: congestion control
							synchronized (buffer) {
								buffer.add(i.next());
								buffer.notify();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					synchronized (readerThreads) {
						readerThreads.remove(this);
					}
					if (readerThreads.size() == 0) {
						synchronized (buffer) {
							buffer.notify();
						}
					}
				}
			};
			readerThreads.add(t);
		}
	}

	@Override
	public boolean hasNext() {
		waitForObjects();
		return buffer.size() > 0;
	}

	@Override
	public T next() {
		waitForObjects();
		synchronized (buffer) {
			return buffer.remove();
		}
	}

	/**
	 * Waits until new objects are obtained from
	 */
	protected void waitForObjects() {
		synchronized (buffer) {
			if (!started) {
				started = true;
				synchronized (readerThreads) {
					for (Thread t : readerThreads) { // start each thread
						t.start();
					}
				}
			}
			if (buffer.size() == 0 && readerThreads.size() > 0) {
				try {
					buffer.wait();
				} catch (InterruptedException e) {
					// e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}

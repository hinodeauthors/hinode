package hwanglab.util;

import java.util.Iterator;
import java.util.Vector;

/**
 * A MergeIterator iterates over the objects from multiple iterators in an iterator-by-iterator manner.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * @param <T>
 *            the type of the objects.
 */
public class MergeIterator<T> implements Iterator<T> {

	/**
	 * The input iterators.
	 */
	Vector<Iterator<T>> iterators = new Vector<Iterator<T>>();

	/**
	 * Adds an input iterator.
	 * 
	 * @param iterator
	 *            an input iterator.
	 */
	public synchronized void add(Iterator<T> iterator) {
		if (iterator.hasNext()) {
			iterators.add(iterator);
		}
	}

	@Override
	public synchronized boolean hasNext() {
		return iterators.size() > 0 && iterators.elementAt(0).hasNext();
	}

	@Override
	public synchronized T next() {
		try {
			Iterator<T> iterator = iterators.elementAt(0);
			T next = iterator.next();
			if (!iterator.hasNext())
				iterators.removeElementAt(0);
			return next;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void remove() {
	}

}

package gstar.query.operator;

import hwanglab.data.DataObject;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A SortOperator sorts the obtained DataObjects.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class SortOperator extends Operator<DataObject, DataObject> {

	/**
	 * A sorted set of the obtained DataObjects.
	 */
	protected SortedSet<DataObject> sorted;

	/**
	 * An iterator over the obtained DataObjects.
	 */
	protected Iterator<DataObject> iterator = null;

	/**
	 * Constructs a SortOperator.
	 * 
	 * @param attributes
	 *            the attributes on which the DataObjects are sorted.
	 */
	public SortOperator(String[] attributes) {
		sorted = new TreeSet<DataObject>(comparator(attributes));
	}

	@Override
	protected void init() {
		while (input(0).hasNext()) {
			sorted.add(input(0).next());
		}
	}

	@Override
	public boolean hasNext() {
		if (iterator == null)
			iterator = sorted.iterator();
		return iterator.hasNext();
	}

	@Override
	public DataObject next() {
		if (iterator == null)
			iterator = sorted.iterator();
		return iterator.next();
	}

}

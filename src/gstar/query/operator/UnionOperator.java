package gstar.query.operator;

import hwanglab.data.DataObject;
import hwanglab.util.ParallelIterator;

/**
 * A UnionOperator merges multiple input streams into an output stream.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class UnionOperator extends Operator<DataObject, DataObject> {

	/**
	 * The ParallelIterator used for this UnionOperator.
	 */
	protected ParallelIterator<DataObject> iterator;

	@Override
	protected void init() {
		iterator = new ParallelIterator<DataObject>(inputs);
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public DataObject next() {
		return iterator.next();
	}

}

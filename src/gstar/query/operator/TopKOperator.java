package gstar.query.operator;

import gstar.query.summary.Aggregate;
import gstar.query.summary.Summary;
import gstar.query.summary.TopK;
import hwanglab.data.DataObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A TopKOperator outputs the top-k DataObjects.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class TopKOperator extends Operator<DataObject, DataObject> {

	/**
	 * The number of DataObjects to output.
	 */
	protected int count;

	/**
	 * The attributes on which the DataObjects are sorted.
	 */
	protected String[] sortAttributes;

	/**
	 * The grouping attributes.
	 */
	protected String[] groupingAttributes;

	/**
	 * The result DataObjects.
	 */
	LinkedList<DataObject> result = new LinkedList<DataObject>();

	/**
	 * Constructs a TopKOperator.
	 * 
	 * @param count
	 *            number of DataObjects to output.
	 * @param sortAttributes
	 *            attributes on which the DataObjects are sorted.
	 * @param groupingAttributes
	 *            the arguments to use to construct an Aggregate.
	 */
	public TopKOperator(String count, String[] sortAttributes, String[] groupingAttributes) {
		this.count = Integer.parseInt(count);
		this.sortAttributes = sortAttributes;
		this.groupingAttributes = groupingAttributes;
	}

	@Override
	protected void init() {
		ArrayList<Summary<DataObject, Collection<DataObject>>> summaries = new ArrayList<Summary<DataObject, Collection<DataObject>>>();
		summaries.add(new TopK(count, comparator(sortAttributes)));
		Aggregate<ArrayList<Object>, DataObject, Collection<DataObject>> aggregate = new Aggregate<ArrayList<Object>, DataObject, Collection<DataObject>>(
				summaries);
		while (input(0).hasNext()) { // consumes all the input data while updating the aggregate.
			DataObject o = input(0).next();
			Set<ArrayList<Object>> groupValues = o.compositeValues(groupingAttributes);
			for (String groupingAttribute : groupingAttributes) {
				o.remove(groupingAttribute);
			}
			aggregate.update(new DataObject[] { o }, groupValues);
		}
		Entry<ArrayList<Object>, ArrayList<Summary<DataObject, Collection<DataObject>>>> next = null;
		while ((next = aggregate.removeFirst()) != null) {
			Object[] gVals = next.getKey().toArray();
			for (Summary<DataObject, Collection<DataObject>> summary : next.getValue()) {
				for (DataObject d : summary.value()) {
					DataObject newD = new DataObject(d);
					for (int j = 0; j < gVals.length; j++)
						newD.update(groupingAttributes[j], gVals[j]);
					result.add(newD);
				}
			}
		}
	}

	@Override
	public boolean hasNext() {
		return result.size() > 0;
	}

	@Override
	public DataObject next() {
		return result.removeFirst();
	}

}

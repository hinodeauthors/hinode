package gstar.query.operator;

import gstar.query.summary.Average;
import gstar.query.summary.Count;
import gstar.query.summary.Maximum;
import gstar.query.summary.Minimum;
import gstar.query.summary.Sum;
import gstar.query.summary.Summary;
import hwanglab.data.DataObject;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Set;

/**
 * An AggregateOperator produces an aggregate value for each group.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class AggregateOperator extends Operator<DataObject, DataObject> {

	/**
	 * The Aggregate that this AggregateOperator manages.
	 */
	protected gstar.query.summary.Aggregate<ArrayList<Object>, Object, Object> aggregate;

	/**
	 * The attributes that are aggregated.
	 */
	protected String[] inputAttributes;

	/**
	 * The attributes for storing the aggregation results.
	 */
	protected String[] outputAttributes;

	/**
	 * The grouping attributes.
	 */
	String[] groupingAttributes;

	/**
	 * Constructs an AggregateOperator.
	 * 
	 * @param aggregateFunctionNames
	 *            the names of aggregate functions.
	 * @param inputAttributes
	 *            the attributes that are aggregated.
	 * @param outputAttributes
	 *            the attributes for storing the aggregation results.
	 * @param groupingAttributes
	 *            the grouping attributes.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AggregateOperator(String[] aggregateFunctionNames, String[] inputAttributes, String[] outputAttributes,
			String[] groupingAttributes) {
		this.inputAttributes = inputAttributes;
		this.outputAttributes = outputAttributes;
		this.groupingAttributes = groupingAttributes;
		aggregate = new gstar.query.summary.Aggregate((aggregateFunctions(aggregateFunctionNames)));
	}

	/**
	 * Returns an aggregate function.
	 * 
	 * @param aggregationFunctionName
	 *            the name of the aggregate function.
	 * @return an Aggregate based on the specified arguments.
	 */
	@SuppressWarnings({ "rawtypes" })
	protected Summary aggregateFunction(String aggregationFunctionName) {
		if (aggregationFunctionName.equals("count"))
			return new Count();
		if (aggregationFunctionName.equals("sum"))
			return new Sum();
		if (aggregationFunctionName.equals("min"))
			return new Minimum();
		if (aggregationFunctionName.equals("max"))
			return new Maximum();
		if (aggregationFunctionName.equals("avg"))
			return new Average();
		throw new UnsupportedOperationException(aggregationFunctionName);
	}

	/**
	 * Returns an array of aggregate functions.
	 * 
	 * @return the names of the aggregate functions.
	 */
	@SuppressWarnings({ "rawtypes" })
	protected ArrayList<Summary> aggregateFunctions(String[] aggregateFunctionNames) {
		ArrayList<Summary> summaries = new ArrayList<Summary>(aggregateFunctionNames.length);
		for (int i = 0; i < aggregateFunctionNames.length; i++) {
			summaries.add(aggregateFunction(aggregateFunctionNames[i]));
		}
		return summaries;
	}

	@Override
	protected void init() {
		while (input(0).hasNext()) { // consume all input objects.
			DataObject o = input(0).next();
			Set<ArrayList<Object>> attributeValues = o.compositeValues(inputAttributes);
			Set<ArrayList<Object>> groupValues = o.compositeValues(groupingAttributes);
			for (ArrayList<Object> a : attributeValues) {
				aggregate.update(a.toArray(), groupValues);
			}
		}
	}

	@Override
	public boolean hasNext() {
		return !aggregate.isEmpty();
	}

	@Override
	public DataObject next() {
		Entry<ArrayList<Object>, ArrayList<Summary<Object, Object>>> entry = aggregate.removeFirst();
		DataObject o = new DataObject();
		Object[] groupValues = entry.getKey().toArray();
		for (int i = 0; i < groupValues.length; i++)
			// add the grouping attributes to the the current object.
			o.update(groupingAttributes[i], groupValues[i]);
		ArrayList<Summary<Object, Object>> summaries = entry.getValue();
		for (int i = 0; i < outputAttributes.length; i++)
			// add the aggregation results to the current object.
			o.update(outputAttributes[i], summaries.get(i).value());
		return o;
	}

}

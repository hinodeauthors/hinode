package gstar.query.operator;

import hwanglab.data.DataObject;
import hwanglab.util.StringTokenizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * A JoinOperator produces an output stream containing objects containing values from two input streams.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class JoinOperator extends Operator<DataObject, DataObject> {

	/**
	 * An iterator over DataObjects obtained from the right input.
	 */
	private Iterator<DataObject> right;

	/**
	 * The join attributes for the left input.
	 */
	protected String[] leftJoinAttributes;

	/**
	 * The join attributes for the right input.
	 */
	protected String[] rightJoinAttributes;

	/**
	 * The index that contains values from the left input.
	 */
	private HashMap<HashSet<Object>, DataObject> joinIndex = new HashMap<HashSet<Object>, DataObject>();

	/**
	 * The next output DataObject.
	 */
	DataObject next = null;

	/**
	 * Constructs a JoinOperator.
	 * 
	 * @param leftJoinAttributes
	 *            the join attributes for the left input.
	 * @param rightJoinAttributes
	 *            the join attributes for the right input.
	 */
	public JoinOperator(String[] leftJoinAttributes, String[] rightJoinAttributes) {
		this.leftJoinAttributes = leftJoinAttributes;
		this.rightJoinAttributes = rightJoinAttributes;
	}

	@Override
	protected void init() {
		Iterator<DataObject> left = input(0);
		while (left.hasNext()) {
			DataObject l = left.next();
			joinIndex.put(joinAttributeValues(l, leftJoinAttributes), l);
		}
		right = input(1);
	}

	/**
	 * Returns the values of the specified attributes from the specified DataObject.
	 * 
	 * @param o
	 *            a DataObject.
	 * @param joinAttributes
	 *            join attributes.
	 * @return the values of the specified attributes from the specified DataObject.
	 */
	private HashSet<Object> joinAttributeValues(DataObject o, String[] joinAttributes) {
		LinkedHashSet<Object> key = new LinkedHashSet<Object>();
		for (int i = 0; i < joinAttributes.length; i++) {
			Object val = o.value(joinAttributes[i]);
			if (!(val instanceof Integer) && !(val instanceof Double))
				try {
					val = StringTokenizer.str2Number(val.toString());
				}
				catch(Exception e) {
					val = val.toString();
				}
			key.add(val);
		}
		return key;
	}

	@Override
	public boolean hasNext() {
		if (next == null)
			prepareNextResult();
		return next != null;
	}

	@Override
	public DataObject next() {
		DataObject r = next;
		next = null;
		return r;
	}

	/**
	 * Prepares the next output DataObject.
	 */
	private void prepareNextResult() {
		while (right.hasNext()) {
			DataObject r = right.next();
			DataObject l;
			l = joinIndex.get(joinAttributeValues(r, rightJoinAttributes));
			if (l != null) {
				next = new DataObject();
				extend(next, "left.", l);
				extend(next, "right.", r);
				return;
			}
		}
	}

	/**
	 * Updates the specified DataObject using the attributes of another DataObject.
	 * 
	 * @param o
	 *            a DataObject.
	 * @param prefix
	 *            a prefix.
	 * @param other
	 *            another DataObject.
	 */
	private void extend(DataObject o, String prefix, DataObject other) {
		LinkedList<String> attributes = new LinkedList<String>(other.attributeNames());
		for (String attribute : attributes) {
			o.update(add(prefix, attribute), other.value(attribute));
		}
	}

	/**
	 * Adds the specified prefix to the specified string.
	 * 
	 * @param prefix
	 *            a prefix.
	 * @param str
	 *            a string.
	 * @return the result of adding the specified prefix to the specified string.
	 */
	protected String add(String prefix, String str) {
		if (str.contains("{")) {
			return "{" + prefix + (str.replace("{", "").replace("}", "")) + "}";
		}
		return prefix + str;
	}

}

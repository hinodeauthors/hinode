package hwanglab.data;

import hwanglab.expression.ArithmeticExpression;
import hwanglab.expression.UnboundVariableException;
import hwanglab.expression.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.HashSet;
import java.util.Set;

/**
 * A DataObject represents an object that can contain an arbitrary number of single-valued and multi-valued attributes.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class DataObject implements java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 113272530279206273L;

	/**
	 * The attributes.
	 */
	protected LinkedHashMap<String, Object> attributes = new LinkedHashMap<String, Object>(0);

	/**
	 * Constructs a DataObject.
	 */
	public DataObject() {
	}

	/**
	 * Constructs a DataObject as a clone of the specified DataObject.
	 * 
	 * @param other
	 *            another DataObject.
	 */
	public DataObject(DataObject other) {
		for (Map.Entry<String, Object> e : other.attributes.entrySet()) {
			update(e.getKey(), e.getValue());
		}
	}

	/**
	 * Returns true only if this DataObject is empty.
	 * 
	 * @return true if this DataObject is empty; false otherwise.
	 */
	public boolean isEmpty() {
		return attributes.isEmpty();
	}

	/**
	 * Updates this DataObject based on the specified attribute.
	 * 
	 * @param attributeName
	 *            the name of the attribute.
	 * @param attributeValue
	 *            the new value of the attribute; if null the specified attribute is removed from this DataObject.
	 * @return the previous value of the specified attribute; null if none.
	 */
	public Object update(String attributeName, Object attributeValue) {
		if (attributeValue == null)
			return attributes.remove(attributeName);
		else
			return attributes.put(attributeName, attributeValue);
	}

	/**
	 * Updates this DataObject according to the specified DataObjectUpdateMessage.
	 * 
	 * @param message
	 *            the DataObjectUpdateMessage to consume.
	 */
	public void update(DataObjectUpdateMessage message) {
		if (message.resetScheduled())
			attributes.clear();
		for (Map.Entry<String, Object> e : message.attributes()) {
			update(e.getKey(), e.getValue());
		}
	}

	/**
	 * Determines whether or not this DataObject has the specified attribute.
	 * 
	 * @param attribute
	 *            the name of the attribute.
	 * @return true if this DataObject has the specified attribute; false otherwise.
	 */
	public boolean hasAttribute(String attribute) {
		return attributes.containsKey(attribute);
	}

	/**
	 * Returns the value of the specified attribute.
	 * 
	 * @param attribute
	 *            the name of the attribute.
	 * @return the value of the specified attribute.
	 */
	public Object value(String attribute) {
		return attributes.get(attribute);
	}

	/**
	 * Removes the specified attribute from this DataObject.
	 * 
	 * @param attribute
	 *            the name of the attribute to remove.
	 * @return the old value of the removed attribute.
	 */
	public Object remove(String attribute) {
		return attributes.remove(attribute);
	}

	/**
	 * Returns the attribute names.
	 * 
	 * @return the attribute names.
	 */
	public Collection<? extends String> attributeNames() {
		return attributes.keySet();
	}

	/**
	 * Returns the attribute values.
	 * 
	 * @return the attribute values.
	 */
	public Collection<Object> values() {
		return attributes.values();
	}

	/**
	 * Returns an Iterable over the attributes.
	 * 
	 * @return an Iterable over the attributes.
	 */
	public Iterable<Map.Entry<String, Object>> attributes() {
		return attributes.entrySet();
	}

	@Override
	public String toString() {
		return attributes.toString();
	}

	/**
	 * Returns a set of values that are extracted from the specified attributes.
	 * 
	 * @param attributes
	 *            the attributes of interest.
	 * @return a set of values that are extracted from the specified attributes.
	 */
	@SuppressWarnings("unchecked")
	public Set<ArrayList<Object>> compositeValues(String[] attributes) {
		Vector<Set<Object>> values = new Vector<Set<Object>>();
		for (int i = 0; i < attributes.length; i++) {
			Object val = this.attributes.get(attributes[i]);
			if (val instanceof Set) {
				values.add((Set<Object>) val);
			} else {
				HashSet<Object> s = new HashSet<Object>();
				s.add(val);
				values.add(s);
			}
		}
		Set<ArrayList<Object>> compositeValues = new HashSet<ArrayList<Object>>();
		for (int i = 0; i < values.size(); i++) {
			if (i == 0)
				for (Object o : values.elementAt(i)) {
					ArrayList<Object> result = new ArrayList<Object>(attributes.length);
					result.add(0, o);
					compositeValues.add(result);
				}
			else {
				Set<ArrayList<Object>> newCompositeValues = new HashSet<ArrayList<Object>>();
				for (Object o : values.elementAt(i)) {
					for (ArrayList<Object> result : compositeValues) {
						ArrayList<Object> newResult = new ArrayList<Object>(result);
						newResult.add(i, o);
						newCompositeValues.add(newResult);
					}
				}
				compositeValues = newCompositeValues;
			}
		}
		return compositeValues;
	}

	/**
	 * Evaluates the specified ArithmeticExpression using the attribute values of this DataObject.
	 * 
	 * @param expression
	 *            an ArithmeticExpression.
	 * @return the result of evaluating the specified ArithmeticExpression.
	 * @throws UnboundVariableException
	 *             if the ArithmeticExpression contains a variable whose value is not set.
	 */
	public Object evaluate(ArithmeticExpression expression) throws UnboundVariableException {
		for (Variable v : expression.variables()) {
			v.set(value(v.name()));
		}
		return expression.evaluate();
	}

}

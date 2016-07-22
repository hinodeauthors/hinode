package gstar.data;

import hwanglab.data.DataObject;

/**
 * An Edge represents a graph edge.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Edge extends DataObject {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 4560335146311513882L;

	/**
	 * The ID of the vertex that this Edge is incident to.
	 */
	protected VID otherEnd;

	/**
	 * The weight of this Edge.
	 */
	protected double weight = 1.0;

	/**
	 * Constructs an Edge.
	 * 
	 * @param otherEnd
	 *            the ID of the vertex on the other end of this Edge.
	 */
	public Edge(VID otherEnd) {
		this.otherEnd = otherEnd;
	}

	/**
	 * Returns the ID of the vertex on the other end of this Edge.
	 * 
	 * @return the ID of the vertex on the other end of this Edge.
	 */
	public VID otherEnd() {
		return otherEnd;
	}

	/**
	 * Returns the weight of this Edge.
	 * 
	 * @return the weight of this Edge.
	 */
	public double weight() {
		return weight;
	}

	@Override
	public Object update(String attributeName, Object value) {
		if (attributeName.equals("weight")) {
			Object oldWeight = weight;
			weight = (Double) value;
			return oldWeight;
		}
		return super.update(attributeName, value);
	}

	@Override
	public Object value(String attributeName) {
		if (attributeName.equals("weight")) {
			return weight;
		} else
			return super.value(attributeName);
	}

	@Override
	public String toString() {
		String s = super.toString();
		return "{" + "weight=" + weight + (s.length() == 2 ? "" : ", ") + s.substring(1);
	}

}
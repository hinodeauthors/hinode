package hinode.util;

import gstar.data.VID;

public class EdgePair implements java.io.Serializable 
{
	private static final long serialVersionUID = 353438584850653266L;

	/**
	 * The first value.
	 */
	VID first;

	/**
	 * The second value.
	 */
	Interval second;

	/**
	 * Constructs a Pair.
	 * 
	 * @param first
	 *            the first value.
	 * @param second
	 *            the second value.
	 */
	public EdgePair(VID first, Interval second) 
	{
		this.first = first;
		this.second = second;
	}

	/**
	 * Returns the first value.
	 * 
	 * @return the first value.
	 */
	public VID first() 
	{
		return first;
	}

	/**
	 * Returns the second value.
	 * 
	 * @return the second value.
	 */
	public Interval second() 
	{
		return second;
	}

	@Override
	public String toString() 
	{
		return "(" + first + ", " + second + ")";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof EdgePair)) {
			return false;
		}
		EdgePair other = (EdgePair) obj;
		if (first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!first.equals(other.first)) {
			return false;
		}
		if (second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!second.equals(other.second)) {
			return false;
		}
		return true;
	}
}

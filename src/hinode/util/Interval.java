package hinode.util;

public class Interval implements java.io.Serializable 
{
	private static final long serialVersionUID = 7172351916261480713L;
	protected int start;
	protected int end;
	
	public Interval()
	{
		start = Integer.MIN_VALUE;
		end = Integer.MAX_VALUE;
	}
	
	public Interval(int start)
	{
		this.start = start;
		this.end = Integer.MAX_VALUE;
	}
	
	public Interval(int start, int end)
	{
		this.start = start;
		this.end = end;
	}

	public int getStart() 
	{
		return start;
	}

	public void setStart(int start) 
	{
		this.start = start;
	}

	public int getEnd() 
	{
		return end;
	}

	public void setEnd(int end) 
	{
		this.end = end;
	}
	
	// Checks if n is inside the interval
	public boolean stab (int n)
	{
		return (start <= n) && (n < end) ? true : false;
	}
	
	public String toString()
	{
		return "[" + start + "," + end + ")";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + start;
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
		if (!(obj instanceof Interval)) {
			return false;
		}
		Interval other = (Interval) obj;
		if (end != other.end) {
			return false;
		}
		if (start != other.start) {
			return false;
		}
		return true;
	}
}

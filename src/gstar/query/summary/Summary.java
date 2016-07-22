package gstar.query.summary;

/**
 * The Summary class implements basic functionalities for a variety of summaries including maxima and histograms.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 * @param <V>
 *            The type of the values for updating the Summary.
 * @param <F>
 *            The type of the summary value obtained from the Summary.
 */
public interface Summary<V, F> extends java.io.Serializable {

	/**
	 * Updates this Summary based on the specified value.
	 * 
	 * @param v
	 *            the value for updating this Summary.
	 */
	public boolean update(V v);

	/**
	 * Updates this Summary based on the specified Summary.
	 * 
	 * @param summary
	 *            the summary for updating this Summary.
	 */
	public boolean update(Summary<V, F> summary);

	/**
	 * The summary value obtained from this Summary.
	 * 
	 * @return the final summary value.
	 */
	public F value();

	/**
	 * Constructs a clone of this Summary.
	 * 
	 * @return the constructed clone of this Summary.
	 */
	public Summary<V, F> clone();

}

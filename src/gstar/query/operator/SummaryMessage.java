package gstar.query.operator;

import gstar.data.GID;
import gstar.data.VID;
import gstar.query.summary.Summary;

import java.util.Set;

/**
 * A SummaryMessage contains a summary, the ID of target vertex and the IDs of the related graphs.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 * @param <I>
 *            The input type of the summary.
 * @param <O>
 *            The output type of the summary.
 */
public class SummaryMessage<I, O> implements java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 2852433160589324177L;

	/**
	 * The ID of the vertex related to the summary.
	 */
	VID targetVertexID;

	/**
	 * The summary that this SummaryMessage contains.
	 */
	Summary<I, O> summary;

	/**
	 * The ID of the vertex to use to update the summary.
	 */
	VID intermediateVertexID;

	/**
	 * The the IDs of the graphs related to the summary.
	 */
	Set<GID> graphIDs;

	/**
	 * Constructs a SummaryMessage.
	 * 
	 * @param targetVertexID
	 *            the ID of the vertex to be associated with the summary.
	 * @param summary
	 *            the summary to be contained in the SummaryMessage.
	 * @param intermediateVertexID
	 *            the ID of the vertex to visit to update the summary.
	 * @param graphIDs
	 *            the IDs of the related graphs.
	 */
	public SummaryMessage(VID targetVertexID, Summary<I, O> summary, VID intermediateVertexID, Set<GID> graphIDs) {
		this.targetVertexID = targetVertexID;
		this.summary = summary;
		this.intermediateVertexID = intermediateVertexID;
		this.graphIDs = graphIDs;
	}

	/**
	 * Returns a copy of this SummaryMessage.
	 * 
	 * @param graphIDs
	 *            the IDs of the related graphs.
	 * @return a copy of this SummaryMessage.
	 */
	public SummaryMessage<I, O> clone(Set<GID> graphIDs) {
		return new SummaryMessage<I, O>(targetVertexID, summary, intermediateVertexID, graphIDs);
	}

	@Override
	public String toString() {
		return "(" + targetVertexID + " " + summary + " " + intermediateVertexID + " " + graphIDs + ")";
	}

	/**
	 * Returns the summary that this SummaryMessage contains.
	 * 
	 * @return the summary that this SummaryMessage contains.
	 */
	public Summary<I, O> summary() {
		return summary;
	}

	/**
	 * Returns the ID of the target vertex (i.e., the vertex to eventually be associated with the summary).
	 * 
	 * @return the ID of the target vertex (i.e., the vertex to eventually be associated with the summary).
	 */
	public VID targetVertexID() {
		return targetVertexID;
	}

	/**
	 * Returns the IDs of the related graphs.
	 * 
	 * @return the IDs of the related graphs.
	 */
	public Set<GID> graphIDs() {
		return graphIDs;
	}

	/**
	 * Returns the ID of the vertex to visit before the summary is sent to the target vertex.
	 * 
	 * @return the ID of the vertex to visit before the summary is sent to the target vertex.
	 */
	public VID intermediateVertexID() {
		return intermediateVertexID;
	}

	/**
	 * Sets the ID of the vertex to visit before the summary is sent to the target vertex.
	 * 
	 * @param v
	 *            the ID of the vertex to visit before the summary is sent to the target vertex.
	 */
	public void setIntermediateVertexID(VID v) {
		this.intermediateVertexID = v;
	}

}

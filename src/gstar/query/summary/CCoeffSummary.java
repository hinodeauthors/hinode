package gstar.query.summary;

import gstar.data.Edge;
import gstar.data.VID;
import gstar.data.Vertex;

import java.util.Set;

/**
 * A CCoeffSummary maintains values for computing the clustering coefficient of a Vertex.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class CCoeffSummary implements Summary<Vertex, Double> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -5286019823979579138L;

	/**
	 * The IDs of the neighbor Vertices of the Vertex whose clustering coefficient is computed.
	 */
	protected Set<VID> neighborIDs;

	/**
	 * The number of Edges between the neighbors of the Vertex whose clustering coefficient is computed.
	 */
	protected int triangles;

	/**
	 * Constructs a ClusteringCoefficientSummary.
	 */
	public CCoeffSummary() {
		this(null, 0);
	}

	/**
	 * Constructs a ClusteringCoefficientSummary.
	 * 
	 * @param neighborIDs
	 *            the IDs of the neighbor Vertices.
	 */
	public CCoeffSummary(Set<VID> neighborIDs) {
		this(neighborIDs, 0);
	}

	/**
	 * Constructs a ClusteringCoefficientSummary based on the specified ClusteringCoefficientSummary.
	 * 
	 * @param summary
	 *            the ClusteringCoefficientSummary to consume.
	 */
	public CCoeffSummary(CCoeffSummary summary) {
		this(summary.neighborIDs, summary.triangles);
	}

	/**
	 * Constructs a ClusteringCoefficientSummary based on the specified data.
	 * 
	 * @param neighborIDs
	 *            the IDs of the neighbor vertices.
	 * @param triangles
	 *            the number of Edges between the neighbors of the Vertex whose clustering coefficient is computed.
	 */
	protected CCoeffSummary(Set<VID> neighborIDs, int triangles) {
		this.neighborIDs = neighborIDs;
		this.triangles = triangles;
	}

	@Override
	public boolean update(Vertex v) {
		boolean updated = false;
		for (Edge e : v.outgoingEdges())
			if (neighborIDs.contains(e.otherEnd())) {
				triangles++;
				updated = true;
			}
		return updated;
	}

	@Override
	public Double value() {
		return 1.0 * triangles / neighborIDs.size() / (neighborIDs.size() - 1);
	}

	@Override
	public String toString() {
		return "(" + neighborIDs + ", " + triangles + ")";
	}

	@Override
	public boolean update(Summary<Vertex, Double> summary) {
		update((CCoeffSummary) summary);
		return true;
	}

	/**
	 * Updates this CCoeffSummary based on the specified CCoeffSummary.
	 * 
	 * @param summary
	 *            the CCoeffSummary to consume.
	 */
	protected void update(CCoeffSummary summary) {
		if (this.neighborIDs == null) {
			this.neighborIDs = summary.neighborIDs;
			this.triangles = summary.triangles;
		} else
			triangles += ((CCoeffSummary) summary).triangles;
	}

	@Override
	public Summary<Vertex, Double> clone() {
		return new CCoeffSummary(this);
	}

}

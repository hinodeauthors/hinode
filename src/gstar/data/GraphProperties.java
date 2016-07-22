package gstar.data;



/**
 * A GraphProperties instance represents the properties of a Graph.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class GraphProperties implements java.io.Serializable {

	/**
	 * Automatically generated serial version UID.
	 */
	private static final long serialVersionUID = -1814346352678654291L;

	/**
	 * The absolute path to the GraphDirectory that contains the Graph.
	 */
	protected GraphPath absolutePath;

	/**
	 * The ID of the Graph.
	 */
	protected GID graphID;

	/**
	 * The number of vertices contained in the Graph.
	 */
	protected int numVertices;

	/**
	 * The number of edges contained in the Graph.
	 */
	protected int numEdges;

	/**
	 * Constructs a GraphProperties instance.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory that contains the Graph.
	 * @param graphID
	 *            the ID of the Graph.
	 * @param numVertices
	 *            the number of vertices contained in the Graph.
	 * @param numEdges
	 *            the number of edges contained in the Graph.
	 */
	public GraphProperties(GraphPath absolutePath, GID graphID, int numVertices, int numEdges) {
		this.absolutePath = absolutePath;
		this.graphID = graphID;
		this.numVertices = numVertices;
		this.numEdges = numEdges;
	}

	/**
	 * Returns the absolute path to the GraphDirectory that contains the Graph.
	 * 
	 * @return the absolute path to the GraphDirectory that contains the Graph.
	 */
	public GraphPath absolutePath() {
		return absolutePath;
	}

	/**
	 * Returns the ID of the Graph.
	 * 
	 * @return the ID of the Graph.
	 */
	public GID graphID() {
		return graphID;
	}

	/**
	 * Returns the number of vertices contained in the Graph.
	 * 
	 * @return the number of vertices contained in the Graph.
	 */
	public int numVertices() {
		return numVertices;
	}

	/**
	 * Returns the number of edges contained in the Graph.
	 * 
	 * @return the number of edges contained in the Graph.
	 */
	public int numEdges() {
		return numEdges;
	}

	/**
	 * Updates this GraphProperties using the specified GraphProperties.
	 * 
	 * @param other
	 *            another GraphProperties.
	 */
	public void update(GraphProperties other) {
		numVertices += other.numVertices;
		numEdges += other.numEdges;
	}

	@Override
	public String toString() {
		return numVertices + " vertices and " + numEdges + " edges";
	}

}

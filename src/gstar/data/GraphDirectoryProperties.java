package gstar.data;


/**
 * A GraphDirectoryProperties instance represents the properties of a GraphDirectory.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class GraphDirectoryProperties implements java.io.Serializable {

	/**
	 * Automatically generated serial version UID.
	 */
	private static final long serialVersionUID = -8220866652406332459L;

	/**
	 * The absolute path to the GraphDirectory.
	 */
	protected GraphPath absolutePath;

	/**
	 * The number of graphs stored in the GraphDirectory.
	 */
	protected int numGraphs;

	/**
	 * The number of Workers that store the data in the GraphDirectory.
	 */
	protected int numWorkers;

	/**
	 * The overall data size of the GraphDirectory.
	 */
	protected long dataSize;

	/**
	 * The overall index size of the GraphDirectory.
	 */
	protected long indexSize;

	/**
	 * Constructs a GraphDirectoryProperties instance.
	 * 
	 * @param path
	 *            the path to the GraphDirectory.
	 * @param numGraphs
	 *            the number of graphs stored in the GraphDirectory.
	 * @param numWorkers
	 *            the number of Workers that store the data in the GraphDirectory.
	 * @param dataSize
	 *            the overall data size of the GraphDirectory.
	 * @param indexSize
	 *            the overall index size of the GraphDirectory.
	 */
	public GraphDirectoryProperties(GraphPath path, int numGraphs, int numWorkers, long dataSize, long indexSize) {
		this.absolutePath = path;
		this.numGraphs = numGraphs;
		this.numWorkers = numWorkers;
		this.dataSize = dataSize;
		this.indexSize = indexSize;
	}

	/**
	 * Returns the absolute path to the GraphDirectory.
	 * 
	 * @return the absolute path to the GraphDirectory.
	 */
	public GraphPath absolutePath() {
		return absolutePath;
	}

	/**
	 * Returns the number of graphs in the GraphDirectory.
	 * 
	 * @return the number of graphs in the GraphDirectory.
	 */
	public int numGraphs() {
		return numGraphs;
	}

	/**
	 * Returns the number of Workers that store the data in the GraphDirectory.
	 * 
	 * @return the number of Workers that store the data in the GraphDirectory.
	 */
	public int numWorkers() {
		return numWorkers;
	}

	/**
	 * Returns the overall data size of the GraphDirectory.
	 * 
	 * @return the overall data size of the GraphDirectory.
	 */
	public long dataSize() {
		return dataSize;
	}

	/**
	 * Returns the overall index size of the GraphDirectory.
	 * 
	 * @return the overall index size of the GraphDirectory.
	 */
	public long indexSize() {
		return indexSize;
	}

	/**
	 * Updates this GraphDirectoryProperties instance using another one.
	 * 
	 * @param other
	 *            another GraphDirectoryProperties instance.
	 */
	public void update(GraphDirectoryProperties other) {
		numGraphs = Math.max(numGraphs, other.numGraphs);
		dataSize += other.dataSize;
		indexSize += other.indexSize;
	}

	@Override
	public String toString() {
		return numGraphs + " graphs on " + numWorkers + " workers";
	}

}

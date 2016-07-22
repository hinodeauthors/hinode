package gstar.client;

import gstar.data.GID;
import gstar.data.VID;
import gstar.data.VertexUpdateMessage;
import hwanglab.data.DataObjectUpdateMessage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * A GraphUpdateMessage contains information for updating a Graph.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class GraphUpdateMessage implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 467933155910982858L;

	/**
	 * The ID of the graph to consume this GraphUpdateMessage.
	 */
	protected GID graphID;

	/**
	 * The VertexUpdateMessages.
	 */
	protected HashMap<VID, VertexUpdateMessage> vertexUpdateMessages = new HashMap<VID, VertexUpdateMessage>();

	/**
	 * The number of edge update messages included in this GraphUpdateMessage.
	 */
	protected int numEdgeUpdateMessages = 0;

	/**
	 * Constructs a GraphUpdateMessage.
	 * 
	 * @param g
	 *            the ID of the graph to consume the GraphUpdateMessage.
	 */
	public GraphUpdateMessage(GID g) {
		this.graphID = g;
	}

	/**
	 * Returns the ID of the graph to consume this GraphUpdateMessage.
	 * 
	 * @return the ID of the graph to consume this GraphUpdateMessage.
	 */

	public GID graphID() {
		return graphID;
	}

	/**
	 * Returns number of edge update messages included in this GraphUpdateMessage.
	 * 
	 * @return number of edge update messages included in this GraphUpdateMessage.
	 */
	public int numEdgeUpdateMessages() {
		return numEdgeUpdateMessages;
	}

	/**
	 * Returns true only if this GraphUpdateMessage is empty.
	 * 
	 * @return true if this GraphUpdateMessage is empty; false otherwise.
	 */
	public boolean isEmpty() {
		return numEdgeUpdateMessages == 0 && vertexUpdateMessages.size() == 0;
	}

	/**
	 * Returns the VertexUpdateMessages contained in this GraphUpdateMessage.
	 * 
	 * @return the VertexUpdateMessages contained in this GraphUpdateMessage.
	 */
	public Iterable<Entry<VID, VertexUpdateMessage>> vertexUpdateMessages() {
		return vertexUpdateMessages.entrySet();
	}

	/**
	 * Clears this GraphUpdateMessage.
	 */
	public void clear() {
		vertexUpdateMessages.clear();
		numEdgeUpdateMessages = 0;
	}

	/**
	 * Returns the VertexUpdateMessage for the specified Vertex.
	 * 
	 * @param vertexID
	 *            the ID of the Vertex.
	 * @return the VertexUpdateMessage for the specified Vertex.
	 */
	public VertexUpdateMessage vertexUpdateMessage(VID vertexID) {
		VertexUpdateMessage old = vertexUpdateMessages.get(vertexID);
		if (old != null)
			return old;
		else {
			old = new VertexUpdateMessage(vertexID);
			vertexUpdateMessages.put(vertexID, old);
			return old;
		}
	}

	/**
	 * Updates the specified Edge.
	 * 
	 * @param src
	 *            the ID of the vertex that the Edge emanates from.
	 * @param des
	 *            the ID of the vertex that the Edge is incident on.
	 * @return the message for updating the specified Edge.
	 */
	public DataObjectUpdateMessage edgeUpdateMessage(VID src, VID des) {
		VertexUpdateMessage m = vertexUpdateMessage(src);
		int oldSize = m.numOutgoingEdgesToUpdate();
		DataObjectUpdateMessage edgeUpdateMessage = m.updateMessageForOutgoingEdge(des);
		numEdgeUpdateMessages += (m.numOutgoingEdgesToUpdate() - oldSize);
		m = vertexUpdateMessage(des);
		edgeUpdateMessage = m.inEdgeUpdateMessage(src, edgeUpdateMessage);
		return edgeUpdateMessage;
	}

}

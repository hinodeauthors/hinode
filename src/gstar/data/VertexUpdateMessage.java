package gstar.data;

import java.util.HashMap;

import hwanglab.data.DataObjectUpdateMessage;

/**
 * A VertexUpdateMessage contains information for updating a Vertex.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class VertexUpdateMessage extends DataObjectUpdateMessage {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5026168333798139739L;

	/**
	 * The ID of the Vertex to consume this VertexUpdateMessage.
	 */
	protected VID vertexID;

	/**
	 * update messages for incoming Edges.
	 */
	public HashMap<VID, DataObjectUpdateMessage> updateMessagesForIncomingEdges = new HashMap<VID, DataObjectUpdateMessage>(); //WARNING! Changed to "public" from protected! Used in FatNode.update()

	/**
	 * update messages for outgoing Edges.
	 */
	public HashMap<VID, DataObjectUpdateMessage> updateMessagesForOutgoingEdges = new HashMap<VID, DataObjectUpdateMessage>(); //FWARNING! Changed to "public" from protected! Used in FatNode.update()

	/**
	 * Constructs a VertexUpdateMessage.
	 * 
	 * @param vertexID
	 *            the ID of the Vertex to consume the VertexUpdateMessage.
	 */
	public VertexUpdateMessage(VID vertexID) {
		this.vertexID = vertexID;
	}

	/**
	 * Returns the ID of the Vertex to consume this VertexUpdateMessage.
	 * 
	 * @return the ID of the Vertex to consume this VertexUpdateMessage.
	 */
	public VID vertexID() {
		return vertexID;
	}

	/**
	 * Returns the update message for the specified incoming edge.
	 * 
	 * @param src
	 *            the ID of the vertex that the Edge emanates from.
	 * @param updateMessage
	 *            an update message to use if there is no update message for the specified edge.
	 * @return the update message for the specified incoming edge.
	 */
	public DataObjectUpdateMessage inEdgeUpdateMessage(VID src, DataObjectUpdateMessage updateMessage) {
		DataObjectUpdateMessage m = updateMessagesForIncomingEdges.get(src);
		if (m == null) {
			m = (updateMessage != null ? updateMessage : new DataObjectUpdateMessage());
			updateMessagesForIncomingEdges.put(src, m);
		}
		return m;
	}

	/**
	 * Returns the update message for the specified outgoing edge.
	 * 
	 * @param des
	 *            the ID of the vertex that the Edge is incident on.
	 * @return the update message for the specified outgoing edge.
	 */
	public DataObjectUpdateMessage updateMessageForOutgoingEdge(VID des) {
		DataObjectUpdateMessage m = updateMessagesForOutgoingEdges.get(des);
		if (m == null) {
			m = new DataObjectUpdateMessage();
			updateMessagesForOutgoingEdges.put(des, m);
		}
		return m;
	}

	/**
	 * Returns the number of outgoing edges to update.
	 * 
	 * @return the number of outgoing edges to update.
	 */
	public int numOutgoingEdgesToUpdate() {
		return updateMessagesForOutgoingEdges.size();
	}

}

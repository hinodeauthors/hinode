package gstar.data;

import hwanglab.data.DataObject;
import hwanglab.data.DataObjectUpdateMessage;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A Vertex represents a vertex in a graph.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Vertex extends DataObject {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -6869869779415243331L;

	/**
	 * The incoming edges of this Vertex.
	 */
	public java.util.TreeMap<VID, Edge> incomingEdges = new java.util.TreeMap<VID, Edge>(); //WARNING! Changed to "public" from protected! Used in FatNode.convertToVertex()

	/**
	 * The outgoing edges of this Vertex.
	 */
	public java.util.TreeMap<VID, Edge> outgoingEdges = new java.util.TreeMap<VID, Edge>(); //WARNING! Changed to "public" from protected! Used in FatNode.convertToVertex()

	/**
	 * The ID of the most recent graph that contains this Vertex.
	 */
	protected GID lastVersion = null;

	/**
	 * Constructs a Vertex
	 * 
	 * @param vertexID
	 *            the identifier of the Vertex.
	 */
	public Vertex(VID vertexID) {
		update("id", vertexID);
		this.update("incoming_edges", incomingEdges);
		this.update("outgoing_edges", outgoingEdges);
	}

	/**
	 * Returns the ID of this Vertex.
	 * 
	 * @return the ID of this Vertex.
	 */
	public VID vertexID() {
		return (VID) value("id");
	}

	/**
	 * Returns the outgoing edges of this Vertex.
	 * 
	 * @return the outgoing edges of this Vertex.
	 */
	public Collection<Edge> outgoingEdges() {
		return outgoingEdges.values();
	}

	/**
	 * Returns the incoming edges of this Vertex.
	 * 
	 * @return the incoming edges of this Vertex.
	 */
	public Collection<Edge> incomingEdges() {
		return incomingEdges.values();
	}

	/**
	 * Updates this DataObject based on the specified DataObjectUpdateMessage.
	 * 
	 * @param message
	 *            the DataObjectUpdateMessage to consume.
	 * @param g
	 *            the ID of the graph where this Vertex is updated.
	 */
	public void update(VertexUpdateMessage message, GID g) {
		super.update(message);
		for (Entry<VID, DataObjectUpdateMessage> e : message.updateMessagesForIncomingEdges.entrySet()) {
			updateIncomingEdge(e.getKey(), e.getValue());
		}
		for (Entry<VID, DataObjectUpdateMessage> e : message.updateMessagesForOutgoingEdges.entrySet()) {
			updateOutgoingEdge(e.getKey(), e.getValue());
		}
		this.lastVersion = g;
	}

	/**
	 * Returns the specified incoming Edge.
	 * 
	 * @param src
	 *            the ID of the Vertex that the Edge emanates from.
	 * @return the specified incoming Edge; null if none exists.
	 */
	public Edge incomingEdge(VID src) {
		return incomingEdges.get(src);
	}

	/**
	 * Returns the specified outgoing Edge.
	 * 
	 * @param des
	 *            the ID of the Vertex that the Edge is incident on.
	 * @return the specified outgoing Edge; null if none exists.
	 */
	public Edge iutgoingEdge(VID des) {
		return outgoingEdges.get(des);
	}

	/**
	 * Updates the specified incoming Edge.
	 * 
	 * @param src
	 *            the ID of the vertex that the Edge emanates from.
	 * @param updateMessage
	 *            the message containing information for updating the Edge.
	 */
	protected void updateIncomingEdge(VID src, DataObjectUpdateMessage updateMessage) {
		if (updateMessage.resetScheduled() && updateMessage.isEmpty())
			incomingEdges.remove(src);
		else
			createIncomingEdge(src).update(updateMessage);
	}

	/**
	 * Updates the specified outgoing Edge.
	 * 
	 * @param des
	 *            the ID of the vertex that the Edge is incident on.
	 * @param updateMessage
	 *            the message containing information for updating the Edge.
	 */
	protected void updateOutgoingEdge(VID des, DataObjectUpdateMessage updateMessage) {
		if (updateMessage.resetScheduled() && updateMessage.isEmpty())
			outgoingEdges.remove(des);
		else
			createOutgoingEdge(des).update(updateMessage);
	}

	/**
	 * Returns the specified incoming Edge. Creates a new one if none exists.
	 * 
	 * @param src
	 *            the ID of the vertex that the Edge emanates from.
	 * @return the specified incoming Edge.
	 */
	protected Edge createIncomingEdge(VID src) {
		Edge edge = incomingEdges.get(src);
		if (edge == null) {
			edge = new Edge(src);
			incomingEdges.put(src, edge);
		}
		return edge;
	}

	/**
	 * Returns the specified outgoing Edge. Creates a new one if none exists.
	 * 
	 * @param des
	 *            the ID of the vertex that the Edge is incident on.
	 * @return the specified outgoing Edge.
	 */
	protected Edge createOutgoingEdge(VID des) {
		Edge edge = outgoingEdges.get(des);
		if (edge == null) {
			edge = new Edge(des);
			outgoingEdges.put(des, edge);
		}
		return edge;
	}

	/**
	 * Returns the ID of the most recent graph that contains this Vertex.
	 * 
	 * @return the ID of the most recent graph that contains this Vertex.
	 */
	public GID lastVersion() {
		return lastVersion;
	}

	/**
	 * Returns the IDs of the graphs related to this Vertex.
	 * 
	 * @return the IDs of the graphs related to this Vertex.
	 */
	@SuppressWarnings("unchecked")
	public Set<GID> graphIDs() {
		return (Set<GID>) value("graph.id");
	}

}

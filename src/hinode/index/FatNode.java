package hinode.index;

import gstar.data.Edge;
import gstar.data.GID;
import gstar.data.VID;
import gstar.data.Vertex;
import gstar.data.VertexUpdateMessage;
import hwanglab.data.DataObject;
import hwanglab.data.DataObjectUpdateMessage;
import hwanglab.util.Pair;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import hinode.util.EdgePair;
import hinode.util.Interval;

public class FatNode extends DataObject 
{
	/**
	 * Automatically generated serial version UID. 
	 */
	private static final long serialVersionUID = -1294976712321529694L;
	
	/**
	 * GID of when the last change occurred in this fat node
	 */
	private ArrayList<Interval> changesLog;

	/**
	 * The incoming edges of this Vertex.
	 */
	protected java.util.LinkedHashMap<EdgePair, Edge> incomingEdges = new java.util.LinkedHashMap<EdgePair, Edge>();

	/**
	 * The outgoing edges of this Vertex.
	 */
	protected java.util.LinkedHashMap<EdgePair, Edge> outgoingEdges = new java.util.LinkedHashMap<EdgePair, Edge>();

	public FatNode(VID vertexID) 
	{
		update("id", vertexID);
		changesLog = new ArrayList<Interval>();
	}
	
	public int getActiveEdgeCount(GID t)
	{
		int count = 0;
		for (EdgePair i : outgoingEdges.keySet())
		{
			if (i.second().stab(Double.valueOf(t.toString()).intValue()))
				count++;
		}
		return count;
	}

	public void update(VertexUpdateMessage message, GID t) 
	{
		if (message.resetScheduled())
		{
		}
		// First, update the fat node's attributes
		for (Map.Entry<String, Object> e : message.attributes())
		{
			updateAttribute(e.getKey(), e.getValue(), t);
		}
		// Then we have to update the edges
		for (Entry<VID, DataObjectUpdateMessage> e : message.updateMessagesForIncomingEdges.entrySet()) // Incoming edges
		{
			updateIncomingEdge(e.getKey(), e.getValue(), t);
		}
		for (Entry<VID, DataObjectUpdateMessage> e : message.updateMessagesForOutgoingEdges.entrySet()) // Outgoing edges
		{
			updateOutgoingEdge(e.getKey(), e.getValue(), t);
		}
		if (!changesLog.isEmpty())
			changesLog.get(changesLog.size()-1).setEnd(Double.valueOf(t.toString()).intValue());
		changesLog.add(new Interval(Double.valueOf(t.toString()).intValue()));
	}
	
	private void updateOutgoingEdge(VID des, DataObjectUpdateMessage value, GID t) 
	{
		EdgePair activeEdgePair = getExistingActiveOutgoingEdgePair(des, t);
		if (activeEdgePair != null) // Close the interval for the current "this" --> "des" edge
		{
			Edge oldedge = outgoingEdges.get(activeEdgePair);
			outgoingEdges.remove(activeEdgePair);
			activeEdgePair.second().setEnd(Double.valueOf(t.toString()).intValue());
			outgoingEdges.put(activeEdgePair, oldedge);
		}
		Interval in = new Interval(Double.valueOf(t.toString()).intValue());
		EdgePair ep = new EdgePair(des,in);
		Edge e = new Edge(des);
		e.update(value);
		outgoingEdges.put(ep, e);	
	}

	private EdgePair getExistingActiveOutgoingEdgePair(VID des, GID t) 
	{
		for (EdgePair pair : outgoingEdges.keySet())
			if (des.equals(pair.first()) && pair.second().stab(Double.valueOf(t.toString()).intValue()))
				return pair;
		return null;
	}

	private void updateIncomingEdge(VID src, DataObjectUpdateMessage value, GID t) 
	{
		EdgePair activeEdgePair = getExistingActiveIncomingEdgePair(src, t);
		if (activeEdgePair != null) // Close the interval for the current "src" --> "this" edge
		{
			Edge oldedge = incomingEdges.get(activeEdgePair);
			incomingEdges.remove(activeEdgePair);
			activeEdgePair.second().setEnd(Double.valueOf(t.toString()).intValue());
			incomingEdges.put(activeEdgePair, oldedge);
		}
		Interval in = new Interval(Double.valueOf(t.toString()).intValue());
		EdgePair ep = new EdgePair(src,in);
		Edge e = new Edge(src);
		e.update(value);
		incomingEdges.put(ep, e);
	}

	private EdgePair getExistingActiveIncomingEdgePair(VID src, GID t) 
	{
		for (EdgePair pair : incomingEdges.keySet())
			if (src.equals(pair.first()) && pair.second().stab(Double.valueOf(t.toString()).intValue()))
				return pair;
		return null;
	}

	private void updateAttribute(String key, Object value, GID t) 
	{ 
		if (!attributes.containsKey(key)) // The attribute "key" doesn't exist for "this" fatnode
		{
			attributes.put(key, new ArrayList<Pair<Object,Interval>>());
		}
		@SuppressWarnings("unchecked")
		ArrayList<Pair<Object,Interval>> pairs = (ArrayList<Pair<Object, Interval>>) attributes.get(key);
		if (pairs.isEmpty())
		{
			Interval in = new Interval(Double.valueOf(t.toString()).intValue());
			Pair<Object, Interval> p = new Pair<Object, Interval>(value,in);
			pairs.add(p);
			attributes.put(key,pairs);
			return;
		}
		// Get the last Pair<Object,Interval> for the attribute "key"
		Object ob = pairs.get(pairs.size() - 1).first();
		Interval interval = pairs.get(pairs.size() - 1).second();
		
		//The following "if" only works for data type "String, Integer, Double".
		if (ob instanceof String) // Attribute is String
		{
			String s = (String) ob;
			String toBeAdded = (String) value;
			if (s.equals(toBeAdded))
				return;
			else
			{
				interval.setEnd(Double.valueOf(t.toString()).intValue());
				Pair<Object, Interval> p = new Pair<Object, Interval>(value,new Interval(Double.valueOf(t.toString()).intValue()));
				pairs.add(p);
				attributes.put(key, pairs);
			}
		}
		else if (ob instanceof Double) // Attribute is Double
		{
			Double d = (Double) ob;
			Double toBeAdded = (Double) value;
			if (d.equals(toBeAdded))
				return;
			else
			{
				interval.setEnd(Double.valueOf(t.toString()).intValue());
				Pair<Object, Interval> p = new Pair<Object, Interval>(value,new Interval(Double.valueOf(t.toString()).intValue()));
				pairs.add(p);
				attributes.put(key, pairs);
			}			
		}
		else if (ob instanceof Integer) // Attribute is Integer
		{
			Integer i = (Integer) ob;
			Integer toBeAdded = (Integer) value;
			if (i.equals(toBeAdded))
				return;
			else
			{
				interval.setEnd(Double.valueOf(t.toString()).intValue());
				Pair<Object, Interval> p = new Pair<Object, Interval>(value,new Interval(Double.valueOf(t.toString()).intValue()));
				pairs.add(p);
				attributes.put(key, pairs);
			}	
		}
	}

	public VID vertexID() 
	{
		return (VID) value("id");
	}
	
	public Interval getLastValidInterval(GID t)
	{
		for (Interval i : changesLog)
			if (i.stab(Double.valueOf(t.toString()).intValue()))
				return i;
		return null;
	}

	public Vertex convertToVertex(GID g) 
	{
		Vertex vertex = new Vertex(vertexID());
		VertexUpdateMessage msg = new VertexUpdateMessage(vertexID());
		for (String attr : attributes.keySet()) // First get all the attributes at time instance "g"
		{
			if (attr.equals("id")) // Ignore the ID attribute
				continue;
			Object value = getAttributeValueAtInstance(attr,g);
			if (value == null) // "attr" doesn't have a value at instance "g"
				continue;
			msg.update(attr, value);		
		}
		vertex.update(msg, g);
		for (EdgePair ep : incomingEdges.keySet()) // Get all incoming edges at time instance "g"
		{
			if (ep.second().stab(Double.valueOf(g.toString()).intValue())) // If an incoming edge exists at time instance "g"
			{
				vertex.incomingEdges.put(ep.first(), incomingEdges.get(ep)); // insert it into "incomingEdges" of "vertex"
			}
		}
		for (EdgePair ep : outgoingEdges.keySet()) // Get all outgoing edges at time instance "g"
		{
			if (ep.second().stab(Double.valueOf(g.toString()).intValue())) // If an outgoing edge exists at time instance "g"
			{
				vertex.outgoingEdges.put(ep.first(), outgoingEdges.get(ep)); // insert it into "outgoingEdges" of "vertex"
			}
		}		
		return vertex;
	}

	private Object getAttributeValueAtInstance(String attr, GID g) 
	{
		@SuppressWarnings("unchecked")
		ArrayList<Pair<Object,Interval>> pairs = (ArrayList<Pair<Object, Interval>>) attributes.get(attr);
		for (Pair<Object,Interval> pair : pairs)
		{
			if (pair.second().stab(Double.valueOf(g.toString()).intValue()))
				return pair.first();
		}
		return null;
	}

	public boolean isRelevant(Vector<GID> sorted)
	{
		int firstInstance = changesLog.get(0).getStart();
		int lastInstance = changesLog.get(changesLog.size()-1).getEnd(); // We assume that changesLog.size() > 0
		Interval life = new Interval(firstInstance,lastInstance);
		for (GID g : sorted)
			if (life.stab(Double.valueOf(g.toString()).intValue()))
				return true;
		return false;
	}
}

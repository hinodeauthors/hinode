package hinode.index;

import gstar.data.GID;
import gstar.data.VID;
import hwanglab.data.storage.ObjectLocation;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;

public class FatNodeIndex implements java.io.Serializable 
{
	/**
	 * Automatically generated serial version UID. 
	 */
	private static final long serialVersionUID = 398750632138012458L;
	
	/**
	 * The index that stores all the fat nodes
	 */
	protected LinkedHashMap<VID, ObjectLocation> index = new LinkedHashMap<VID, ObjectLocation>();
	
	/**
	 * A set of all the versions in the index
	 */
	protected Set<GID> versions = new HashSet<GID>();
	
	public ObjectLocation getFatNode(VID id)
	{
		return index.get(id);
	}

	public void put(VID id, ObjectLocation location, GID t) 
	{
		index.put(id, location);
		versions.add(t);
	}
	
	public Vector<ObjectLocation> getAllFatNodes()
	{
		Vector<ObjectLocation> allNodes = new Vector<ObjectLocation>();
		allNodes.addAll(index.values());
		return allNodes;
	}
	
	public Set<GID> versionNumbers() 
	{
		return versions;
	}
}

package gstar.data;


import gstar.data.GraphPath.InvalidPathException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * A GraphDirectory is a collection of other GraphDirectories and a series of graphs.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class GraphDirectory implements Comparable<GraphDirectory>, java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -9087131623119360693L;

	/**
	 * The name of this GraphDirectory.
	 */
	protected String name;

	/**
	 * The parent GraphDirectory.
	 */
	protected GraphDirectory parent;

	/**
	 * The children.
	 */
	protected TreeMap<String, GraphDirectory> children = new TreeMap<String, GraphDirectory>();

	/**
	 * The IDs of the workers that store graph data.
	 */
	protected int[] workerIDs = null;

	/**
	 * The attributes defined in this GraphDirectory.
	 */
	protected TreeMap<String, Class<?>> attributes;

	/**
	 * The properties of this GraphDirectory.
	 */
	protected GraphDirectoryProperties properties;

	/**
	 * The properties of the graphs within this GraphDirectory.
	 */
	protected TreeMap<GID, GraphProperties> graphProperties;

	/**
	 * Creates a GraphDirectory.
	 * 
	 * @param name
	 *            the name of the GraphDirectory.
	 * @param parent
	 *            the parent GraphDirectory.
	 * @param workerIDs
	 *            the IDs of the workers that store the graph data.
	 * @throws InvalidPathException
	 *             if an invalid name for the GraphDirectory is given.
	 */
	public GraphDirectory(String name, GraphDirectory parent, int[] workerIDs) throws InvalidPathException {
		this.name = name;
		this.parent = parent;
		this.workerIDs = workerIDs;
		this.attributes = new TreeMap<String, Class<?>>();
		GraphPath path = new GraphPath(name);
		if ((parent == null && path.toString().length() > 0) || path.split().length > 1)
			throw new InvalidPathException();
		if (parent != null)
			path = parent.absolutePath().concatenate(path);
		this.properties = new GraphDirectoryProperties(path, 0, workerIDs.length, 0, 0);
		this.graphProperties = new TreeMap<GID, GraphProperties>();
	}

	/**
	 * Returns the absolute path to this GraphDirectory.
	 * 
	 * @return the absolute path to this GraphDirectory.
	 */
	public GraphPath absolutePath() {
		try {
			if (parent == null)
				return new GraphPath(name);
			else
				return parent.absolutePath().concatenate(new GraphPath(name));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Adds a child.
	 * 
	 * @param child
	 *            the new child GraphDirectory.
	 */
	public void addChild(GraphDirectory child) {
		children.put(child.name, child);
	}

	/**
	 * Returns the specified child.
	 * 
	 * @param childName
	 *            the name of the child.
	 * @return the specified child.
	 */
	public GraphDirectory child(String childName) {
		if (childName.equals(GraphPath.WILDCARD))
			return null;
		return children.get(childName);
	}

	/**
	 * Returns the specified children.
	 * 
	 * @param expression
	 *            the expression that specifies the children.
	 * @return the specified children.
	 */
	public Collection<GraphDirectory> children(String expression) {
		if (expression.equals(GraphPath.WILDCARD))
			return new LinkedList<GraphDirectory>(children());
		LinkedList<GraphDirectory> children = new LinkedList<GraphDirectory>();
		GraphDirectory child = this.children.get(expression);
		if (child != null)
			children.add(child);
		return children;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns the children of this GraphDirectory.
	 * 
	 * @return the children of this GraphDirectory.
	 */
	public Collection<GraphDirectory> children() {
		return children.values();
	}

	@Override
	public int compareTo(GraphDirectory other) {
		int step1 = this.parent.compareTo(other.parent);
		int step2 = this.name.compareTo(other.name);
		if (step1 == 0)
			if (step2 == 0)
				if (this.workerIDs.length == other.workerIDs.length)
					return 0;
				else
					return this.workerIDs.length < other.workerIDs.length ? -1 : 1;
			else
				return step2;
		else
			return step1;
	}

	@Override
	public boolean equals(Object other) {
		return compareTo((GraphDirectory) other) == 0;
	}

	/**
	 * Returns the properties of this GraphDirectory.
	 * 
	 * @return the properties of this GraphDirectory.
	 */
	public GraphDirectoryProperties properties() {
		return properties;
	}

	/**
	 * Returns the properties of the graphs in this GraphDirectory.
	 * 
	 * @return the properties of the graphs in this GraphDirectory.
	 */
	public TreeMap<GID, GraphProperties> graphProperties() {
		return graphProperties;
	}

	/**
	 * Returns the attributes defined in this GraphDirectory.
	 * 
	 * @return the attributes defined in this GraphDirectory.
	 */
	public TreeMap<String, Class<?>> attributes() {
		return attributes;
	}

	/**
	 * Removes this GraphDirectory from its parent.
	 */
	public void remove() {
		parent.children.remove(this.name);
	}

	/**
	 * Returns the IDs of the Workers that store the data of this GraphDirectory.
	 * 
	 * @return the IDs of the Workers that store the data of this GraphDirectory.
	 */
	public int[] workerIDs() {
		return workerIDs;
	}

}

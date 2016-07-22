package gstar.data;

import gstar.data.GraphPath.InvalidPathException;
import gstar.query.operator.Operator;
import hwanglab.data.storage.ObjectLocation;
import hwanglab.data.storage.StorageManager;
import hwanglab.util.versioning.DuplicateVersionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import hinode.index.FatNode;
import hinode.index.FatNodeIndex;
import hinode.util.Interval;

public class GraphManager {

	/**
	 * The ID of this GraphManager.
	 */
	int id;

	/**
	 * The system directory.
	 */
	protected String systemDirectory;

	/**
	 * The size of the memory buffer.
	 */
	protected long bufferSize;

	/**
	 * The path to the current GraphDirectory.
	 */
	private GraphPath path;
	
	/**
	 * Helper flag. True if we want to use our (hinode) index
	 */
	private boolean useSimpleIndex=true;

	/**
	 * The graph index.
	 */
	protected FatNodeIndex index;

	/**
	 * The StorageManager.
	 */
	protected StorageManager storageManager;

	/**
	 * Constructs a GraphManager.
	 * 
	 * @param id
	 *            the ID of this GraphManager.
	 * @param systemDirectory
	 *            the system directory.
	 * @param bufferSize
	 *            the size of the memory buffer.
	 * @throws IOException
	 *             if an error occurs.
	 * @throws ClassNotFoundException
	 *             if a class cannot be found.
	 */
	public GraphManager(int id, String systemDirectory, long bufferSize) throws IOException, ClassNotFoundException {
		this.id = id;
		this.systemDirectory = systemDirectory;
		this.bufferSize = bufferSize;
		try {
			File dir = new File(systemDirectory);
			if (!dir.exists())
				dir.mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			changeGraphPath(new GraphPath(""));
		} catch (InvalidPathException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructs a new graph.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory where the graph is created.
	 * @param g
	 *            the ID of the new graph.
	 * @throws DuplicateVersionException
	 *             if the graph version to create already exists.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws ClassNotFoundException
	 *             if a class cannot be found.
	 */
	public synchronized void createGraph(GraphPath absolutePath, GID g) throws ClassNotFoundException,
			DuplicateVersionException, IOException {
		createGraph(absolutePath, g, null);
	}

	/**
	 * Creates a new graph based on the specified graph.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory where the graph is created.
	 * @param g
	 *            the ID of the new graph.
	 * @param prevG
	 *            the ID of the graph from which a new graph is constructed.
	 * @throws DuplicateVersionException
	 *             if the graph version to create already exists.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws ClassNotFoundException
	 *             if a class cannot be found.
	 */
	public synchronized void createGraph(GraphPath absolutePath, GID g, GID prevG) throws DuplicateVersionException,
			ClassNotFoundException, IOException {
		changeGraphPath(absolutePath);
	}

	/**
	 * Updates the specified graph.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @param t
	 *            Originally the ID of the graph (here used as a time instance of the graph).
	 * @param messages
	 *            the VertexUpdateMessages.
	 * @return a GraphProperties instance representing the properties of the specified graph.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws ClassNotFoundException
	 *             if a class cannot be found.
	 */
	public synchronized GraphProperties updateGraph(GraphPath absolutePath, GID t,
			Collection<VertexUpdateMessage> messages) throws ClassNotFoundException, IOException {
		changeGraphPath(absolutePath);
		int verticesAdded = 0;
		int edgesAdded = 0;
		for (VertexUpdateMessage message : messages) 
		{
			ObjectLocation location = index.getFatNode(message.vertexID());
			FatNode v = location == null ? null : (FatNode) storageManager.get(location);
			if (v == null)
			{
				v = new FatNode(message.vertexID);
				verticesAdded++;
			}
			
			int	prevEdgeCount = v.getActiveEdgeCount(t);
			if (location != null)
			{
				v.update(message, t);
				index.put(v.vertexID(), storageManager.put(location, v), t);
			}
			else
			{
				v.update(message, t);
				index.put(v.vertexID(), storageManager.add(v), t);				
			}
			edgesAdded += (v.getActiveEdgeCount(t) - prevEdgeCount);
		}
		return new GraphProperties(absolutePath, t, verticesAdded, edgesAdded);
	}

	/**
	 * Returns an iterator over all of the vertices contained in the specified graphs.
	 * 
	 * @param g
	 *            a set of graph IDs.
	 * @return an iterator over all of the vertices contained in the specified graphs.
	 */
	public synchronized Iterator<Vertex> vertices(Set<GID> g)
	{
		final Vector<GID> sorted = new Vector<GID>();
		sorted.addAll(g);
		Collections.sort(sorted);
		
		final Iterator<ObjectLocation> fatnodes = index.getAllFatNodes().iterator();
		
		return new Iterator<Vertex>() {
			
			protected FatNode current = null;
			protected ArrayList<Vertex> vertexList = null;
			protected Iterator<Vertex> vertexListIterator = null;
			
			@Override
			public boolean hasNext() 
			{
				if (vertexListIterator != null && vertexListIterator.hasNext())
					return true;
				
				while (fatnodes.hasNext())
				{
					ObjectLocation ol = fatnodes.next();
					try 
					{
						current = (FatNode) storageManager.get(ol);
					} 
					catch (ClassNotFoundException e) 
					{
						e.printStackTrace();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					if (current.isRelevant(sorted))
						return true;
				}
				return false;
			}

			@Override
			public Vertex next() 
			{
				if (vertexListIterator!=null && vertexListIterator.hasNext())
					return vertexListIterator.next();
				
				vertexList = new ArrayList<Vertex>();
				Set<GID> validGraphs = new HashSet<GID>();
				Vertex vertex = null;
				Interval interval = null;
				boolean hasIntervalChanged = true;
				
				for (GID instance : sorted) // For each time instance
				{
					if (interval != null) // Check if the interval changes with the new time instance "instance"
					{
						if (current.getLastValidInterval(instance) == interval)
							hasIntervalChanged = false;
						else
							hasIntervalChanged = true;
					}
					if (hasIntervalChanged) // If this is a new relevant interval
					{
						if (vertex == null)
						{
							interval = current.getLastValidInterval(instance);	
							if (interval == null)
								continue;
							vertex = current.convertToVertex(instance);
							validGraphs = new HashSet<GID>();
							validGraphs.add(instance);					
						}
						else if (vertex != null) 
						{
							Operator.setGraphIDs(vertex, validGraphs);
							vertexList.add(vertex);
							vertex = current.convertToVertex(instance);
							validGraphs = new HashSet<GID>();
							validGraphs.add(instance);
							interval = current.getLastValidInterval(instance);
						}
					}
					else
					{
						validGraphs.add(instance);
					}
				}
				if (vertex != null)
				{
					Operator.setGraphIDs(vertex, validGraphs);
					vertexList.add(vertex);
				}
				vertexListIterator = vertexList.iterator();
				return vertexListIterator.next();
			}				

			@Override
			public void remove() 
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Returns vertices that are related to the specified vertex and graph IDs.
	 * 
	 * @param v
	 *            the ID of a vertex.
	 * @param g
	 *            a set of graph IDs.
	 */
	public Iterator<Vertex> vertices(VID v, Set<GID> g)
	{
		ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
		
		Vector<GID> sorted = new Vector<GID>();
		sorted.addAll(g);
		Collections.sort(sorted);
		
		Set<GID> validGraphs = new HashSet<GID>();
		Vertex vertex = null;
		FatNode node = null;
		try 
		{
			ObjectLocation ol = index.getFatNode(v);
			node = (FatNode) storageManager.get(ol);
		} catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
		Interval interval = null;
		boolean hasIntervalChanged = true;
		
		for (GID instance : sorted) // For each time instance
		{
			if (interval != null) // Check if the interval changes with the new time instance "instance"
			{
				if (node.getLastValidInterval(instance) == interval)
					hasIntervalChanged = false;
				else
					hasIntervalChanged = true;
			}
			if (hasIntervalChanged) // If this is a new relevant interval
			{
				if (vertex == null)
				{
					interval = node.getLastValidInterval(instance);	
					if (interval == null)
						continue;
					vertex = node.convertToVertex(instance);
					validGraphs = new HashSet<GID>();
					validGraphs.add(instance);					
				}
				else if (vertex != null) 
				{
					Operator.setGraphIDs(vertex, validGraphs);
					vertexList.add(vertex);
					vertex = node.convertToVertex(instance);
					validGraphs = new HashSet<GID>();
					validGraphs.add(instance);
					interval = node.getLastValidInterval(instance);
				}
			}
			else
			{
				validGraphs.add(instance);
			}
		}
		if (vertex != null)
		{
			Operator.setGraphIDs(vertex, validGraphs);
			vertexList.add(vertex);
		}
		
		return vertexList.iterator();
	}

	/**
	 * Removes the data stored in the specified GraphDirectory.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @throws IOException
	 *             if an error occurs.
	 * @throws ClassNotFoundException
	 *             if a class cannot be found.
	 */
	public synchronized void reset(GraphPath absolutePath) throws IOException, ClassNotFoundException {
		changeGraphPath(absolutePath);
		if (useSimpleIndex)
			index = new FatNodeIndex();
		if (storageManager != null)
			storageManager.clearData();
	}

	/**
	 * Changes the current GraphDirectory.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @throws ClassNotFoundException
	 *             if a class cannot be found.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public void changeGraphPath(GraphPath absolutePath) throws ClassNotFoundException, IOException {
		if (this.path == null || !absolutePath.equals(this.path)) {
			this.path = absolutePath;
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(dataFileName() + ".idx"));
				try {
					index = (FatNodeIndex) in.readObject();
				} finally {
					in.close();
				}
				if (storageManager != null)
					storageManager.shutdown();
				storageManager = new StorageManager(dataFileName() + ".db", bufferSize);
			} catch (FileNotFoundException e) {
				try {
					if (useSimpleIndex)
					{
						index = new FatNodeIndex();
					}
					if (storageManager != null)
						storageManager.shutdown();
					storageManager = new StorageManager(dataFileName() + ".db", bufferSize);
					storageManager.clearData();
				} catch (Exception ee) {
				}
			}
			System.gc();
		}
	}

	/**
	 * Saves the state of this GraphManager in the current GraphDirectory.
	 * 
	 * @return a GraphDirectoryProperties instance describing the properties of the current GraphDirectory.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws FileNotFoundException
	 *             if a file cannot be found.
	 */
	public synchronized GraphDirectoryProperties checkpoint() throws FileNotFoundException, IOException {
		File f = new File(dataFileName() + ".idx"); // saving the graph index
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
		try {
			out.writeObject(index);
			out.flush();
		} finally {
			out.close();
		}
		storageManager.checkpoint(); // checkpoint the storage manager
		PrintStream o = new PrintStream(new FileOutputStream(dataFileName() + ".rst")); // reporting
		try {
			o.println("% [id] [index size] [data size] [graphs]");
			o.println(id + " " + f.length() + " " + storageManager.dataSize() + " " + index.versionNumbers().size());
		} finally {
			o.close();
		}
		return new GraphDirectoryProperties(path, index.versionNumbers().size(), 1, storageManager.dataSize(), f.length());
	}

	/**
	 * Shuts down this GraphManager.
	 */
	public synchronized void shutdown() {
		if (storageManager != null)
			storageManager.shutdown();
	}

	/**
	 * Returns the name of the data file.
	 * 
	 * @return the name of the data file.
	 */
	protected String dataFileName() {
		return systemDirectory + File.separator + id + "_" + path.fileName();
	}

	/**
	 * Returns the size of the data on disk.
	 * 
	 * @return the size of the data on disk.
	 * @throws IOException
	 *             if an IO error occurs.
	 */
	public long dataSize() throws IOException {
		return storageManager.dataSize();
	}

	/**
	 * Returns the size of the index.
	 * 
	 * @return the size of the index.
	 * @throws IOException
	 *             if an error occurs.
	 */
	public synchronized long indexSize() throws IOException {
		java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
		java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(b);
		out.writeObject(index);
		return b.size();
	}

	/**
	 * Returns the StorageManager.
	 * 
	 * @return the StorageManager.
	 */
	public StorageManager storageManager() {
		return storageManager;
	}
}
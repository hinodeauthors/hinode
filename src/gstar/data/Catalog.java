package gstar.data;


import gstar.data.GraphPath.InvalidPathException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * A Catalog maintains information about the graph data.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Catalog implements java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 6276015089068170417L;

	/**
	 * The name of the file that stores this Catalog.
	 */
	private String fileName;

	/**
	 * The root GraphDirectory.
	 */
	protected GraphDirectory root;
	
	/**
	 * A flag indicating whether or not this Catalog has changed since the beginning of the most recent checkpoint.
	 */
	protected boolean changed = false;

	/**
	 * Constructs a Catalog.
	 * 
	 * @param fileName
	 *            the name of the file that stores the catalog.
	 * @throws FileNotFoundException
	 *             if the specified file cannot be found.
	 * @throws IOException
	 *             if an IO error occurs.
	 */
	public Catalog(String fileName, int[] workerIDs) throws FileNotFoundException, IOException {
		this.fileName = fileName;
		try {
			new File(fileName).getParentFile().mkdirs();
			FileInputStream fileIn = new FileInputStream(fileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			root = (GraphDirectory) in.readObject();
			fileIn.close();
		} catch (Exception e) {
//			System.err.println(e + ", so starting with an empty catalog");
			try {
				root = new GraphDirectory("", null, workerIDs);
			} catch (InvalidPathException e1) {
			}
			checkpoint();
			// e.printStackTrace();
		}
	}

	/**
	 * Returns the root GraphDirectory.
	 * 
	 * @return the root GraphDirectory.
	 */
	public GraphDirectory root() {
		return root;
	}
	
	/**
	 * Determines whether or not if this Catalog has been changed since the beginning or the most recent checkpoint.
	 *
	 * @return true if this Catalog has been changed since the beginning or the most recent checkpoint; false otherwise.
	 */
	public boolean changed() {
		return changed;
	}

	/**
	 * Creates a GraphDirectory
	 * 
	 * @param absolutePath
	 *            the absolute path to the directory
	 * @param workerIDs
	 *            the IDs of the workers that will store the data.
	 * @return the constructed GraphDirectory.
	 * @throws IOException
	 *             if there is a problem in writing the catalog data on disk.
	 * @throws InvalidPathException
	 *             if an invalid path name is given.
	 * @throws ExistingDirectoryException
	 *             if the specified directory already exists.
	 */
	public GraphDirectory createDirectory(GraphPath absolutePath, int[] workerIDs) throws IOException,
			InvalidPathException, ExistingDirectoryException {
		changed = true;
		String[] names = absolutePath.split();
		GraphDirectory current = root;
		for (int i = 1; i < names.length - 1; i++) {
			current = current.child(names[i]);
		}
		String childName = names[names.length - 1];
		if (current.child(childName) != null)
			throw new ExistingDirectoryException();
		GraphDirectory child = new GraphDirectory(childName, current, workerIDs);
		current.addChild(child);
		return child;
	}

	/**
	 * Returns the specified GraphDirectory.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory
	 * @return the specified GraphDirectory.
	 * @throws InvalidPathException
	 *             if an invalid path is given.
	 * @throws NoDirectoryException
	 *             if the specified GraphDirectory does not exist.
	 */
	public GraphDirectory directory(GraphPath absolutePath) throws InvalidPathException, NoDirectoryException {
		if (absolutePath.contains(GraphPath.WILDCARD))
			throw new GraphPath.InvalidPathException();
		String[] names = absolutePath.split();
		GraphDirectory directory = root;
		for (int i = 1; i < names.length; i++) {
			directory = directory.child(names[i]);
			if (directory == null)
				throw new NoDirectoryException();
		}
		return directory;
	}

	/**
	 * Returns the GraphDirectories that match the specified path.
	 * 
	 * @param absolutePath
	 *            a path expression in the form of an absolute path.
	 * @return the GraphDirectories that match the specified path.
	 */
	public Collection<GraphDirectory> directories(GraphPath absolutePath) {
		String[] names = absolutePath.split();
		GraphDirectory directory = root;
		for (int i = 1; i < names.length - 1; i++) {
			directory = directory.child(names[i]);
			if (directory == null)
				return new LinkedList<GraphDirectory>();
		}
		return directory.children(names[names.length - 1]);
	}

	/**
	 * Writes this Catalog on disk.
	 * 
	 * @throws FileNotFoundException
	 *             if the catalog file cannot be created.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public void checkpoint() throws FileNotFoundException, IOException {
		changed = false;
		final FileOutputStream fileOutStream = new FileOutputStream(fileName);
		final ObjectOutputStream out = new ObjectOutputStream(fileOutStream);
		try {
			out.writeObject(root);
		} finally {
			out.close();
			fileOutStream.close();
		}
	}

	/**
	 * Updates this Catalog using the specified properties.
	 * 
	 * @param properties
	 *            the properties of a GraphDirectory.
	 * @throws FileNotFoundException
	 *             if the catalog file cannot be found.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public synchronized void update(GraphDirectoryProperties properties) {
		changed = true;
		try {
			directoryProperties(properties.absolutePath()).update(properties);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates this Catalog using the specified properties.
	 * 
	 * @param properties
	 *            the properties of a Graph.
	 */
	public synchronized void update(GraphProperties properties) {
		changed = true;
		try {
			TreeMap<GID, GraphProperties> t = directory(properties.absolutePath()).graphProperties();
			GraphProperties tt = t.get(properties.graphID());
			if (tt == null) {
				tt = new GraphProperties(properties.absolutePath(), properties.graphID(), 0, 0);
				t.put(properties.graphID(), tt);
			}
			tt.update(properties);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the properties of the specified GraphDirectory.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @return the properties of the specified GraphDirectory.
	 */
	public GraphDirectoryProperties directoryProperties(GraphPath absolutePath) {
		try {
			return directory(absolutePath).properties();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the properties of the graphs in the specified GraphDirectory.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @return the properties of the graphs in the specified GraphDirectory.
	 */
	public TreeMap<GID, GraphProperties> graphProperties(GraphPath absolutePath) {
		try {
			return directory(absolutePath).graphProperties();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the properties of the specified Graph.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @param graphID
	 *            the ID of the Graph.
	 * @return the properties of the specified Graph.
	 * @throws InvalidPathException
	 *             if an invalid path name is given.
	 * @throws NoDirectoryException
	 *             if the specified GraphDirectory cannot be found.
	 */
	public GraphProperties graphProperties(GraphPath absolutePath, GID graphID) throws InvalidPathException,
			NoDirectoryException {
		TreeMap<GID, GraphProperties> p = directory(absolutePath).graphProperties();
		return (p == null ? null : p.get(graphID));
	}

	@Override
	public String toString() {
		return "Catalog [" + fileName + "]";
	}

	/**
	 * Adds the specified attributes in the specified directory.
	 * 
	 * @param absolutePath
	 *            the absolute path to the directory.
	 * @param type
	 *            the type of the attributes.
	 * @param attributeNames
	 *            an iterator over the attribute names.
	 * @return the attributes in the current directory.
	 * @throws AttributeRedefinitionException
	 *             if an attribute is being defined again.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws FileNotFoundException
	 *             if a desired file cannot be found.
	 * @throws InvalidPathException
	 *             if an invalid path is given.
	 * @throws NoDirectoryException
	 *             if the specified GraphDirectory cannot be found.
	 */
	public TreeMap<String, Class<?>> addAttributes(GraphPath absolutePath, Class<?> type, String... attributeNames)
			throws AttributeRedefinitionException, FileNotFoundException, IOException, InvalidPathException,
			NoDirectoryException {
		TreeMap<String, Class<?>> a = directory(absolutePath).attributes();
		for (String attributeName : attributeNames) {
			Class<?> oldType = a.put(attributeName, type);
			if (oldType != null && oldType != type) {
				throw new AttributeRedefinitionException("attribute " + attributeName + " already defined!");
			}
		}
		return a;
	}

	/**
	 * A NoDirectoryExeception is thrown if a GraphDirectory cannot be located.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public static class NoDirectoryException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = -7305437559211218349L;

	}

	/**
	 * An ExistingDirectoryExeception is thrown if a GraphDirectory that is attempted to create already exists.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public static class ExistingDirectoryException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = 6706533956955929654L;

	}

	/**
	 * An AttributeRedefinitionException is thrown if an attribute is being defined again.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public static class AttributeRedefinitionException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = 640680337350520191L;

		/**
		 * Constructs an AttributeRedefinitionException.
		 * 
		 * @param message
		 *            the error message.
		 */
		public AttributeRedefinitionException(String message) {
			super(message);
		}
	}

}

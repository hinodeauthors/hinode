package gstar.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Map;
import java.util.TreeMap;

import gstar.MasterInterface;
import gstar.Master.ExistingGraphException;
import gstar.data.Catalog;
import gstar.data.GID;
import gstar.data.GraphDirectory;
import gstar.data.GraphDirectoryProperties;
import gstar.data.GraphPath;
import gstar.data.GraphProperties;
import gstar.data.VID;
import gstar.data.Catalog.AttributeRedefinitionException;
import gstar.data.Catalog.NoDirectoryException;
import gstar.data.GraphPath.InvalidPathException;
import gstar.query.OperatorDefinition;
import gstar.query.OperatorID;
import gstar.statistics.SystemStatistics;
import hwanglab.data.DataObject;
import hwanglab.data.DataObjectUpdateMessage;
import hwanglab.util.StringTokenizer;

/**
 * A Client can interact with the Master.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Client {

	/**
	 * The Master.
	 */
	MasterInterface master;

	/**
	 * The absolute path to the current GraphDirectory.
	 */
	protected GraphPath currentPath;

	/**
	 * The attributes defined in the current GraphDirectory.
	 */
	protected TreeMap<String, Class<?>> currentAttributes = new TreeMap<String, Class<?>>();

	/**
	 * The current GraphUpdateMessage.
	 */
	protected GraphUpdateMessage currentGraphUpdateMessage = null;

	/**
	 * A thread for sending a GraphUpdateMessage to the Master.
	 */
	protected Thread graphUpdateMessageSendingThread = null;

	/**
	 * Constructs a Client.
	 * 
	 * @param master
	 *            the Master.
	 */
	public Client(MasterInterface master) {
		this.master = master;
		try {
			currentPath = new GraphPath(GraphPath.SEPARATOR);
			Catalog catalog = null;
			while ((catalog = master.getCatalog()) == null) {
				Thread.sleep(100);
			}
			currentAttributes = catalog.directory(currentPath).attributes();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints the current working GraphDirectory.
	 * 
	 * @param out
	 *            the PrintStream.
	 */
	public synchronized void printWorkingDirectory(PrintStream out) {
		out.println(currentPath);
	}

	/**
	 * Prints the contents of the current GraphDirectory.
	 * 
	 * @param out
	 *            the PrintStream.
	 */
	public synchronized void pringWorkingDirectoryContents(PrintStream out) {
		try {
			Catalog catalog = master.getCatalog();
			GraphDirectory currentDirectory = catalog.directory(currentPath);
			for (GraphDirectory child : currentDirectory.children()) {
				GraphDirectoryProperties properties = child.properties();
				out.println(child + "   # directory" + (properties == null ? "" : (" containing " + properties)));
			}
			for (Map.Entry<String, Class<?>> e : currentDirectory.attributes().entrySet()) {
				out.println(e.getKey() + "   # " + toString(e.getValue()) + " type attribute");
			}
			TreeMap<GID, GraphProperties> graphProperties = catalog.graphProperties(currentPath);
			if (graphProperties != null)
				for (Entry<GID, GraphProperties> e : graphProperties.entrySet()) {
					out.println(e.getKey() + "   # graph containing " + e.getValue());
				}
		} catch (Exception e) {
			out.println("error retrieving current directory!");
		}
	}

	/**
	 * Changes the GraphDirectory to the specified GraphDirectory.
	 * 
	 * @param path
	 *            the path to the GraphDirectory.
	 * @return the specified GraphDirectory.
	 * @throws InvalidPathException
	 *             if an invalid path is given.
	 * @throws NoDirectoryException
	 *             if the specified GraphDirectory does not exist.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public synchronized GraphDirectory changeDirectory(String path) throws InvalidPathException, NoDirectoryException,
			Exception {
		GraphPath targetPath = null;
		targetPath = absolutePath(new GraphPath(path));
		GraphDirectory targetDirectory = master.getCatalog().directory(targetPath);
		if (targetDirectory == null)
			throw new Catalog.NoDirectoryException();
		applyCurrentGraphUpdateMessage();
		currentPath = targetPath;
		currentAttributes = targetDirectory.attributes();
		return targetDirectory;
	}

	/**
	 * Creates the specified GraphDirectory.
	 * 
	 * @param path
	 *            the name of the GraphDirectory to create.
	 * @throws Exception
	 *             if an error occurs while a new GraphDirectory is created.
	 */
	public synchronized GraphDirectory createDirectory(String path, String workerIDs) throws Exception {
		GraphPath target = absolutePath(new GraphPath(path));
		return master.createDirectory(target, workerIDs);
	}

	/**
	 * Removes the specified GraphDirectories.
	 * 
	 * @param path
	 *            the path to the GraphDirectories.
	 * @throws InvalidPathException
	 *             if an invalid path is given.
	 */
	public synchronized Collection<GraphPath> removeDirectories(String path) throws InvalidPathException {
		GraphPath targetPath = absolutePath(new GraphPath(path));
		return master.removeDirectories(targetPath);
	}

	/**
	 * Adds the specified attributes.
	 * 
	 * @param type
	 *            the type of the attributes.
	 * @param attributeNames
	 *            attribute names.
	 * @param out
	 *            the PrintStream to use.
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
	public synchronized void addAttributes(Class<?> type, PrintStream out, String... attributeNames)
			throws FileNotFoundException, AttributeRedefinitionException, IOException, InvalidPathException,
			NoDirectoryException {
		currentAttributes = master.addAttributes(currentPath, type, attributeNames);
		for (String attributeName : attributeNames) {
			out.println(attributeName + "   # " + toString(currentAttributes.get(attributeName)) + " type attribute");
		}
	}

	/**
	 * Returns the properties of the specified graph in the current GraphDirectory. Creates a new graph if none exists.
	 * 
	 * @param g
	 *            the ID of the graph.
	 * @return the properties of the specified graph in the current GraphDirectory.
	 * @throws Exception
	 *             if an error occurs while creating a new graph.
	 */
	public synchronized GraphProperties graph(GID g) throws Exception {
		prepareNewGraphUpdateMessage(g);
		GraphProperties properties = master.graph(absolutePath(currentPath), g);
		return properties;
	}

	/**
	 * Creates a graph as clone of another graph in the specified GraphDirectory.
	 * 
	 * @param g
	 *            the ID of the graph to create.
	 * @param prevG
	 *            the ID of the graph from which a new graph is constructed.
	 * @return the properties of the created graph.
	 * @throws ExistingGraphException
	 *             if the graph to create already exists.
	 * @throws Exception
	 *             if an error occurs while creating a new graph.
	 */
	public synchronized GraphProperties createGraph(GID g, GID prevG) throws ExistingGraphException, Exception {
		prepareNewGraphUpdateMessage(g);
		GraphProperties properties = master.createGraph(absolutePath(currentPath), g, prevG);
		return properties;
	}

	/**
	 * Updates the specified vertex.
	 * 
	 * @param vertexID
	 *            the ID of the vertex.
	 * @param attributes
	 *            an iterator over a series of Strings that alternate between an attribute name and an attribute value.
	 * @throws InvalidAttributeNameException
	 *             if an invalid attribute name is given.
	 * @throws ParsingException
	 *             if a paring error occurs.
	 * @throws NoGraphException
	 *             if there is an attempt to update a Vertex when the current graph is not yet specified.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public synchronized void updateVertex(String vertexID, Iterator<String> attributes)
			throws InvalidAttributeNameException, ParsingException, NoGraphException, Exception {
		if (currentGraphUpdateMessage == null)
			throw new NoGraphException();
		update(currentGraphUpdateMessage.vertexUpdateMessage(new VID(vertexID)), attributes);
	}

	/**
	 * Updates the specified edge.
	 * 
	 * @param srcID
	 *            the ID of the vertex that the edge emanates from.
	 * @param desID
	 *            the ID of the vertex that the Edge is incident to.
	 * @param attributes
	 *            an iterator over a series of Strings that alternate between an attribute name and an attribute value.
	 * @throws InvalidAttributeNameException
	 *             if an invalid attribute name is given.
	 * @throws ParsingException
	 *             if a paring error occurs.
	 * @throws NoGraphException
	 *             if there is an attempt to update an Edge when the current graph is not yet specified.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public synchronized void updateEdge(String srcID, String desID, Iterator<String> attributes)
			throws InvalidAttributeNameException, ParsingException, NoGraphException, Exception {
		if (currentGraphUpdateMessage == null)
			throw new NoGraphException();
		update(currentGraphUpdateMessage.edgeUpdateMessage(new VID(srcID), new VID(desID)), attributes);
	}

	/**
	 * Constructs Operators according to the OperatorDefinition obtained from the specified tokenizer.
	 * 
	 * @return the OperatorDefinition obtained from the specified tokenizer.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public synchronized OperatorDefinition createOperators(StringTokenizer tokenizer) throws Exception {
		OperatorDefinition definition = new OperatorDefinition(tokenizer);
		master.createOperators(absolutePath(currentPath), definition);
		return definition;
	}

	/**
	 * Returns an Iterator over the data that the specified Operator produces.
	 * 
	 * @param operatorID
	 *            an OperatorID.
	 * @return an Iterator over the data that the specified Operator produces.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public synchronized Iterator<DataObject> iterator(OperatorID operatorID) throws Exception {
		applyCurrentGraphUpdateMessage();
		return master.iterator(operatorID);
	}

	/**
	 * Discards operators that are not used any more.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public synchronized void removeCompletedOperators() throws Exception {
		master.removeCompletedOperators();
	}

	/**
	 * Checkpoints the state of the whole system.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public synchronized void checkpoint() throws Exception {
		applyCurrentGraphUpdateMessage();
		master.checkpoint();
	}

	/**
	 * Shuts down the whole system.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public synchronized void shutdown() throws Exception {
		applyCurrentGraphUpdateMessage();
		master.shutdown();
	}

	/**
	 * Returns the system catalog.
	 * 
	 * @return the system catalog.
	 */
	public synchronized Catalog getCatalog() throws Exception {
		return master.getCatalog();
	}

	/**
	 * Returns the current system statistics.
	 * 
	 * @return the current system statistics.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public SystemStatistics currentStatistics() throws Exception {
		return master.currentStatistics();
	}

	/**
	 * Executes the query consisting of the specified Operators.
	 * 
	 * @param operatorDefinitions
	 *            a collection of Strings that define Operators.
	 * @return an iterator over the results of the query.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public synchronized Iterator<DataObject> runQuery(String[] operatorDefinitions) throws Exception {
		OperatorDefinition last = null;
		for (String operatorDefinition : operatorDefinitions) {
			last = createOperators(new StringTokenizer(operatorDefinition, "@()[]=,", '\"', '#'));
		}
		return last != null ? iterator(last.operatorID()) : null;
	}

	/**
	 * Updates the specified DataObjectUpdateMessage using the specified attributes.
	 * 
	 * @param message
	 *            the DataObjectUpdateMessage to update.
	 * @param attributes
	 *            an iterator over a series of Strings that alternate between an attribute name and an attribute value.
	 * @throws ParsingException
	 *             if a paring error occurs.
	 */
	protected synchronized void update(DataObjectUpdateMessage message, Iterator<String> attributes)
			throws ParsingException {
		while (attributes.hasNext()) {
			String attributeName = attributes.next();
			if (!"=".equals(attributes.next()))
				throw new ParsingException("= is missing");
			String token = attributes.next();
			if (token == null)
				throw new ParsingException("the value for " + attributeName + " is missing.");
			Class<?> type = currentAttributes.get(attributeName);
			if (type == null)
				throw new ParsingException("attribute " + attributeName + " is not yet defined.");
			try {
				message.update(attributeName, newInstance(type, token));
			} catch (Exception e) {
				throw new ParsingException("cannot create the value for " + attributeName + " from " + token + ".");
			}
		}
		if (graphUpdateMessageSendingThread == null) { // if no thread is ready
			graphUpdateMessageSendingThread = new Thread() {
				public void run() {
					try {
						Thread.sleep(1000); // wait for a second
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					graphUpdateMessageSendingThread = null;
					try {
						applyCurrentGraphUpdateMessage(); // send the update message to the master
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			graphUpdateMessageSendingThread.start();
		}
		if (currentGraphUpdateMessage.numEdgeUpdateMessages() >= 10000) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Prepares a new GraphUpdateMessage.
	 * 
	 * @param g
	 *            the ID of the graph to consume the new GraphUpdateMessage.
	 * @throws Exception
	 *             if an error occurs in sending the remaining GraphUpdateMessage if any.
	 */
	protected void prepareNewGraphUpdateMessage(GID g) throws Exception {
		applyCurrentGraphUpdateMessage();
		currentGraphUpdateMessage = new GraphUpdateMessage(g);
	}

	/**
	 * Returns the absolute path for the specified path.
	 * 
	 * @param path
	 *            the path for which the absolute path is found.
	 * @return the absolute path for the specified path.
	 * @throws InvalidPathException
	 *             if an invalid path is given.
	 */
	protected GraphPath absolutePath(GraphPath path) throws InvalidPathException {
		if (path.isRelative()) {
			path = currentPath.concatenate(path);
		}
		return path;
	}

	/**
	 * Returns the String representation of the specified type.
	 * 
	 * @param type
	 *            the type.
	 * @return the String representation of the specified type.
	 */
	protected String toString(Class<?> type) {
		if (type == int.class)
			return "int";
		else if (type == double.class)
			return "double";
		else if (type == String.class)
			return "string";
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * Constructs an object of the specified type.
	 * 
	 * @param type
	 *            the type of the object to construct.
	 * @param value
	 *            the String representation of the value.
	 * @return the constructed object.
	 */
	protected Object newInstance(Class<?> type, String value) {
		if (type == int.class)
			return new Integer(value);
		else if (type == double.class)
			return new Double(value);
		else if (type == String.class)
			return value;
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * Sends the current GraphUpdateMessage to the Master if it is not empty.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	protected synchronized void applyCurrentGraphUpdateMessage() throws Exception {
		if (currentGraphUpdateMessage != null && !currentGraphUpdateMessage.isEmpty()) {
			master.updateGraph(currentPath, currentGraphUpdateMessage);
			try {
				notify();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			currentGraphUpdateMessage.clear();
		}
	}

	/**
	 * An InvalidAttributeNameException is thrown if an invalid attribute name is used.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public static class InvalidAttributeNameException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = 6421768903564442933L;

		/**
		 * Constructs an InvalidAttributeNameException.
		 * 
		 * @param message
		 *            the error message.
		 */
		public InvalidAttributeNameException(String message) {
			super(message);
		}
	}

	/**
	 * An ParsingException is thrown if a parsing error occurs.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public static class ParsingException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = 6421768903564442933L;

		/**
		 * Constructs an ParsingException.
		 * 
		 * @param message
		 *            the error message.
		 */
		public ParsingException(String message) {
			super(message);
		}
	}

	/**
	 * A NoGraphException is thrown if there is an attempt to update a Vertex or an Edge when the current graph is not
	 * yet specified.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public static class NoGraphException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = 6421768903564442933L;

	}

}

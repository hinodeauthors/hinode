package gstar;

import gstar.Master.ExistingGraphException;
import gstar.client.GraphUpdateMessage;
import gstar.data.Catalog;
import gstar.data.GID;
import gstar.data.GraphDirectory;
import gstar.data.GraphPath;
import gstar.data.GraphProperties;
import gstar.data.Catalog.AttributeRedefinitionException;
import gstar.data.Catalog.NoDirectoryException;
import gstar.data.GraphPath.InvalidPathException;
import gstar.query.OperatorDefinition;
import gstar.query.OperatorID;
import gstar.statistics.SystemStatistics;
import hwanglab.data.DataObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

public interface MasterInterface extends hwanglab.system.MasterInterface {

	/**
	 * Returns the system catalog.
	 * 
	 * @return the system catalog.
	 */
	public Catalog getCatalog();

	/**
	 * Creates the specified directory.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @param workerIDs
	 *            the IDs of the workers that will store the graph data.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public GraphDirectory createDirectory(GraphPath absolutePath, String workerIDs) throws Exception;

	/**
	 * Removes the specified GraphDirectories.
	 * 
	 * @param absolutePath
	 *            the path to the GraphDirectories to remove.
	 * @return the absolute paths to the deleted GraphDirectories.
	 */
	public Collection<GraphPath> removeDirectories(GraphPath absolutePath);

	/**
	 * Adds the specified attributes in the specified directory.
	 * 
	 * @param absolutePath
	 *            the absolute path to the directory.
	 * @param type
	 *            the type of the attributes.
	 * @param attributeNames
	 *            attribute names.
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
			throws FileNotFoundException, AttributeRedefinitionException, IOException, InvalidPathException,
			NoDirectoryException;

	/**
	 * Returns the properties of the specified graph. Creates a new graph if none exists.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory that the graph belongs to.
	 * @param g
	 *            the ID of the graph.
	 * @return the properties of the the specified graph.
	 * @throws Exception
	 *             if an error occurs while creating a new graph.
	 */
	public GraphProperties graph(GraphPath absolutePath, GID g) throws Exception;

	/**
	 * Creates a Graph.
	 * 
	 * @param absolutePath
	 * @param g
	 *            the ID of the new graph.
	 * @return the properties of the created Graph.
	 * @throws ExistingGraphException
	 *             if the graph to create already exists.
	 * @throws Exception
	 *             if an error occurs while creating a new graph.
	 */
	public GraphProperties createGraph(GraphPath absolutePath, GID g, GID prevG) throws ExistingGraphException,
			Exception;

	/**
	 * Adds the specified graph elements into the specified graph.
	 * 
	 * @param absolutePath
	 * @param message
	 *            the GraphUpdateMessage.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void updateGraph(GraphPath absolutePath, GraphUpdateMessage message) throws Exception;

	/**
	 * Checkpoints all workers.
	 * 
	 * @throws Exception
	 */
	public void checkpoint() throws Exception;

	/**
	 * Constructs Operators according to the specified OperatorDefinition.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory that stores data for the Operators.
	 * @param definition
	 *            the OperatorDefinition.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void createOperators(GraphPath absolutePath, OperatorDefinition definition) throws Exception;

	/**
	 * Returns an Iterator over the data that the specified Operator produces.
	 * 
	 * @param operatorID
	 *            an OperatorID.
	 * @return an Iterator over the data that the specified Operator produces.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Iterator<DataObject> iterator(OperatorID operatorID) throws Exception;

	/**
	 * Requests the query coordinator to make a decision on the specified BSP operation.
	 * 
	 * @param opID
	 *            the ID of the requester operator.
	 * @param workerID
	 *            the ID of the worker that runs the requester operator.
	 * @param superStepID
	 *            the super step ID.
	 * @param voteForCompletion
	 *            a flag indicating whether or not the operator votes for completion.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void requestsBSPDecision(String opID, int workerID, int superStepID, boolean voteForCompletion)
			throws Exception;

	/**
	 * Discards operators that are not used any more.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void removeCompletedOperators() throws Exception;

	/**
	 * Returns the current system statistics.
	 * 
	 * @return the current system statistics.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public SystemStatistics currentStatistics() throws Exception;

}

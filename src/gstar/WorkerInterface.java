package gstar;

import gstar.data.GID;
import gstar.data.GraphDirectoryProperties;
import gstar.data.GraphPath;
import gstar.data.GraphProperties;
import gstar.data.VertexUpdateMessage;
import gstar.query.OperatorDefinition;
import gstar.query.OperatorID;
import gstar.query.operator.SummaryMessage;
import gstar.statistics.SystemStatistics;
import hwanglab.data.DataObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * WorkerInterface is the interface for Workers.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public interface WorkerInterface extends hwanglab.system.WorkerInterface {

	/**
	 * Removes the data stored in the specified GraphDirectory.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void clear(GraphPath absolutePath) throws Exception;

	/**
	 * Changes the current GraphDirectory.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws ClassNotFoundException
	 *             if a class cannot be found.
	 */
	public void changeGraphDirectory(GraphPath absolutePath) throws ClassNotFoundException, IOException;

	/**
	 * Constructs a new graph.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @param g
	 *            the ID of the new graph.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void createGraph(GraphPath absolutePath, GID g) throws Exception;

	/**
	 * Constructs a new graph as a clone of the specified graph.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @param g
	 *            the ID of the new graph.
	 * @param prevG
	 *            the ID of the previous graph.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void createGraph(GraphPath absolutePath, GID g, GID prevG) throws Exception;

	/**
	 * Updates the specified graph.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @param g
	 *            the ID of the graph.
	 * @param messages
	 *            the VertexUpdateMessages.
	 * @return a GraphProperties instance representing the properties of the specified graph.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public GraphProperties updateGraph(GraphPath absolutePath, GID g, Collection<VertexUpdateMessage> messages)
			throws Exception;

	/**
	 * Saves the data managed by this Worker.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public GraphDirectoryProperties checkpoint() throws Exception;

	/**
	 * Constructs an Operator according to the specified OperatorDefinition.
	 * 
	 * @param definition
	 *            the definition of the Operator to create.
	 * @param inputOperators
	 *            the IDs of the input Operators.
	 * @param workerIDs
	 *            the IDs of the Workers to collaborate with.
	 * @param graphProperties
	 *            the properties of related graphs.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void createOperator(OperatorDefinition definition, Vector<OperatorID> inputOperators, int[] workerIDs,
			Map<GID, GraphProperties> graphProperties) throws Exception;

	/**
	 * Removes the completed Operators.
	 */
	public void removeCompletedOperators();

	/**
	 * Returns an iterator from the specified Operator.
	 * 
	 * @param operatorName
	 *            the name of the Operator.
	 * @return an iterator over the output data of the Operator.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Iterator<DataObject> iterator(String operatorName) throws Exception;

	/**
	 * Handles the specified SummaryMessages.
	 * 
	 * @param operatorName
	 *            the name of the Operator.
	 * @param summaryMessages
	 *            the SummaryMessages to handle.
	 */
	public void handle(String operatorName, Collection<SummaryMessage<?, ?>> summaryMessages);

	/**
	 * Starts a new super step for the specified Operator.
	 * 
	 * @param operatorName
	 *            the name of the Operator.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void startSuperStep(String operatorName) throws Exception;

	/**
	 * Finalize the BSP operation for the specified Operator.
	 * 
	 * @param operatorName
	 *            the name of the Operator.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void finalizeBSP(String operatorName) throws Exception;

	/**
	 * Returns the current Statistics.
	 * 
	 * @return the current Statistics.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public SystemStatistics currentStatistics() throws Exception;

}

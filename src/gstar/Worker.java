package gstar;

import gstar.data.GID;
import gstar.data.GraphDirectoryProperties;
import gstar.data.GraphManager;
import gstar.data.GraphPath;
import gstar.data.GraphProperties;
import gstar.data.VID;
import gstar.data.VertexUpdateMessage;
import gstar.query.OperatorDefinition;
import gstar.query.OperatorID;
import gstar.query.QueryEngine;
import gstar.query.operator.SummaryMessage;
import gstar.statistics.SystemStatistics;
import gstar.statistics.WorkerStatistics;
import hwanglab.data.DataObject;
import hwanglab.net.CommunicationException;
import hwanglab.net.LookupException;
import hwanglab.system.LookupServiceInterface;
import hwanglab.util.JVMMonitor;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * A Workers manages a subset of the whole graph data.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Worker extends hwanglab.system.Worker implements WorkerInterface {

	/**
	 * The GraphManager for this Worker.
	 */
	protected GraphManager graphManager;

	/**
	 * The QueryEngine for this Worker.
	 */
	protected QueryEngine queryEngine;

	/**
	 * The JVMMonitor for this Worker.
	 */
	protected JVMMonitor monitor = new JVMMonitor();

	/**
	 * Constructs a Worker
	 * 
	 * @param workerID
	 *            the ID of the Worker.
	 * @param workerAddress
	 *            the address of the Worker.
	 * @param masterAddress
	 *            the address of the Master.
	 * @param lookupService
	 *            the lookupService for the Worker.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Worker(int workerID, String workerAddress, String masterAddress, LookupServiceInterface lookupService)
			throws Exception {
		super(workerID, workerAddress, masterAddress, MasterInterface.class, lookupService);
	}

	/**
	 * Returns the Master.
	 * 
	 * @return the Master.
	 */
	public MasterInterface master() {
		return (MasterInterface) master;
	}

	/**
	 * Returns the GraphManager for this Worker.
	 * 
	 * @return the GraphManager for this Worker.
	 */
	public GraphManager graphManager() {
		return graphManager;
	}

	/**
	 * Returns the QueryEngine for this Worker.
	 * 
	 * @return the QueryEngine for this Worker.
	 */
	public QueryEngine queryEngine() {
		return queryEngine;
	}

	/**
	 * Returns the specified Worker.
	 * 
	 * @param workerID
	 *            the ID of the Worker.
	 * @return the specified Worker.
	 * @throws LookupException
	 *             if the Worker cannot be looked up.
	 * @throws CommunicationException
	 *             if the communication fails.
	 */
	public WorkerInterface worker(int workerID) throws LookupException, CommunicationException {
		return (WorkerInterface) super.worker(workerID);
	}

	/**
	 * Returns the ID of the Worker that stores the specified Vertex.
	 * 
	 * @param vertexID
	 *            the ID of the Vertex.
	 * @param workerIDs
	 *            the IDs of the Workers that store data.
	 * @return the ID of the Worker that stores the specified Vertex.
	 */
	public static int workerID(VID vertexID, int[] workerIDs) {
		return workerIDs[Math.abs(vertexID.hashCode()) % workerIDs.length];
	}

	@Override
	public synchronized void clear(GraphPath graphFolderName) throws IOException, ClassNotFoundException {
		graphManager.reset(graphFolderName);
		System.gc();
	}

	@Override
	public void changeGraphDirectory(GraphPath graphFolderName) throws ClassNotFoundException, IOException {
		graphManager.changeGraphPath(graphFolderName);
	}

	@Override
	public void createGraph(GraphPath graphFolderName, GID g) throws Exception {
		graphManager.createGraph(graphFolderName, g);
	}

	@Override
	public void createGraph(GraphPath graphFolderName, GID newG, GID prevG) throws Exception {
		graphManager.createGraph(graphFolderName, newG, prevG);
	}

	@Override
	public GraphProperties updateGraph(GraphPath graphFolderName, GID g, Collection<VertexUpdateMessage> messages)
			throws Exception {
		GraphProperties p = graphManager.updateGraph(graphFolderName, g, messages);
		return p;
	}

	@Override
	public GraphDirectoryProperties checkpoint() throws Exception {
		GraphDirectoryProperties p = graphManager.checkpoint();
		return p;
	}

	@Override
	public void createOperator(OperatorDefinition description, Vector<OperatorID> inputs, int[] workerIDs,
			Map<GID, GraphProperties> graphProperties) throws Exception {
		queryEngine.createOperator(description, inputs, workerIDs, graphProperties);
	}

	@Override
	public void removeCompletedOperators() {
		queryEngine.removeCompletedOperators();
	}

	@Override
	public Iterator<DataObject> iterator(String operatorID) throws Exception {
		return queryEngine.iterator(operatorID);
	}

	@Override
	public void handle(String opID, Collection<SummaryMessage<?, ?>> messages) {
		queryEngine.handle(opID, messages);
	}

	@Override
	public void shutdown() {
		graphManager.shutdown();
		queryEngine.shutdown();
		super.shutdown();
	}

	@Override
	public void startSuperStep(String opID) throws Exception {
		queryEngine.startSuperStep(opID);
	}

	@Override
	public void finalizeBSP(String opID) throws Exception {
		queryEngine.finalizeBSP(opID);
	}

	@Override
	public SystemStatistics currentStatistics() throws Exception {
		SystemStatistics stat = new SystemStatistics();
		stat.update(queryEngine.operatorStatistics());
		stat.update(new WorkerStatistics(workerID, monitor, graphManager.storageManager()));
		return stat;
	}

	/**
	 * Returns the Worker that stores the specified Vertex.
	 * 
	 * @param vertexID
	 *            the ID of the Vertex.
	 * @param workerIDs
	 *            the IDs of the Workers that store data.
	 * @return the Worker that stores the specified Vertex.
	 * @throws LookupException
	 *             if a Worker cannot be looked up.
	 * @throws CommunicationException
	 *             if the communication fails.
	 */
	protected WorkerInterface worker(VID vertexID, int[] workerIDs) throws LookupException, CommunicationException {
		return worker(workerID(vertexID, workerIDs));
	}

	@Override
	protected void initialize(hwanglab.system.Configuration configuration) throws Exception {
		Configuration c = (Configuration) configuration;
		graphManager = new GraphManager(workerID, c.systemDirectory(), c.bufferSize());
		queryEngine = new QueryEngine(this);
		System.out.println(this + " started its graph manager and query engine.");
	}

	/**
	 * Starts a Worker.
	 * 
	 * @param args
	 *            the arguments.
	 * @throws NumberFormatException
	 *             if there is a problem in the number format.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public static void main(String[] args) throws NumberFormatException, Exception {
		String[] workerAddress = args[1].split(":");
		int port = Integer.parseInt(workerAddress[workerAddress.length - 1]);
		new Worker(Integer.parseInt(args[0]), args[1], args[2], new hwanglab.system.real.LookupService(port));
	}

}
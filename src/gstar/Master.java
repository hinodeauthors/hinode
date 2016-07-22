package gstar;

import gstar.client.GraphUpdateMessage;
import gstar.data.Catalog;
import gstar.data.GID;
import gstar.data.GraphDirectory;
import gstar.data.GraphPath;
import gstar.data.GraphProperties;
import gstar.data.VertexUpdateMessage;
import gstar.data.Catalog.AttributeRedefinitionException;
import gstar.data.Catalog.NoDirectoryException;
import gstar.data.GraphPath.InvalidPathException;
import gstar.data.VID;
import gstar.query.OperatorDefinition;
import gstar.query.OperatorID;
import gstar.query.QueryCoordinator;
import gstar.statistics.SystemStatistics;
import hwanglab.data.DataObject;
import hwanglab.util.ParallelExecutor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * A Master manages the whole G* system.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 */
public class Master extends hwanglab.system.Master implements MasterInterface {

	/**
	 * The QueryCoordinator for this Master.
	 */
	protected QueryCoordinator queryCoordinator;

	/**
	 * The Catalog.
	 */
	protected Catalog catalog;

	/**
	 * Constructs a Master.
	 * 
	 * @param configuration
	 *            the system configuration.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Master(Configuration configuration) throws Exception {
		super(configuration, WorkerInterface.class, Worker.class);
		queryCoordinator = new QueryCoordinator(this);
		catalog = new Catalog(configuration.systemDirectory()
				+ (configuration.systemDirectory().endsWith(File.separator) ? "" : File.separator) + "catalog",
				workerIDs(configuration.numberWorkers()));
		new Thread() {
			public void run() {
				synchronized (Master.this) {
					try {
						Master.this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	@Override
	public Catalog getCatalog() {
		return catalog;
	}

	@Override
	public GraphDirectory createDirectory(final GraphPath absolutePath, String workerIDs) throws Exception {
		GraphDirectory directory = catalog.createDirectory(absolutePath, workerIDs == null ? workerIDs(workers.size())
				: workerIDs(workerIDs));
		ParallelExecutor executor = new ParallelExecutor();
		for (final Integer i : directory.workerIDs()) {
			executor.add(new ParallelExecutor.Task() {
				@Override
				public void run() throws Exception {
					((WorkerInterface) workers.get(i)).clear(absolutePath);
				}
			});
		}
		executor.run();
		checkpoint();
		return directory;
	}

	@Override
	public Collection<GraphPath> removeDirectories(GraphPath absolutePath) {
		LinkedList<GraphPath> paths = new LinkedList<GraphPath>();
		Collection<GraphDirectory> directories = catalog.directories(absolutePath);
		for (GraphDirectory directory : directories) {
			paths.add(directory.absolutePath());
			directory.remove();
		}
		return paths;
	}

	@Override
	public TreeMap<String, Class<?>> addAttributes(GraphPath absolutePath, Class<?> type, String... attributeNames)
			throws FileNotFoundException, AttributeRedefinitionException, IOException, InvalidPathException,
			NoDirectoryException {
		return catalog.addAttributes(absolutePath, type, attributeNames);
	}

	@Override
	public synchronized GraphProperties graph(final GraphPath absolutePath, final GID g) throws Exception {
		GraphDirectory directory = catalog.directory(absolutePath);
		GraphProperties properties = directory.graphProperties().get(g);
		if (properties != null)
			return properties;
		ParallelExecutor executor = new ParallelExecutor();
		for (final Integer i : directory.workerIDs()) {
			executor.add(new ParallelExecutor.Task() {
				@Override
				public void run() throws Exception {
					((WorkerInterface) workers.get(i)).createGraph(absolutePath, g);
				}
			});
		}
		executor.run();
		properties = new GraphProperties(absolutePath, g, 0, 0);
		catalog.update(properties);
		checkpoint();
		System.out.println(this + " has created an empty graph " + g + ".");
		return properties;
	}

	@Override
	public synchronized GraphProperties createGraph(final GraphPath path, final GID g, final GID prevG)
			throws ExistingGraphException, Exception {
		GraphDirectory directory = catalog.directory(path);
		if (directory.graphProperties().containsKey(g))
			throw new ExistingGraphException();
		ParallelExecutor executor = new ParallelExecutor();
		for (final Integer i : directory.workerIDs()) {
			executor.add(new ParallelExecutor.Task() {
				@Override
				public void run() throws Exception {
					((WorkerInterface) workers.get(i)).createGraph(path, g, prevG);
				}
			});
		}
		executor.run();
		GraphProperties properties = catalog.graphProperties(path, prevG);
		properties = new GraphProperties(path, g, properties.numVertices(), properties.numEdges());
		catalog.update(properties);
		checkpoint();
		System.out.println(this + " has created graph " + g + " which contains " + properties.numVertices()
				+ " vertices and " + properties.numEdges() + " edges.");
		return properties;
	}

	@Override
	public synchronized void updateGraph(final GraphPath path, GraphUpdateMessage message) throws Exception {
		final GID g = message.graphID();
		GraphDirectory folder = catalog.directory(path);
		Map<Integer, Collection<VertexUpdateMessage>> worker2messages = new HashMap<Integer, Collection<VertexUpdateMessage>>();
		for (Entry<VID, VertexUpdateMessage> m : message.vertexUpdateMessages()) {
			update(worker2messages, g, m.getKey(), m.getValue(), folder.workerIDs());
		}
		ParallelExecutor executor = new ParallelExecutor();
		for (final Entry<Integer, Collection<VertexUpdateMessage>> entry : worker2messages.entrySet()) {
			executor.add(new ParallelExecutor.Task() {
				@Override
				public void run() throws Exception {
					hwanglab.system.WorkerInterface proxy = workers.get(entry.getKey());
					catalog.update(((WorkerInterface) proxy).updateGraph(path, g, entry.getValue()));
				}
			});
		}
		executor.run();
		catalog.checkpoint();
		GraphProperties properties = folder.graphProperties().get(message.graphID());
		System.out.println("graph " + message.graphID() + " has " + properties.numVertices() + " vertices and "
				+ properties.numEdges() + " edges.");
	}

	@Override
	public synchronized void checkpoint() throws Exception {
		ParallelExecutor executor = new ParallelExecutor();
		for (final hwanglab.system.WorkerInterface proxy : workers()) {
			executor.add(new ParallelExecutor.Task() {
				@Override
				public void run() throws Exception {
					catalog.update(((WorkerInterface) proxy).checkpoint());
				}
			});
		}
		executor.run();
		catalog.checkpoint();
	}

	@Override
	public void createOperators(GraphPath path, OperatorDefinition definition) throws Exception {
		queryCoordinator.createOperators(path, definition, catalog.directory(path).graphProperties());
		System.out.println(this + " has created " + definition + ".");
	}

	@Override
	public Iterator<DataObject> iterator(OperatorID operatorID) throws Exception {
		System.out.println(this + " has started " + operatorID + ".");
		return worker(operatorID.workerID()).iterator(operatorID.operatorName());
	}

	@Override
	public void requestsBSPDecision(String opID, int workerID, int superStepID, boolean voteForCompletion)
			throws Exception {
		this.queryCoordinator.makeBSPDecision(opID, workerID, superStepID, voteForCompletion);
	}

	@Override
	public void removeCompletedOperators() throws Exception {
		queryCoordinator.removeCompletedOperators();
	}

	@Override
	public SystemStatistics currentStatistics() throws Exception {
		final SystemStatistics statistics = new SystemStatistics();
		ParallelExecutor executor = new ParallelExecutor();
		for (final hwanglab.system.WorkerInterface proxy : workers()) {
			executor.add(new ParallelExecutor.Task() {

				@Override
				public void run() throws Exception {
					SystemStatistics stat = ((WorkerInterface) proxy).currentStatistics();
					statistics.update(stat);
				}
			});
		}
		executor.run();
		return statistics;
	}

	/**
	 * Returns the specified Worker.
	 * 
	 * @param workerID
	 *            the ID of the Worker.
	 * @return the specified Worker.
	 * @throws InvalidWorkerIDException
	 *             if an invalid worker ID is given.
	 */
	public WorkerInterface worker(int workerID) throws InvalidWorkerIDException {
		WorkerInterface worker = (WorkerInterface) workers.get(workerID);
		if (worker == null)
			throw new InvalidWorkerIDException(workerID);
		else
			return worker;
	}

	/**
	 * An InvalidWorkerIDException is thrown if an invalid worker ID is given.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 * 
	 */
	public static class InvalidWorkerIDException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = -6040469951475860012L;

		/**
		 * Constructs an InvalidWorkerIDException.
		 * 
		 * @param workerID
		 */
		public InvalidWorkerIDException(int workerID) {
			super(workerID + ": invalid worker ID");
		}

	}

	/**
	 * Returns the IDs of the Workers that store the data in the specified GraphDirectory.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 * @return the IDs of the Workers that store the data in the specified GraphDirectory.
	 * @throws InvalidPathException
	 *             if an invalid path is given.
	 * @throws NoDirectoryException
	 *             if the specified GraphDirectory does not exist.
	 */
	public int[] workerIDs(GraphPath absolutePath) throws InvalidPathException, NoDirectoryException {
		return catalog.directory(absolutePath).workerIDs();
	}

	/**
	 * Returns the specified Worker IDs.
	 * 
	 * @param workerIDs
	 *            the IDs of the Workers.
	 * @return the specified Worker IDs.
	 */
	protected static int[] workerIDs(String workerIDs) {
		String[] tokens = workerIDs.split("-");
		if (tokens.length == 1) {
			return new int[] { Integer.parseInt(tokens[0]) };
		} else if (tokens.length == 2) {
			int start = Integer.parseInt(tokens[0]);
			int end = Integer.parseInt(tokens[1]);
			int[] ids = new int[end - start + 1];
			for (int i = start; i <= end; i++) {
				try {
					ids[i - start] = i;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return ids;
		} else
			throw new UnsupportedOperationException();
	}

	/**
	 * An ExistingGraphExeception is thrown if a Graph to create already exists.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public static class ExistingGraphException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = 4118567177327994280L;

	}

	@Override
	public synchronized void shutdown() {
//		if (catalog.changed())
			try {
				checkpoint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		super.shutdown();
		notify();
	}

	/**
	 * Updates the specified collection of VertexUpdateMessages.
	 * 
	 * @param worker2messages
	 *            a map between worker IDs and VertexUpdateMessages.
	 * @param graphID
	 *            the ID of the graph.
	 * @param vertexID
	 *            the ID of the vertex.
	 * @param message
	 *            the VertexUpdateMessage.
	 * @param workerIDs
	 *            the IDs of the Workers.
	 */
	protected void update(Map<Integer, Collection<VertexUpdateMessage>> worker2messages, GID graphID, VID vertexID,
			VertexUpdateMessage message, int[] workerIDs) {
		int workerID = Worker.workerID(vertexID, workerIDs);
		Collection<VertexUpdateMessage> messages = worker2messages.get(workerID);
		if (messages == null) {
			messages = new java.util.LinkedList<VertexUpdateMessage>();
			worker2messages.put(workerID, messages);
		}
		messages.add(message);
	}

	/**
	 * Returns the specified Worker IDs.
	 * 
	 * @param workers
	 *            the number of Workers.
	 * @return the specified Worker IDs.
	 */
	protected int[] workerIDs(int workers) {
		int[] workerIDs = new int[workers];
		for (int i = 0; i < workers; i++)
			workerIDs[i] = i;
		return workerIDs;
	}

	/**
	 * Starts the Master.
	 * 
	 * @param args
	 *            the arguments.
	 * @throws NumberFormatException
	 *             if an invalid port number is specified.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public static void main(String[] args) throws NumberFormatException, Exception {
		Configuration config = new Configuration();
		config.update(args);
		config.update("-real-execution"); // forces real execution
		new Master(config);
	}

}
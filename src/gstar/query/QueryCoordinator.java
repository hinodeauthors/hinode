package gstar.query;

import gstar.Master;
import gstar.data.GID;
import gstar.data.GraphPath;
import gstar.data.GraphProperties;
import hwanglab.util.ParallelExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

/**
 * The QueryCoordinator executes queries using multiple Workers.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class QueryCoordinator {

	/**
	 * The Master.
	 */
	Master master;

	/**
	 * The IDs of the Workers that runs each operator.
	 */
	Map<String, int[]> operator2workerIDs = new HashMap<String, int[]>();

	/**
	 * The requests for BSP operations.
	 */
	HashMap<String, Map<Integer, Boolean>> bspRequests = new HashMap<String, Map<Integer, Boolean>>();

	/**
	 * Constructs a QueryCoordinator.
	 * 
	 * @param master
	 *            the Master.
	 */
	public QueryCoordinator(Master master) {
		this.master = master;
	}

	/**
	 * Constructs Operators according to the specified OperatorDefinition.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory that stores data for the Operators.
	 * @param definition
	 *            the OperatorDefinition.
	 * @param graphProperties
	 *            the properties of related graphs.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void createOperators(GraphPath absolutePath, OperatorDefinition definition,
			final Map<GID, GraphProperties> graphProperties) throws Exception {
		final int[] workerIDs = master.workerIDs(absolutePath);
		changeGraphDirectory(absolutePath, workerIDs);
		if (definition.workerID() == OperatorID.WORKER_WILDCARD) {
			ParallelExecutor executor = new ParallelExecutor();
			operator2workerIDs.put(definition.operatorName(), workerIDs);
			for (int workerID : workerIDs) {
				final OperatorDefinition newDefinition = new OperatorDefinition(definition.operatorName(), workerID,
						definition.type(), definition.inputOperators(), definition.arguments());
				executor.add(new ParallelExecutor.Task() {
					@Override
					public void run() throws Exception {
						createOperator(newDefinition, workerIDs, graphProperties);
					}
				});
			}
			executor.run();
		} else {
			createOperator(definition, workerIDs, graphProperties);
		}
	}

	/**
	 * Removes the completed Operators.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void removeCompletedOperators() throws Exception {
		ParallelExecutor executor = new ParallelExecutor();
		for (final hwanglab.system.WorkerInterface worker : master.workers()) {
			executor.add(new ParallelExecutor.Task() {
				@Override
				public void run() throws Exception {
					((gstar.WorkerInterface) worker).removeCompletedOperators();
				}
			});
		}
		executor.run();
	}

	/**
	 * Makes a decision on the specified BSP operation.
	 * 
	 * @param operatorName
	 *            the name of the Operator.
	 * @param workerID
	 *            the ID of the worker that runs the Operator.
	 * @param superStepNumber
	 *            the super step number.
	 * @param voteForCompletion
	 *            a flag indicating whether or not the Operator votes for completion.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void makeBSPDecision(String operatorName, int workerID, int superStepNumber, boolean voteForCompletion)
			throws Exception {
		synchronized (bspRequests) {
			String key = operatorName + ":" + superStepNumber;
			Map<Integer, Boolean> worker2vote = bspRequests.get(key);
			if (worker2vote == null) {
				worker2vote = new HashMap<Integer, Boolean>();
				bspRequests.put(key, worker2vote);
			}
			worker2vote.put(workerID, voteForCompletion);
			if (worker2vote.size() == operator2workerIDs.get(operatorName).length) { // if received all needed votes
				System.out.println(master + " completes super step " + superStepNumber
						+ " for operators whose IDs are " + operatorName + " on " + worker2vote.size() + " workers.");
				Map<Integer, Boolean> votes = bspRequests.remove(key);
				for (Entry<Integer, Boolean> e : votes.entrySet()) {
					if (!e.getValue()) {
						startSuperStep(operatorName, superStepNumber);
						return;
					}
				}
				finalizeBSP(operatorName);
			}
		}
	}

	/**
	 * Constructs an Operator based on the specified definition.
	 * 
	 * @param definition
	 *            the definition of an operator to create.
	 * @param workerIDs
	 *            the IDs of the Workers to collaborate with.
	 * @param graphProperties
	 *            the properties of the related graphs.
	 * @throws Exception
	 *             if an exception occurs.
	 */
	protected void createOperator(OperatorDefinition definition, int[] workerIDs,
			Map<GID, GraphProperties> graphProperties) throws Exception {
		Vector<OperatorID> inputs = new Vector<OperatorID>();
		for (OperatorID source : definition.inputOperators()) {
			if (source.workerID() == OperatorID.WORKER_WILDCARD) {
				for (int workerID : workerIDs) {
					inputs.add(new OperatorID(source.operatorName(), workerID));
				}
			} else if (source.workerID() == OperatorID.LOCAL_WORKER) {
				inputs.add(new OperatorID(source.operatorName(), definition.workerID()));
			} else {
				inputs.add(source);
			}
		}
		master.worker(definition.workerID()).createOperator(definition, inputs, workerIDs, graphProperties);
	}

	/**
	 * Changes the current GraphDirectory.
	 * 
	 * @param absolutePath
	 *            the absolute path to the GraphDirectory.
	 */
	protected void changeGraphDirectory(final GraphPath absolutePath, int[] workerIDs) throws Exception {
		ParallelExecutor executor = new ParallelExecutor();
		for (final int workerID : workerIDs) {
			executor.add(new ParallelExecutor.Task() {
				@Override
				public void run() throws Exception {
					master.worker(workerID).changeGraphDirectory(absolutePath);
				}
			});
		}
		executor.run();
	}

	/**
	 * Starts a super step for the specified Operators.
	 * 
	 * @param operatorName
	 *            the common name of the Operators.
	 * @param superStepNumber
	 *            the super step number.
	 * @throws Exception
	 *             if an error occurs.
	 */
	protected void startSuperStep(final String operatorName, final int superStepNumber) {
		new Thread() {
			public void run() {
				ParallelExecutor executor = new ParallelExecutor();
				for (final int workerID : operator2workerIDs.get(operatorName)) {
					executor.add(new ParallelExecutor.Task() {
						@Override
						public void run() throws Exception {
							master.worker(workerID).startSuperStep(operatorName);
						}
					});
				}
				try {
					executor.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}.start();
	}

	/**
	 * Finalizes the BSP operation on the specified Operators.
	 * 
	 * @param operatorName
	 *            the common name of the Operators.
	 */
	protected void finalizeBSP(final String operatorName) {
		new Thread() {
			public void run() {
				ParallelExecutor executor = new ParallelExecutor();
				for (final int workerID : operator2workerIDs.get(operatorName)) {
					executor.add(new ParallelExecutor.Task() {
						@Override
						public void run() throws Exception {
							master.worker(workerID).finalizeBSP(operatorName);
						}
					});
				}
				try {
					executor.run();
					bspRequests.remove(operatorName);
					System.out.println(master + " completes the BSP operation on Operators " + operatorName + "@*.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}.start();
	}

}

package gstar.query.operator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

import gstar.Worker;
import gstar.data.Vertex;
import gstar.query.summary.Summary;
import gstar.query.summary.AggregateManager;
import hwanglab.util.ParallelExecutor;

/**
 * A MessageRounter routes messages to operators on other Workers.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class MessageRouter<V, F> extends Thread {

	/**
	 * The maximum number of messages that are sent together.
	 */
	public static final int MAXIMUM_BUFFER_SIZE = 5000;

	/**
	 * The associated operator.
	 */
	protected BSPOperator<?, ?> operator;

	/**
	 * The buffered outgoing messages.
	 */
	protected LinkedList<MessageBuffer> outgoingMessages = new LinkedList<MessageBuffer>();

	/**
	 * The AggregateManager for maintaining Summaries for Vertices.
	 */
	protected AggregateManager<V, F> aggregateManager = new AggregateManager<V, F>();

	/**
	 * A flag indicating whether or not this MessageRounter is terminated.
	 */
	protected boolean terminated = false;

	/**
	 * A flag indicating whether or not this MessageRounter is waiting for all of the outgoing messages to be sent out.
	 */
	private boolean waiting = false;

	/**
	 * A MessageBuffer maintains SummaryMessages for each Worker.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public class MessageBuffer extends HashMap<Integer, LinkedList<SummaryMessage<?, ?>>> {

		/**
		 * The Serial Version UID.
		 */
		private static final long serialVersionUID = -8492557357359173806L;

		/**
		 * A flag indicating whether or not this Buffer is full.
		 */
		protected boolean full = false;

		/**
		 * Adds the key and value to this MessageBuffer.
		 * 
		 * @param workerID
		 *            the ID of a Worker.
		 * @param message
		 *            a SummaryMessage.
		 */
		public void add(Integer workerID, SummaryMessage<?, ?> message) {
			LinkedList<SummaryMessage<?, ?>> buffer = get(workerID);
			if (buffer == null) {
				buffer = new LinkedList<SummaryMessage<?, ?>>();
				put(workerID, buffer);
			}
			buffer.add(message);
			full = (buffer.size() >= MAXIMUM_BUFFER_SIZE);
		}

		/**
		 * Determines whether or not this Buffer is full.
		 * 
		 * @return true if this Buffer is full; false otherwise.
		 */
		public boolean isFull() {
			return full;
		}

	}

	/**
	 * Constructs a MessageRouter.
	 * 
	 * @param operator
	 *            the associated Operator.
	 */
	public MessageRouter(BSPOperator<?, ?> operator) {
		this.operator = operator;
		start();
	}

	/**
	 * Puts the specified SummaryMessage in the send buffer.
	 * 
	 * @param message
	 *            the message to send.
	 */
	public synchronized void enqueue(SummaryMessage<?, ?> message) {
		MessageBuffer buffer;
		if (outgoingMessages.size() == 0 || outgoingMessages.getLast().isFull()) {
			buffer = new MessageBuffer();
			outgoingMessages.add(buffer);
		} else {
			buffer = outgoingMessages.getLast();
		}

		int workerID = message.intermediateVertexID() == null ? Worker.workerID(message.targetVertexID(),
				operator.workerIDs()) : Worker.workerID(message.intermediateVertexID(), operator.workerIDs());
		buffer.add(workerID, message.clone(message.graphIDs()));
	}

	/**
	 * Shuts down this MessageRouter.
	 */
	public void shutdown() {
		terminated = true;
	}

	/**
	 * Keeps sending buffered messages.
	 */
	public void run() {
		while (!terminated) {
			try {
				if (outgoingMessages.size() > 0) {
					MessageBuffer buffer;
					synchronized (this) {
						buffer = outgoingMessages.poll();
					}
					send(buffer);
				}
				if (outgoingMessages.size() == 0) {
					if (waiting)
						synchronized (this) {
							notify();
							waiting = false;
						}
					Thread.sleep(100);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sends the SummaryMessages contained in the specified SummaryMessage to appropriate Workers.
	 * 
	 * @param buffer
	 *            the MessageBuffer that contains the messages to send.
	 * @throws Exception
	 *             if an error occurs.
	 */
	protected void send(MessageBuffer buffer) throws Exception {
		ParallelExecutor executor = new ParallelExecutor();
		for (final Entry<Integer, LinkedList<SummaryMessage<?, ?>>> e : buffer.entrySet()) {
			executor.add(new ParallelExecutor.Task() {
				@Override
				public void run() throws Exception {
					operator.worker().worker(e.getKey()).handle(operator.name(), e.getValue());
				}
			});
		}
		executor.run();
	}

	/**
	 * Handles the received SummaryMessages.
	 * 
	 * @param messages
	 *            the SummaryMessages to handle.
	 */
	@SuppressWarnings("unchecked")
	public void handle(Collection<SummaryMessage<?, ?>> messages) {
		for (SummaryMessage<?, ?> m : messages) {
			if (m.intermediateVertexID() != null) { // if need to update the summary using the intermediate vertex
				Iterator<Vertex> i = operator.vertices(m.intermediateVertexID(), m.graphIDs());
				while (i.hasNext()) {
					Vertex v = i.next();
					try {
						((Summary<Vertex, ?>) m.summary()).update(v);
					} catch (Exception e) {
						e.printStackTrace();
					}
					m.setIntermediateVertexID(null);
					enqueue((SummaryMessage<?, ?>) m);
				}
			} else { // if the summary has arrived at the final destination
				operator.statistics().increaseSummaryMessages(1);
				aggregateManager.updateSummary(m.targetVertexID(), (Summary<V, F>) m.summary(), m.graphIDs());
			}
		}
	}

	/**
	 * Waits until there is no outgoing SummaryMessages.
	 */
	public synchronized void waitUntilNoOutgoingMessages() {
		try {
			waiting = true;
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Resets the AggregateManager.
	 * 
	 * @return the previous AggregateManager that contains Summaries recently received.
	 */
	public AggregateManager<?, ?> resetAggregateManager() {
		AggregateManager<?, ?> r = aggregateManager;
		aggregateManager = new AggregateManager<V, F>();
		return r;
	}

}

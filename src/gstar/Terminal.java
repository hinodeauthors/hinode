package gstar;

import gstar.Master.ExistingGraphException;
import gstar.client.Client;
import gstar.client.Client.NoGraphException;
import gstar.client.Client.InvalidAttributeNameException;
import gstar.client.Client.ParsingException;
import gstar.data.GID;
import gstar.data.GraphDirectory;
import gstar.data.GraphProperties;
import gstar.data.Catalog.AttributeRedefinitionException;
import gstar.query.OperatorID;
import gstar.query.QueryEngine.NoOperatorException;
import hwanglab.data.DataObject;
import hwanglab.system.real.LookupService;
import hwanglab.util.LineReader;
import hwanglab.util.StringTokenizer;

import java.io.PrintStream;
import java.util.Date;
import java.util.Iterator;

/**
 * A Terminal allows a user to interact with the G* system.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Terminal extends hwanglab.util.Terminal {

	/**
	 * The Client to use.
	 */
	protected Client client;
	
	/**
	 * The last wall-clock time.
	 */
	protected Long lastWallClockTime = null;

	/**
	 * Constructs a Terminal.
	 * 
	 * @param reader
	 *            the LineReader.
	 * @param out
	 *            the PrintStream.
	 * @param client
	 *            the Client to connect to.
	 * @param echo
	 *            a flag indicating whether or not to print each command.
	 * @param welcomeMessage
	 *            the welcome message.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Terminal(LineReader reader, PrintStream out, Client client, boolean echo, String welcomeMessage) throws Exception {
		super(reader, out, echo, welcomeMessage);
		this.client = client;
	}

	@Override
	public boolean process(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line, "@()[]=,", '\"', '#');
		String command = tokenizer.next();
		if (command != null) {
			if (command.equals("exit"))
				return true;
			else if (command.equals("pwd"))
				client.printWorkingDirectory(out);
			else if (command.equals("ls"))
				client.pringWorkingDirectoryContents(out);
			else if (command.equals("cd")) {
				String path = tokenizer.next();
				try {
					println("moved to " + client.changeDirectory(path).absolutePath());
				} catch (Exception e) {
					println("cd " + path + ": no such directory");
				}
			} else if (command.equals("mkdir")) {
				String path = tokenizer.next();
				String token = tokenizer.next();
				try {
					if (token == null) {
						GraphDirectory dir = client.createDirectory(path, null);
						println(dir.absolutePath() + " created on " + dir.workerIDs().length + " workers.");
					} else if (token.equals("@")) {
						GraphDirectory dir = client.createDirectory(path, tokenizer.next());
						println(dir.absolutePath() + " created on " + dir.workerIDs().length + " workers.");
					} else {
						println("invalid command");
					}
				} catch (Exception e) {
					println("mkdir " + path + ": invalid directory");
				}
			} else if (command.equals("rmdir")) {
				String path = tokenizer.next();
				try {
					client.removeDirectories(path);
				} catch (Exception e) {
					println("rmdir " + path + ": no such directory");
				}
			} else if (command.equals("int")) {
				try {
					client.addAttributes(int.class, out, tokenizer.remainder());
				} catch (AttributeRedefinitionException e) {
					println(e.getMessage());
				} catch (Exception e) {
					System.err.println(e);
				}
			} else if (command.equals("double")) {
				try {
					client.addAttributes(double.class, out, tokenizer.remainder());
				} catch (AttributeRedefinitionException e) {
					println(e.getMessage());
				} catch (Exception e) {
					System.err.println(e);
				}
			} else if (command.equals("string")) {
				try {
					client.addAttributes(String.class, out, tokenizer.remainder());
				} catch (AttributeRedefinitionException e) {
					println(e.getMessage());
				} catch (Exception e) {
					System.err.println(e);
				}
			} else if (command.equals("graph")) {
				String current = tokenizer.next();
				String previous = null;
				Double prevGID = null;
				try {
					double newGID = Double.parseDouble(current);
					previous = tokenizer.next();
					if (previous != null) {
						prevGID = Double.parseDouble(previous);
					}
					try {
						GraphProperties properties;
						if (prevGID != null)
							properties = client.createGraph(new GID(newGID), new GID(prevGID));
						else
							properties = client.graph(new GID(newGID));
						println("graph " + properties.graphID() + " is in " + properties.absolutePath() + ".");
					} catch (Exception e) {
						if (causedBy(e, ExistingGraphException.class))
							println("graph " + newGID + " already exists!");
						else
							System.err.println(e);
					}
				} catch (Exception e) {
					println("graph " + current + (previous == null ? "" : " " + previous) + ": invalid command");
				}
			} else if (command.equals("vertex")) {
				String vertexID = tokenizer.next();
				if (vertexID == null) {
					println("vertex ID is missing");
				} else {
					try {
						client.updateVertex(vertexID, tokenizer);
					} catch (InvalidAttributeNameException e) {
						System.err.println(e);
					} catch (ParsingException e) {
						System.err.println(e);
					} catch (Exception e) {
						if (causedBy(e, NoGraphException.class))
							println("specify the graph using the graph command!");
						else
							System.err.println(e);
					}
				}
			} else if (command.equals("edge")) {
				String srcID = tokenizer.next();
				String desID = tokenizer.next();
				if (srcID == null) {
					println("source vertex ID is missing");
				} else if (desID == null) {
					println("destination vertex ID is missing");
				} else {
					try {
						client.updateEdge(srcID, desID, tokenizer);
					} catch (InvalidAttributeNameException e) {
						System.err.println(e);
					} catch (ParsingException e) {
						System.err.println(e);
					} catch (Exception e) {
						if (causedBy(e, NoGraphException.class))
							println("specify the graph using the graph command!");
						else
							System.err.println(e);
					}
				}
			} else if (command.equals("operator")) {
				try {
					client.createOperators(tokenizer);
				} catch (Exception e) {
					println("cannot create the specified operator!");
					getCause(e).printStackTrace(System.err);
				}
			} else if (command.equals("run")) {
				String operatorName = tokenizer.next();
				if (operatorName == null) {
					println("operator name is missing!");
					return false;
				}
				if (!"@".equals(tokenizer.next())) {
					println("@ is needed!");
					return false;
				}
				int workerID;
				try {
					workerID = Integer.parseInt(tokenizer.next());
				} catch (Exception e) {
					println("invalid worker ID");
					return false;
				}
				try {
					Iterator<DataObject> i = client.iterator(new OperatorID(operatorName, workerID));
					while (i.hasNext()) {
						println(i.next());
					}
					client.removeCompletedOperators();
				} catch (Exception e) {
					if (causedBy(e, NoOperatorException.class))
						println("operator " + operatorName + " cannot be found!");
					else
						System.err.println(e);
				}
			} else if (command.equals("shutdown")) {
				try {
					client.shutdown();
					return true;
				} catch (Exception e) {
					System.err.println(e);
				}
			} else if (command.equals("time")) {
				long currentTime = System.currentTimeMillis();
				if (lastWallClockTime != null)
					println("elapsed time: " + (currentTime - lastWallClockTime) + " milliseconds");
				println("current time: " + new Date(currentTime));
				lastWallClockTime = currentTime;
			} else
				println(command + ": command not found");
		}
		return false;
	}

	/**
	 * Returns true if and only if the specified Throwable is caused by an exception of the specified type.
	 * 
	 * @param t
	 *            the Throwable to examine.
	 * @param exceptionType
	 *            an exception type.
	 * @return true if the specified Throwable is caused by an exception of the specified type; false otherwise.
	 */
	protected boolean causedBy(Throwable t, Class<?> exceptionType) {
		while (t != null) {
			if (t.getClass() == exceptionType)
				return true;
			t = t.getCause();
		}
		return false;
	}

	/**
	 * Returns the ultimate cause of the specified Throwable.
	 * 
	 * @param t
	 *            the Throwable to examine.
	 * @return the ultimate cause of the specified Throwable.
	 */
	protected Throwable getCause(Throwable t) {
		while (t.getCause() != null) {
			t = t.getCause();
		}
		return t;
	}

	/**
	 * Starts a Terminal.
	 * 
	 * @param args
	 *            the arguments.
	 * @throws NumberFormatException
	 *             if there is a problem in the number format.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public static void main(String[] args) throws NumberFormatException, Exception {
		LookupService lookupService = new hwanglab.system.real.LookupService(Integer.parseInt(args[0]));
		MasterInterface master = lookupService.lookup(args[1], MasterInterface.class);
		new Terminal(new LineReader("G*$ "), System.out, new Client(master), false, "Welcome to G* version 1.0.0.").run();
		lookupService.shutdown();
	}

}

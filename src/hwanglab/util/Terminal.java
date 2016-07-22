package hwanglab.util;

import java.io.PrintStream;

/**
 * A Terminal interacts with human users.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public abstract class Terminal {

	/**
	 * The LineReader.
	 */
	private LineReader reader;

	/**
	 * The PrintStream.
	 */
	protected PrintStream out;

	/**
	 * A flag indicating whether or not to print each command.
	 */
	protected boolean echo;

	/**
	 * Constructs a Terminal.
	 * 
	 * @param reader
	 *            the LineReader.
	 * @param out
	 *            the PrintStream.
	 * @param echo
	 *            a flag indicating whether or not to print each command.
	 * @param welcomeMessage
	 *            the welcome message.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Terminal(LineReader reader, PrintStream out, boolean echo, String welcomeMessage) throws Exception {
		this.reader = reader;
		this.out = out;
		this.echo = echo;
		out.println(welcomeMessage);
	}

	/**
	 * Runs this Terminal.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void run() throws Exception {
		String line;
		while ((line = reader.readLine()) != null) {
			if (echo)
				println(line);
			if (process(line.trim()))
				return;
		}
	}

	/**
	 * Parses and executes the specified command.
	 * 
	 * @param command
	 *            the command to parse and execute.
	 * @return true if the command is for exiting the current session; false otherwise.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public abstract boolean process(String command) throws Exception;

	/**
	 * Prints the specified message.
	 * 
	 * @param o
	 *            the message to print.
	 */
	protected void println(Object o) {
		out.println(o);
	}

}

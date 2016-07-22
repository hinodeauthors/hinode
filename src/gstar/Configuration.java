package gstar;

import java.io.File;

/**
 * A Configuration represents a configuration for the G* system.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Configuration extends hwanglab.system.Configuration implements java.io.Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 3507300316261404999L;

	/**
	 * The system directory.
	 */
	protected String systemDirectory = "sys" + File.separator;

	/**
	 * The the size of the buffer for caching the graph data.
	 */
	protected long bufferSize = 100*1024*1024;

	@Override
	public String toString() {
		String s = super.toString();
		s += "system directory: " + systemDirectory() + "\r\n";
		s += "buffer size: " + bufferSize() + "\r\n";
		return s;
	}

	@Override
	protected void update(String argument, hwanglab.util.StringArrayIterator i) throws ParsingException {
		if (argument.equals("-system-dir")) {
			this.systemDirectory = i.next();
		} else if (argument.equals("-bufferSize")) {
			bufferSize = Long.parseLong(i.next());
		} else
			super.update(argument, i);
	}

	/**
	 * Returns the name of the directory for storing result files.
	 * 
	 * @return the name of the directory for storing result files.
	 */
	public String resultDirectory() {
		java.io.File f = new java.io.File("results");
		if (!f.exists())
			f.mkdir();
		return f.toString() + java.io.File.separator;
	}

	/**
	 * Returns the prefix that the names of the output files share.
	 * 
	 * @return the prefix that the names of the output files share.
	 */
	public String fileNamePrefix() {
		return resultDirectory();
	}

	/**
	 * Returns the system directory.
	 * 
	 * @return the system directory
	 */
	public String systemDirectory() {
		return systemDirectory;
	}

	/**
	 * Returns the size of the buffer for caching the graph data.
	 * @return the size of the buffer for caching the graph data.
	 */
	public long bufferSize() {
		return bufferSize;
	}

}

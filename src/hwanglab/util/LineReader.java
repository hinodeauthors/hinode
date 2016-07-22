package hwanglab.util;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * A LineReader can read text lines from either the standard input or a file.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class LineReader {

	/**
	 * The file reader.
	 */
	BufferedReader reader = null;

	/**
	 * The prompt for this LineReader.
	 */
	String prompt;

	/**
	 * The system console.
	 */
	Console c = null;

	/**
	 * An iterator over the files to read.
	 */
	protected Iterator<File> files;

	/**
	 * Constructs a LineReader that reads text lines from the specified file.
	 * 
	 * @param files
	 *            an iterator over the files to read.
	 * @throws FileNotFoundException
	 *             if the specified does not exist.
	 */
	public LineReader(Iterator<File> files) throws FileNotFoundException {
		this.files = files;
		reader = nextReader();
	}

	/**
	 * Constructs a new LineReader that reads text lines from the standard input.
	 * 
	 * @param prompt
	 *            the prompt for the LineReader.
	 */
	public LineReader(String prompt) {
		this.c = System.console();
		this.prompt = prompt;
		reader = new BufferedReader(new java.io.InputStreamReader(System.in));
	}

	/**
	 * Reads a line of text.
	 * 
	 * @return the read text.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public String readLine() throws IOException {
		if (c != null)
			return c.readLine(prompt);
		else {
			String line = null;
			while (reader != null && (line = reader.readLine()) == null && files != null && files.hasNext()) {
				reader = nextReader();
			}
			return line;
		}
	}

	/**
	 * Returns a new BufferedReader constructed from the next file.
	 * 
	 * @return a new BufferedReader constructed from the next file.
	 */
	protected BufferedReader nextReader() {
		while (files.hasNext()) {
			try {
				return new BufferedReader(new FileReader(files.next()));
			} catch (Exception e) {
			}
		}
		return null;
	}

}

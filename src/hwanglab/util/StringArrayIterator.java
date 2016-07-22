package hwanglab.util;

import java.util.Iterator;

/**
 * A StringArrayIterator iterates over the Strings in a String array.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 */
public class StringArrayIterator implements Iterator<String> {

	/**
	 * The String array.
	 */
	protected String[] strings;

	/**
	 * The current String index.
	 */
	protected int index = 0;

	/**
	 * The most recently accessed String.
	 */
	protected String mostRecentString = null;

	/**
	 * Constructs a StringArrayIterator.
	 * 
	 * @param strings
	 *            the Strings to iterate over.
	 */
	public StringArrayIterator(String[] strings) {
		this.strings = strings;
	}

	@Override
	public boolean hasNext() {
		return index < strings.length;
	}

	@Override
	public String next() {
		mostRecentString = strings[index++];
		return mostRecentString;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return Arrays.toString(strings, 0, index - 1) + ":accessed, "
				+ Arrays.toString(strings, index, strings.length - 1) + ":unaccessed";
	}

	/**
	 * Returns the most recently accessed String.
	 * 
	 * @return the most recently accessed String.
	 */
	public String mostRecentString() {
		return mostRecentString;
	}

}

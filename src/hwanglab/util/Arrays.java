package hwanglab.util;

/**
 * The Arrays class provides methods for dealing with arrays.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class Arrays {

	/**
	 * Returns a String representation of the specified array.
	 * 
	 * @param array
	 *            the array.
	 * @param start
	 *            the start index.
	 * @param end
	 *            the end index.
	 * @param left
	 *            the left delimiter.
	 * @param right
	 *            the right delimiter.
	 * @param separator
	 *            the separator between two elements of the array.
	 * @return a String representation of the specified array.
	 */
	public static String toString(Object[] array, int start, int end, String left, String right, String separator) {
		String s = left;
		for (int i = start; i <= end; i++) {
			if (i == start)
				s += array[i];
			else
				s += separator + array[i];
		}
		return s + right;
	}

	/**
	 * Returns a String representation of the specified array.
	 * 
	 * @param array
	 *            the array.
	 * @param start
	 *            the start index.
	 * @param end
	 *            the end index.
	 * @return a String representation of the specified array.
	 */
	public static String toString(Object[] array, int start, int end) {
		return toString(array, start, end, "[", "]", ", ");
	}

	/**
	 * Returns a String representation of the specified array.
	 * 
	 * @param array
	 *            the array.
	 * @param left
	 *            the left delimiter.
	 * @param right
	 *            the right delimiter.
	 * @param separator
	 *            the separator between two elements of the array.
	 * @return a String representation of the specified array.
	 */
	public static String toString(Object[] array, String left, String right, String separator) {
		return toString(array, 0, array == null ? -1 : (array.length - 1), left, right, separator);
	}

}

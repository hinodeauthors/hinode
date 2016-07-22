package hwanglab.data.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A SlottedPage stores a number of objects in a byte array.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class SlottedPage {

	/**
	 * The ID of this SlottedPage.
	 */
	private int pageID;

	/**
	 * A byte array for storing objects.
	 */
	protected byte[] page;

	/**
	 * The number of bytes for representing an int value.
	 */
	protected static int INT_SIZE = 4;

	/**
	 * Constructs a SlottedPage.
	 * 
	 * @param pageID
	 *            the ID of the SlottedPage.
	 * @param size
	 *            the size of the SlottedPage.
	 */
	public SlottedPage(int pageID, int size) {
		page = new byte[size - 2 * INT_SIZE];
		setEntryCount(0);
		this.pageID = pageID;
	}

	/**
	 * Constructs a SlottedPage.
	 * 
	 * @param file
	 *            a file from which the SlottedPage is constructed.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public SlottedPage(RandomAccessFile file) throws IOException {
		int size = file.readInt();
		page = new byte[size];
		file.read(page);
		pageID = file.readInt();
	}

	/**
	 * Saves this SlottedPage on the specified file.
	 * 
	 * @param file
	 *            a file on which this SlottedPage is saved.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public void save(RandomAccessFile file) throws IOException {
		file.writeInt(page.length);
		file.write(page);
		file.writeInt(pageID);
	}

	/**
	 * Returns the size of this SlottedPage.
	 * 
	 * @return the size of this SlottedPage.
	 */
	public long size() {
		return this.page.length + 2 * INT_SIZE;
	}

	/**
	 * Returns the size of the SlottedPage for storing the specified byte array.
	 * 
	 * @param b
	 *            a byte array.
	 * @param defaultPageSize
	 *            default size for SlottedPages.
	 * @return the size of a SlottedPage for storing the specified byte array.
	 */
	public static int size(byte[] b, int defaultPageSize) {
		double relativeSize = Math.max(1,  (b.length + 5.0 * INT_SIZE) / defaultPageSize);
		relativeSize = Math.pow(2, Math.ceil(Math.log(relativeSize)/Math.log(2)));
		return (int) (defaultPageSize * Math.round(relativeSize));
	}

	/**
	 * Returns the ID of this SlottedPage.
	 * 
	 * @return the ID of this SlottedPage.
	 */
	public int pageID() {
		return pageID;
	}

	@Override
	public String toString() {
		return "" + pageID;
	}

	/**
	 * Returns a stream for reading the object at the specified index.
	 * 
	 * @param index
	 *            an index within this SlottedPage.
	 * @return a stream for reading the object at the specified index; null if no such object exists.
	 */
	public ByteArrayInputStream get(int index) {
		try {
			if (index < getEntryCount()) {
				return new ByteArrayInputStream(page, getOffset(index), getObjectSize(index));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Stores the specified byte array within this SlottedPage.
	 * 
	 * @param b
	 *            a byte array.
	 * @return the index at which the byte array is stored within this SlottedPage.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws OverFlowException
	 *             if this SlottedPage cannot accommodate the specified byte array.
	 */
	public int add(byte[] b) throws IOException, OverFlowException {
		try {
			int index = getEntryCount();
			put(index, b);
			return index;
		} catch (IndexOutofBoundsException e) {
			e.printStackTrace();
			System.err.println("cannot happen!");
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Stores the specified byte array at the specified index within this SlottedPage.
	 * 
	 * @param index
	 *            the index for the object.
	 * @param b
	 *            a byte array.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws OverFlowException
	 *             if this SlottedPage cannot accommodate the specified byte array.
	 * @throws IndexOutofBoundsException
	 *             an illegal index is used.
	 */
	public void put(int index, byte[] b) throws IOException, OverFlowException, IndexOutofBoundsException {
		int entryCount = getEntryCount();
		if (index < 0 || index > entryCount)
			throw new IndexOutofBoundsException();
		if (index == entryCount) { // new insertion
			int headerSize = (2 + entryCount) * INT_SIZE;
			if (headerSize + getDataSize() + b.length > page.length) {
				throw new OverFlowException();
			}
			setEntryCount(entryCount + 1);
			int offset = (entryCount == 0) ? page.length - b.length : getOffset(entryCount - 1) - b.length;
			setOffset(entryCount, offset);
			System.arraycopy(b, 0, page, offset, b.length); // write the byte array
		} else { // re-insertion
			int headerSize = (1 + entryCount) * INT_SIZE;
			int sizeDiff = b.length - getObjectSize(index);
			if (headerSize + getDataSize() + sizeDiff > page.length)
				throw new OverFlowException();
			int offset = getOffset(index);
			if (sizeDiff != 0)
				moveData(index, sizeDiff);
			System.arraycopy(b, 0, page, offset - sizeDiff, b.length); // write the byte array
		}
	}

	/**
	 * Removes the object at the specified index.
	 * 
	 * @param index
	 *            an index within this SlottedPage.
	 * @throws IndexOutofBoundsException
	 *             an illegal index is used.
	 */
	public void remove(int index) throws IndexOutofBoundsException {
		int entryCount = getEntryCount();
		if (index < 0 || index >= entryCount)
			throw new IndexOutofBoundsException();
		moveData(index, -getObjectSize(index));
	}

	/**
	 * A BufferOverFlowException is thrown if a SlottedPage cannot accommodate an additional object.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public class OverFlowException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = -3007432568764672956L;

	}

	/**
	 * An IndexOutofBoundsException is thrown if an illegal index is used.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	public class IndexOutofBoundsException extends Exception {

		/**
		 * Automatically generated serial version UID.
		 */
		private static final long serialVersionUID = 7167791498344223410L;

	}

	/**
	 * Returns the number of objects stored in this SlottedPage.
	 * 
	 * @return the number of objects stored in this SlottedPage.
	 */
	protected int getEntryCount() {
		return readInt(0);
	}

	/**
	 * Sets the number of objects stored in this SlottedPage.
	 * 
	 * @param count
	 *            the number of objects stored in this SlottedPage.
	 */
	protected void setEntryCount(int count) {
		writeInt(0, count);
	}

	/**
	 * Returns the start location of the specified object within this SlottedPage.
	 * 
	 * @param index
	 *            an index that specifies an object.
	 * @return the start location of the specified object within this SlottedPage.
	 */
	protected int getOffset(int index) {
		return readInt((index + 1) * INT_SIZE);
	}

	/**
	 * Sets the start location of the specified object within this SlottedPage.
	 * 
	 * @param index
	 *            an index that specifies an object.
	 * @param value
	 *            a location within this SlottedPage.
	 */
	protected void setOffset(int index, int value) {
		writeInt((index + 1) * INT_SIZE, value);
	}

	/**
	 * Returns the size of the object at the specified index.
	 * 
	 * @param index
	 *            an index within this SlottedPage.
	 * @return the size of the object at the specified index.
	 */
	protected int getObjectSize(int index) {
		if (index == 0)
			return page.length - getOffset(index);
		else
			return getOffset(index - 1) - getOffset(index);
	}

	/**
	 * Writes an integer value at the specified location in the page.
	 * 
	 * @param offset
	 *            a location in the page.
	 * @param value
	 *            the value to write.
	 */
	protected void writeInt(int offset, int value) {
		page[offset] = (byte) (value >>> 24);
		page[offset + 1] = (byte) (value >>> 16);
		page[offset + 2] = (byte) (value >>> 8);
		page[offset + 3] = (byte) value;
	}

	/**
	 * Reads an integer from the page at the specified location.
	 * 
	 * @param offset
	 *            a location in the page.
	 * @return an integer read from the page at the specified offset.
	 */
	protected int readInt(int offset) {
		return ((page[offset]) << 24) + ((page[offset + 1] & 0xFF) << 16) + ((page[offset + 2] & 0xFF) << 8)
				+ (page[offset + 3] & 0xFF);
	}

	/**
	 * Returns the size of the data stored in this SlottedPage.
	 * 
	 * @return the size of the data stored in this SlottedPage.
	 */
	protected int getDataSize() {
		int entryCount = getEntryCount();
		if (entryCount > 0) {
			return page.length - getOffset(entryCount - 1);
		} else
			return 0;
	}

	/**
	 * Moves all of the objects at or after the specified index.
	 * 
	 * @param index
	 *            an index with this SlottedPage.
	 * @param sizeDiff
	 *            the change in the size of the object at the specified index.
	 */
	protected void moveData(int index, int sizeDiff) {
		int entryCount = getEntryCount();
		int dataStart = getOffset(entryCount - 1);
		int moveSize = getOffset(index) - dataStart;
		if (moveSize > 0) {
			byte[] temp = new byte[moveSize];
			System.arraycopy(page, dataStart, temp, 0, moveSize);
			System.arraycopy(temp, 0, page, dataStart - sizeDiff, moveSize);
		}
		for (int i = index; i < entryCount; i++)
			setOffset(i, getOffset(i) - sizeDiff);
	}

}

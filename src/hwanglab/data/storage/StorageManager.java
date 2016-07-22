package hwanglab.data.storage;

import hwanglab.data.storage.SlottedPage.IndexOutofBoundsException;
import hwanglab.data.storage.SlottedPage.OverFlowException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

/**
 * A StorageManager stores objects on disk.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 */
public class StorageManager {

	/**
	 * The RandomAccessFile used by this StorageManager.
	 */
	protected RandomAccessFile file;

	/**
	 * The default size for SlottedPages.
	 */
	protected int defaultPageSize = 64 * 1024;

	/**
	 * The ID of the next SlottedPage to create.
	 */
	protected int nextPageID;

	/**
	 * The SlottedPages that are buffered in the memory.
	 */
	protected LinkedHashMap<Integer, SlottedPage> bufferedPages = new LinkedHashMap<Integer, SlottedPage>();

	/**
	 * The SlottedPages that are recently updated and thus need to be saved on disk.
	 */
	protected LinkedHashMap<Integer, SlottedPage> dirtyPages = new LinkedHashMap<Integer, SlottedPage>();

	/**
	 * The thread that saves dirty pages on disk.
	 */
	PurgeThread purgeThread;

	/**
	 * The number of disk seeks so far.
	 */
	protected long diskSeeks;

	/**
	 * The ID of the last SlottedPage.
	 */
	protected int lastPageID;

	/**
	 * The size of the buffer for caching the graph data.
	 */
	protected long bufferSize;

	/**
	 * The number of bytes that are buffered.
	 */
	protected long bytesBuffered;

	/**
	 * A PurgeThread writes dirty pages on the disk.
	 * 
	 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
	 */
	protected class PurgeThread extends Thread {

		Boolean shutdownRequested = false;

		public void run() {
			while (true) {
				try {
					if (dirtyPages.size() == 0) { // if no dirty page, sleep
						if (shutdownRequested)
							return;
						else {
							Thread.sleep(100);
						}
					}
					purgeDirtyPage(null); // purge the first dirty page
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Shuts down this PurgeThread.
		 */
		public void shutdown() {
			shutdownRequested = true;
		}

	}

	/**
	 * Constructs a StorageManager.
	 * 
	 * @param fileName
	 *            the name of the RandomAccessFile.
	 * @param bufferSize
	 *            the size of the buffer for caching the graph data.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public StorageManager(String fileName, long bufferSize) throws IOException {
		file = new RandomAccessFile(fileName, "rw");
		nextPageID = (int) file.length() / defaultPageSize;
		if (nextPageID > 0) {
			file.seek(file.length() - 4);
			lastPageID = file.readInt();
		} else
			lastPageID = -1;
		diskSeeks = 0;
		purgeThread = new PurgeThread();
		purgeThread.setPriority(Thread.MIN_PRIORITY);
		purgeThread.start();
		this.bufferSize = bufferSize;
		this.bytesBuffered = 0;
	}

	/**
	 * Removes all the data.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public synchronized void clearData() throws IOException {
		nextPageID = 0;
		file.setLength(0);
		bufferedPages = new LinkedHashMap<Integer, SlottedPage>();
		dirtyPages = new LinkedHashMap<Integer, SlottedPage>();
	}

	/**
	 * Stores an object in this StorageManager.
	 * 
	 * @param location
	 *            the target location of the object.
	 * @param object
	 *            the object to add.
	 * @return the ObjectLocation at which the object is saved.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public ObjectLocation put(ObjectLocation location, Object object) throws IOException {
		byte[] b = toByteArray(object);
		int pageID = location.pageID;
		SlottedPage page = null;
		try {
			page = findPage(pageID);
			page.put(location.index, b);
			registerDirtyPage(page);
			return new ObjectLocation(pageID, location.index);
		} catch (Exception e) {
			if (page != null)
				try {
					page.remove(location.index);
					registerDirtyPage(page);
				} catch (IndexOutofBoundsException e1) {
				}
		}
		try { // if the target page cannot accommodate the specified object, try the last one
			page = findPage(lastPageID);
			int index = page.add(b);
			registerDirtyPage(page);
			return new ObjectLocation(page.pageID(), index);
		} catch (Exception e) {
		}
		return createPage(b);
	}

	/**
	 * Constructs a new SlottedPage containing the specified the byte array.
	 * 
	 * @param b
	 *            a byte array.
	 * @return a new SlottedPage containing the specified the byte array.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	protected ObjectLocation createPage(byte[] b) throws IOException {
		int size = SlottedPage.size(b, defaultPageSize);
		SlottedPage page = new SlottedPage(nextPageID, (int) size);
		lastPageID = nextPageID;
		nextPageID += size / defaultPageSize;
		cache(page);
		int index;
		try {
			index = page.add(b);
			registerDirtyPage(page);
			return new ObjectLocation(page.pageID(), index);
		} catch (OverFlowException e) {
			e.printStackTrace();
			System.err.println("cannot happen!");
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Stores an object in this StorageManager.
	 * 
	 * @param object
	 *            an object.
	 * @return the ObjectLocation at which the object is stored.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public ObjectLocation add(Object object) throws IOException {
		if (this.nextPageID == 0)
			return put(new ObjectLocation(0, 0), object);
		else {
			SlottedPage lastPage = findPage(lastPageID);
			return put(new ObjectLocation(lastPage.pageID(), lastPage.getEntryCount()), object);
		}
	}

	/**
	 * Returns the object stored at the specified LogicalLocation.
	 * 
	 * @param location
	 *            a ObjectLocation.
	 * @return the object stored at the specified LogicalLocation.
	 * @throws IOException
	 *             if an I/O error occurs.
	 * @throws ClassNotFoundException
	 *             if a relevant class cannot be found.
	 */
	public Object get(ObjectLocation location) throws IOException, ClassNotFoundException {
		if (location == null)
			return null;
		if (location.pageID < nextPageID) {
			SlottedPage page = findPage(location.pageID);
			if (page != null) {
				ByteArrayInputStream in = page.get(location.index);
				if (in != null)
					return new ObjectInputStream(in).readObject();
			}
		}
		return null;
	}

	/**
	 * Returns the size of the disk-resident data.
	 * 
	 * @return the size of the disk-resident data.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public long dataSize() throws IOException {
		return file.length();
	}

	/**
	 * Returns the number of disk seeks that this StorageManager has performed.
	 * 
	 * @return the number of disk seeks that this StorageManager has performed.
	 */
	public long diskSeeks() {
		return diskSeeks;
	}

	/**
	 * Returns the actual ratio of the memory-resident data to the disk-resident data.
	 * 
	 * @return the actual ratio of the memory-resident data to the disk-resident data.
	 */
	public double actualCachingRatio() {
		return nextPageID > 0 ? 1.0 * bytesBuffered / defaultPageSize / nextPageID : 0;
	}

	/**
	 * Shuts down this StorageManager.
	 */
	public synchronized void shutdown() {
		purgeThread.shutdown(); // shut down the purge thread.
		while (purgeThread.isAlive()) { // wait until all the dirty pages are written to disk.
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Checkpoints this StorageManager.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public synchronized void checkpoint() throws IOException {
		synchronized (dirtyPages) {
			for (Map.Entry<Integer, SlottedPage> entry : dirtyPages.entrySet()) {
				save(entry.getValue());
			}
			dirtyPages.clear();
		}
	}

	/**
	 * Registers the specified SlottedPage as a dirty page.
	 * 
	 * @param page
	 *            a SlottedPage to register as a dirty page.
	 */
	protected void registerDirtyPage(SlottedPage page) {
		synchronized (dirtyPages) {
			dirtyPages.put(page.pageID(), page);
		}
	}

	/**
	 * Registers the specified SlottedPage into the memory buffer.
	 * 
	 * @param page
	 *            the SlottedPage to register.
	 */
	protected synchronized void cache(SlottedPage page) {
		SlottedPage removed = bufferedPages.remove(page.pageID()); // remove the page from the list
		if (removed != null)
			bytesBuffered -= removed.size();
		bufferedPages.put(page.pageID(), page); // put the new page at the end of the list
		bytesBuffered += page.size();
		while (bufferedPages.size() > bufferSize) { // if no more space in the buffer
			try {
				Map.Entry<Integer, SlottedPage> leastRecentlyUsed = bufferedPages.entrySet().iterator().next();
				Integer id = leastRecentlyUsed.getKey();
				if (dirtyPages.containsKey(id)) // if the page to evict is dirty, purge it
					purgeDirtyPage(id);
				bufferedPages.remove(id); // remove the first page in the buffer
				bytesBuffered -= leastRecentlyUsed.getValue().size();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the SlottedPage associated with the specified page ID.
	 * 
	 * @param pageID
	 *            the ID of the SlottedPage.
	 * @return the SlottedPage associated with the specified page ID.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	protected SlottedPage findPage(int pageID) throws IOException {
		if (pageID < nextPageID) {
			SlottedPage page = bufferedPages.get(pageID);
			if (page == null) { // if the page is not in the buffer
				synchronized (file) {
					file.seek(((long) pageID) * defaultPageSize); // seek to the right location in the file
					diskSeeks++; // increment the disk seek counter
					page = new SlottedPage(file);
				}
			}
			cache(page); // put it in the buffer
			return page;
		}
		return null; // if there has been no such page
	}

	/**
	 * Purges a SlottedPage.
	 * 
	 * @param pageID
	 *            the ID of the SlottedPage to save on the disk.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	protected void purgeDirtyPage(Integer pageID) throws IOException {
		if (dirtyPages.size() > 0) {
			synchronized (dirtyPages) {
				if (dirtyPages.size() > 0) { // needed since the situation might have been changed
					if (pageID == null) { // if so, purge the first dirty page
						Map.Entry<Integer, SlottedPage> entry = dirtyPages.entrySet().iterator().next();
						pageID = entry.getKey();
					}
					SlottedPage page = dirtyPages.get(pageID);
					if (page != null) {// if we get the wanted page, save it
						save(page);
						dirtyPages.remove(pageID); // deregister the save page
					}
				}
			}
		}
	}

	/**
	 * Saves the specified SlottedPage on disk.
	 * 
	 * @param page
	 *            a SlottedPage.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public void save(SlottedPage page) throws IOException {
		synchronized (file) {
			file.seek(((long) page.pageID()) * defaultPageSize); // seek to the right location
			diskSeeks++; // increment the disk seek counter
			page.save(file);
		}
	}

	/**
	 * Returns a byte array representing the specified object.
	 * 
	 * @param o
	 *            an object.
	 * @return a byte array representing the specified object.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	protected byte[] toByteArray(Object o) throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(b);
		out.writeObject(o);
		out.flush();
		b.flush();
		return b.toByteArray();
	}

}

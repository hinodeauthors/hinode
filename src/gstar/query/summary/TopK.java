package gstar.query.summary;

import hwanglab.data.DataObject;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * A TopK maintains top-k DataObjects.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 */
public class TopK implements Summary<DataObject, Collection<DataObject>> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -1471290357096887082L;

	/**
	 * The DataObjects.
	 */
	TreeSet<DataObject> objects;

	/**
	 * The number of DataObjects to keep.
	 */
	int count;

	/**
	 * Constructs a TopK instance.
	 * 
	 * @param count
	 *            the number of DataObjects to keep.
	 * @param c
	 *            a Comparator.
	 */
	public TopK(int count, Comparator<DataObject> c) {
		objects = new TreeSet<DataObject>(c);
		this.count = count;
	}

	@Override
	public boolean update(DataObject v) {
		boolean r = objects.add(v);
		while (objects.size() > count)
			objects.remove(objects.last());
		return r;
	}

	@Override
	public boolean update(Summary<DataObject, Collection<DataObject>> summary) {
		boolean r = objects.addAll(((TopK) summary).objects);
		while (objects.size() > count)
			objects.remove(objects.last());
		return r;
	}

	@Override
	public Collection<DataObject> value() {
		return objects;
	}

	@Override
	public Summary<DataObject, Collection<DataObject>> clone() {
		return new TopK(this);
	}

	@SuppressWarnings("unchecked")
	protected TopK(TopK topK) {
		objects = (TreeSet<DataObject>) topK.objects.clone();
		count = topK.count;
	}

}

package gstar.query.summary;

import hwanglab.util.MultiValuedHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * An Aggregate maintains a summary for each group.
 * 
 * @author Jeong-Hyon Hwang (jhh@cs.albany.edu)
 * 
 * @param <G>
 *            The type of the grouping values.
 * @param <I>
 *            the type of the values for updating summaries.
 * @param <O>
 *            the type of the values computed from the summaries.
 */
public class Aggregate<G, I, O> {

	/**
	 * A map between groups and summaries.
	 */
	Map<G, ArrayList<Summary<I, O>>> group2summaries = new HashMap<G, ArrayList<Summary<I, O>>>();

	/**
	 * The initial summary.
	 */
	ArrayList<Summary<I, O>> summaries;

	/**
	 * Constructs an Aggregate.
	 */
	public Aggregate() {
	}

	/**
	 * Constructs an Aggregate.
	 * 
	 * @param summaries
	 *            the summaries for updating this Aggregate.
	 */
	public Aggregate(ArrayList<Summary<I, O>> summaries) {
		this.summaries = summaries;
	}

	/**
	 * Removes the first entry from this Aggregate.
	 * 
	 * @return the removed entry; null if no such entry existed.
	 */
	public Entry<G, ArrayList<Summary<I, O>>> removeFirst() {
		Iterator<Entry<G, ArrayList<Summary<I, O>>>> i = group2summaries.entrySet().iterator();
		if (!i.hasNext())
			return null;
		Entry<G, ArrayList<Summary<I, O>>> next = i.next();
		if (next != null)
			group2summaries.remove(next.getKey());
		return next;
	}

	/**
	 * Determines whether or not this Aggregate is empty.
	 * 
	 * @return true if this Aggregate is empty; false otherwise.
	 */
	public boolean isEmpty() {
		return group2summaries.size() == 0;
	}

	/**
	 * Returns the summary associated with the specified group.
	 * 
	 * @param g
	 *            the group.
	 * @return the summary associated with the specified group.
	 */
	public ArrayList<Summary<I, O>> getSummary(G g) {
		return group2summaries.get(g);
	}

	/**
	 * Removes the entry with the specified group.
	 * 
	 * @param g
	 *            the group to remove.
	 */
	public void remove(G g) {
		group2summaries.remove(g);
	}

	/**
	 * Updates this Aggregate based on the specified values and groups.
	 * 
	 * @param values
	 *            input values.
	 * @param groups
	 *            a set of groups.
	 */
	public void update(I[] values, Set<G> groups) {
		HashSet<G> newGroups = new HashSet<G>();
		MultiValuedHashMap<ArrayList<Summary<I, O>>, G> existingSummaries = new MultiValuedHashMap<ArrayList<Summary<I, O>>, G>();
		for (G g : groups) {
			ArrayList<Summary<I, O>> s = group2summaries.get(g);
			if (s == null)
				newGroups.add(g);
			else
				existingSummaries.add(s, g);
		}
		if (newGroups.size() > 0) {
			ArrayList<Summary<I, O>> s = newSummaries(this.summaries, values);
			for (G g : newGroups)
				group2summaries.put(g, s);
		}
		for (Map.Entry<ArrayList<Summary<I, O>>, Set<G>> e : existingSummaries.entrySet()) {
			ArrayList<Summary<I, O>> s = newSummaries(e.getKey(), values);
			for (G g : e.getValue())
				group2summaries.put(g, s);
		}
	}

	/**
	 * Updates this Aggregate based on the specified summaries.
	 * 
	 * @param summaries
	 *            summaries for updating this Aggregate.
	 * @param groups
	 *            a set of groups.
	 */
	public void update(Summary<I, O>[] summaries, Set<G> groups) {
		HashSet<G> newGroups = new HashSet<G>();
		MultiValuedHashMap<ArrayList<Summary<I, O>>, G> existingSummaries = new MultiValuedHashMap<ArrayList<Summary<I, O>>, G>();
		for (G g : groups) {
			ArrayList<Summary<I, O>> s = group2summaries.get(g);
			if (s == null)
				newGroups.add(g);
			else
				existingSummaries.add(s, g);
		}
		if (newGroups.size() > 0) {
			ArrayList<Summary<I, O>> s = new ArrayList<Summary<I, O>>(summaries.length);
			for (int i = 0; i < summaries.length; i++)
				s.add(i, summaries[i].clone());
			for (G g : newGroups)
				group2summaries.put(g, s);
		}
		for (Map.Entry<ArrayList<Summary<I, O>>, Set<G>> e : existingSummaries.entrySet()) {
			ArrayList<Summary<I, O>> s = new ArrayList<Summary<I, O>>(summaries.length);
			for (int i = 0; i < summaries.length; i++) {
				s.add(i, e.getKey().get(i).clone());
				s.get(i).update(summaries[i]);
			}
			for (G g : e.getValue())
				group2summaries.put(g, s);
		}
	}

	/**
	 * Returns the summaries associated with the specified groups.
	 * 
	 * @param groups
	 *            the groups.
	 * @return the summaries associated with the specified groups.
	 */
	public Set<Entry<ArrayList<Summary<I, O>>, Set<G>>> summaries(Set<G> groups) {
		MultiValuedHashMap<ArrayList<Summary<I, O>>, G> m = new MultiValuedHashMap<ArrayList<Summary<I, O>>, G>();
		TreeSet<G> remainingGroups = new TreeSet<G>(groups);
		for (G g : groups) {
			ArrayList<Summary<I, O>> summary = group2summaries.get(g);
			if (summary != null) {
				m.add(summary, g);
				remainingGroups.remove(g);
			}
		}
		if (remainingGroups.size() > 0) {
			m.put(null, remainingGroups);
		}
		return m.entrySet();
	}

	/**
	 * Returns all of the summaries managed by this Aggregate.
	 * 
	 * @return all of the summaries managed by this Aggregate.
	 */
	public Set<Entry<ArrayList<Summary<I, O>>, Set<G>>> summaries() {
		return summaries(group2summaries.keySet());
	}

	/**
	 * Returns all the values that can be obtained from the summaries contained in this Aggregate.
	 * 
	 * @return all the values that can be obtained from the summaries contained in this Aggregate.
	 */
	public Iterable<Entry<ArrayList<O>, Set<G>>> values() {
		MultiValuedHashMap<ArrayList<O>, G> m = new MultiValuedHashMap<ArrayList<O>, G>();
		Entry<G, ArrayList<Summary<I, O>>> e = null;
		while ((e = removeFirst(group2summaries)) != null) {
			ArrayList<Summary<I, O>> s = e.getValue();
			ArrayList<O> values = null;
			for (int i = 0; i < s.size(); i++) {
				if (i == 0)
					values = new ArrayList<O>(s.size());
				values.add(i, s.get(i).value());
			}
			m.add(values, e.getKey());
		}
		return m.entrySet();
	}

	/**
	 * Constructs a new ArrayList of Summaries using the specified ArrayList of Summaries and values.
	 * 
	 * @param summaries
	 *            an ArrayList of Summaries.
	 * @param values
	 *            an array of values.
	 * @return a new ArrayList of Summaries
	 */
	protected ArrayList<Summary<I, O>> newSummaries(ArrayList<Summary<I, O>> summaries, I[] values) {
		ArrayList<Summary<I, O>> s = new ArrayList<Summary<I, O>>(values.length);
		for (int i = 0; i < values.length; i++) {
			s.add(i, summaries.get(i).clone());
			s.get(i).update(values[i]);
		}
		return s;
	}

	/**
	 * Removes the first entry from the specified map.
	 * 
	 * @param m
	 *            a map.
	 * @return the removed entry.
	 */
	protected Entry<G, ArrayList<Summary<I, O>>> removeFirst(Map<G, ArrayList<Summary<I, O>>> m) {
		Iterator<Entry<G, ArrayList<Summary<I, O>>>> i = m.entrySet().iterator();
		if (i.hasNext()) {
			Entry<G, ArrayList<Summary<I, O>>> e = i.next();
			m.remove(e.getKey());
			return e;
		} else
			return null;
	}

}

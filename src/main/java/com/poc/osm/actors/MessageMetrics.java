package com.poc.osm.actors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * message given the list of pending messages for an actor
 * @author pfreydiere
 *
 */
public class MessageMetrics implements Serializable {

	private Map<String, Long> allrefs = new HashMap<String, Long>();

	public MessageMetrics() {

	}

	public MessageMetrics(MessageMetrics metrics) {
		if (metrics == null)
			return;
		this.allrefs = new HashMap<String, Long>(metrics.allrefs);
	}

	public Map<String, Long> getAllrefs() {
		return allrefs;
	}

	public void mergeWith(MessageMetrics controlMessageMetrics) {
		Set<Entry<String, Long>> entrySet = controlMessageMetrics.getAllrefs()
				.entrySet();

		for (Iterator<Entry<String, Long>> iterator = entrySet.iterator(); iterator
				.hasNext();) {
			Entry<String, Long> e = iterator.next();

			Long al = allrefs.get(e.getKey());
			if (al == null) {
				allrefs.put(e.getKey(), e.getValue());
			} else {
				allrefs.put(e.getKey(), al + e.getValue());
			}

		}

	}

	public long computeMailBoxMessageCount() {
		Set<Entry<String, Long>> entrySet = allrefs.entrySet();
		long c = 0;
		for (Iterator<Entry<String, Long>> iterator = entrySet.iterator(); iterator
				.hasNext();) {
			Entry<String, Long> e = iterator.next();

			Long al = e.getValue();
			if (al != null)
				c += al;

		}

		return c;
	}
	
	/**
	 * update metrics
	 * 
	 * @param path
	 * @param delta
	 */
	public void addMetricFor(String path, long delta) {
		synchronized (this) {
			Long p = allrefs.get(path);
			if (p == null) {
				p = delta;
				allrefs.put(path, p);
			} else {
				allrefs.put(path, p + delta);
			}
		}
	}

	public void dump() {
		Set<Entry<String, Long>> entrySet = allrefs.entrySet();

		for (Iterator<Entry<String, Long>> iterator = entrySet.iterator(); iterator
				.hasNext();) {
			Entry<String, Long> e = iterator.next();
			System.out.println(" " + e.getKey() + "->" + e.getValue());

		}
	}

}

package com.poc.osm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.poc.osm.model.WayToConstruct.WayPointReference;

public class WayToConstructRegistry {
	
	private HashMap<Long, List<WayPointReference>> ts = new HashMap<Long, List<WayPointReference>>();

	private long waysRegistered = 0;

	public WayToConstructRegistry() {

	}

	private WayConstructListener wayConstructListener;

	public void setWayConstructListener(WayConstructListener l) {
		this.wayConstructListener = l;
	}

	public void register(WayToConstruct w) {
		w.register(this);
		waysRegistered++;
	}

	public long getWaysRegistered() {
		return waysRegistered;
	}

	void registerWayPoint(Long id, WayPointReference r) {
		List<WayPointReference> list = ts.get(id);
		if (list == null) {
			list = new ArrayList<WayPointReference>(1);
			ts.put(id, list);
		}
		list.add(r);
	}

	void signalPoint(OSMEntity entity) {
		List<WayPointReference> l = ts.get(entity.getId());
		if (l != null) {
			for (WayPointReference wr : l) {
				if (wr.signalPoint(entity)) {
					OSMEntity osmEntity = wr.getOSMEntity();
					if (wayConstructListener != null)
						wayConstructListener.signalWayOSMEntity(osmEntity);
					waysRegistered --;
					// System.out.println(" " + waysRegistered + " left");
				}
				// finish for the Way !!
			}
			ts.remove(entity.getId());
		}
	}

	/**
	 * give the points for filling the refs
	 * 
	 * @param points
	 */
	public void givePoints(Collection<OSMEntity> points) {
		for (OSMEntity e : points) {
			signalPoint(e);
		}
	}

}

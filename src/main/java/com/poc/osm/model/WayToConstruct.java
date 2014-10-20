package com.poc.osm.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;

public class WayToConstruct implements Serializable {

	static class WayPointReference {

		private WayToConstruct c;

		public WayPointReference(WayToConstruct c, int idx) {
			this.pointidx = idx;
			this.c = c;
		}

		private int pointidx;

		/**
		 * when a point is available,
		 * 
		 * @param entity
		 *            the point
		 * @return true if the way to construct is ready to be processed
		 */
		public boolean signalPoint(OSMEntity entity) {
			assert entity != null;
			if (c.associatedPoints[pointidx] == null) {
				c.fill--;
			}

			c.associatedPoints[pointidx] = entity;
			if (c.fill == 0) {
				// finished
				return true;
			}

			return false;
		}

		public OSMEntity getOSMEntity() {
			return c.constructOSMEntity();
		}

	}

	/**
	 * list of refs ids
	 */
	private long[] refids;

	/**
	 * associated points
	 */
	private OSMEntity[] associatedPoints;

	/**
	 * nomber of node left to fill
	 */
	private int fill;

	/**
	 * fields, might be null
	 */
	private HashMap<String, Object> fields;

	private long id;

	/**
	 * Constructor
	 * 
	 * @param refids
	 *            the list of referenced ids
	 * @param fields
	 */
	public WayToConstruct(long id, long[] refids, HashMap<String, Object> fields) {
		this.id = id;
		this.refids = refids;
		this.associatedPoints = new OSMEntity[refids.length];
		this.fill = refids.length;
		this.fields = fields;
	}

	/**
	 * internal method to register the {@link WayPointReference} in registry
	 * 
	 * @param r
	 */
	void register(WayToConstructRegistry r) {
		for (int i = 0; i < refids.length; i++) {
			long current = refids[i];
			r.registerWayPoint(current, new WayPointReference(this, i));
		}
		refids = null; //
	}

	public OSMEntity constructOSMEntity() {

		Polyline p = new Polyline();
		boolean started = false;
		for (OSMEntity e : associatedPoints) {
			assert e != null;
			if (!started) {
				p.startPath((Point) e.getGeometry());
				started = true;
			} else {
				p.lineTo((Point) e.getGeometry());
			}
		}

		return new OSMEntityGeometry(id, p, fields);

	}

}

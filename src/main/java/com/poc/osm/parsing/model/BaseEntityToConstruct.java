package com.poc.osm.parsing.model;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.poc.osm.model.OSMEntity;

public abstract class BaseEntityToConstruct {

	/**
	 * list of refs ids
	 */
	protected long[] refids;
	/**
	 * associated points
	 */
	protected OSMEntity[] associatedEntity;
	/**
	 * nomber of entities left to fill
	 */
	protected AtomicInteger fill = new AtomicInteger(0);
	/**
	 * fields, might be null
	 */
	protected HashMap<String, Object> fields;
	
	protected long id;

	public BaseEntityToConstruct(long id, long[] refids, HashMap<String, Object> fields) {
		this.id = id;
		assert refids != null;
		this.refids = refids;
		this.associatedEntity = new OSMEntity[refids.length];
		assert refids.length > 0;
		this.fill.set( refids.length); // FIXME might be zero
		this.fields = fields;
	}

	/**
	 * internal method to register the {@link EntityConstructReference} in registry
	 * 
	 * @param r
	 */
	protected void register(OSMEntityConstructRegistry r) {
		for (int i = 0; i < refids.length; i++) {
			long current = refids[i];
			r.registerWayPoint(current, new EntityConstructReference(this, i));
		}
		refids = null; //
	}

	public abstract OSMEntity constructOSMEntity();
	
}
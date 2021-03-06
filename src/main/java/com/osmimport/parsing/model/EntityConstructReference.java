package com.osmimport.parsing.model;

import com.osmimport.model.OSMEntity;
import com.osmimport.tools.IReport;

class EntityConstructReference {

	private BaseEntityToConstruct c;
	
	private int objectIdx;

	public EntityConstructReference(BaseEntityToConstruct c, int idx) {
		this.objectIdx = idx;
		this.c = c;
	}

	/**
	 * when a point is available,
	 * 
	 * @param entity
	 *            the point
	 * @return true if the way to construct is ready to be processed
	 */
	public boolean signalEntity(OSMEntity entity) {
		assert entity != null;
		if (c.associatedEntity[objectIdx] == null) {
			c.fill.decrementAndGet();
		}

		c.associatedEntity[objectIdx] = entity;
		if (c.fill.get() <= 0) {
			// finished
			return true;
		}

		return false;
	}

	public OSMEntity getOSMEntity(IReport report) {
		return c.constructOSMEntity(report);
	}

	@Override
	public String toString() {
		return " (Entity " + c.id + ") index "+ objectIdx;
	}
	
}



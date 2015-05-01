package com.poc.osm.parsing.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.poc.osm.model.OSMAttributedEntity;
import com.poc.osm.model.OSMEntity;

/**
 * registry handling the progressive construction of the entities
 * 
 * @author pfreydiere
 *
 */
public class OSMEntityConstructRegistry {
	
	/**
	 * hash containing the Point reference for a way
	 */
	private HashMap<Long, List<EntityConstructReference>> ts = new HashMap<Long, List<EntityConstructReference>>();

	private long entitiesRegistered = 0;

	public OSMEntityConstructRegistry() {

	}

	private OSMEntityConstructListener entityConstructListener;

	public void setEntityConstructListener(OSMEntityConstructListener l) {
		this.entityConstructListener = l;
	}

	public void register(BaseEntityToConstruct w) {
		w.register(this);
		entitiesRegistered++;
	}

	public long getEntitiesRegistered() {
		return entitiesRegistered;
	}

	void registerWayPoint(Long id, EntityConstructReference r) {
		List<EntityConstructReference> list = ts.get(id);
		if (list == null) {
			list = new ArrayList<EntityConstructReference>(1);
			ts.put(id, list);
		}
		list.add(r);
	}

	
	void signalEntity(OSMEntity entity) {
		assert entity != null;
		List<EntityConstructReference> l = ts.get(entity.getId());
		if (l != null) {
			for (EntityConstructReference wr : l) {
				if (wr.signalEntity(entity)) {
					OSMEntity osmEntity = wr.getOSMEntity();
					if (entityConstructListener != null)
						entityConstructListener.signalOSMEntity(osmEntity);
					entitiesRegistered --;
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
	 * @param entities
	 */
	public void giveEntity(Collection<OSMEntity> entities) {
		for (OSMEntity e : entities) {
			signalEntity(e);
		}
	}

}

package com.osmimport.parsing.model;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.osmimport.model.OSMEntity;
import com.osmimport.tools.IReport;

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

	private IReport constructReporter = null;

	/**
	 * constructor
	 * 
	 * @param report
	 *            the report object for the object construction
	 */
	public OSMEntityConstructRegistry(IReport report) {
		this.constructReporter = report;
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

	void registerEntityConstructReference(Long id, EntityConstructReference r) {
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
					OSMEntity osmEntity = wr.getOSMEntity(constructReporter);
					if (entityConstructListener != null) {
						entityConstructListener.signalOSMEntity(osmEntity);
					}
					entitiesRegistered--;
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

	public void dumpTS(Writer out) throws Exception {
		if (ts == null) {
			out.write("no entities left");
			return;
		}
		out.write("dump needed Entities (" + this + ")");
		for (Map.Entry<Long, List<EntityConstructReference>> es : ts.entrySet()) {
			out.write("    Entity " + es.getKey() + " needed for ->  \n");
			List<EntityConstructReference> l = es.getValue();
			for (EntityConstructReference r : l) {
				out.write("       " + r + "\n");
			}
		}
	}

}

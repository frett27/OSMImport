package com.osmimport.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * relation to objects
 * 
 * @author pfreydiere
 * 
 */
public class OSMRelation extends OSMAttributedEntity {

	private List<OSMRelatedObject> relations;

	public OSMRelation(long id, Map<String, Object> fields, List<OSMRelatedObject> relations) {
		super(id, fields);

		this.relations = relations;

	}

	public List<OSMRelatedObject> getRelations() {
		return this.relations;
	}

	@Override
	public OSMAttributedEntity copy() {

		Map<String, Object> f = this.fields;
		if (f != null) {
			f = new HashMap<String, Object>(f);
		}

		List<OSMRelatedObject> r = this.relations;
		if (r != null) {
			r = new ArrayList<OSMRelatedObject>(r);
		}

		return new OSMRelation(this.id, f, r);

	}

}

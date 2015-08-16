package com.osmimport.model;

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

	public OSMRelation(long id, Map<String, Object> fields,
			List<OSMRelatedObject> relations) {
		super(id, fields);

		this.relations = relations;

	}

	public List<OSMRelatedObject> getRelations() {
		return this.relations;
	}

}

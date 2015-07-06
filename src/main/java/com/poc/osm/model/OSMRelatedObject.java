package com.poc.osm.model;

public class OSMRelatedObject {

	private long relatedId;

	private String relation;

	private String type;

	public OSMRelatedObject(long relatedId, String relation, String type) {
		this.relatedId = relatedId;
		this.relation = relation;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public String getRelation() {
		return relation;
	}

	public long getRelatedId() {
		return relatedId;
	}

}

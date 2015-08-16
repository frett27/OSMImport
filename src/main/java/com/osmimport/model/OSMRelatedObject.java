package com.osmimport.model;

/**
 * OSM role
 * @author pfreydiere
 *
 */
public class OSMRelatedObject {

	private long relatedId;

	private String role;

	private String type;

	public OSMRelatedObject(long relatedId, String role, String type) {
		this.relatedId = relatedId;
		this.role = role;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public String getRole() {
		return role;
	}

	public long getRelatedId() {
		return relatedId;
	}

}

package com.poc.osm.model;

import java.io.Externalizable;
import java.util.HashMap;
import java.util.Map;

import com.esri.core.geometry.Geometry;

public abstract class OSMEntity implements Externalizable {

	protected long id;

	protected Map<String, Object> fields;

	public OSMEntity() {

	}

	public OSMEntity(long id, Map<String, Object> fields) {
		this.id = id;
		this.fields = fields;
	}

	/**
	 * return the id
	 * 
	 * @return
	 */
	public long getId() {
		return id;
	}

	/**
	 * get the geometry
	 * 
	 * @return
	 */
	public abstract Geometry getGeometry();

	/**
	 * get the geometry type
	 * 
	 * @return
	 */
	public abstract Geometry.Type getGeometryType();

	/**
	 * return the fields, if no fields, null is returned
	 * 
	 * @return
	 */
	public Map<String, Object> getFields() {
		return fields;
	}

	/**
	 * define the fields
	 * 
	 * @param fields
	 */
	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}

	/**
	 * define a field value
	 * 
	 * @param name
	 * @param value
	 */
	public void setValue(String name, Object value) {
		if (this.fields == null)
			this.fields = new HashMap<String, Object>();
		if (name != null)
			this.fields.put(name, value);
	}

}



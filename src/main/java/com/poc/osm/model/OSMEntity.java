package com.poc.osm.model;

import java.io.Externalizable;
import java.util.Map;

import com.esri.core.geometry.Geometry;

public abstract class OSMEntity extends OSMAttributedEntity implements Externalizable {

	
	protected OSMEntity() {
		super();
	}
	
	public OSMEntity(long id, Map<String, Object> fields) {
		super(id, fields);
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

}



package com.poc.osm.parsing.model;

import java.io.Serializable;
import java.util.HashMap;

import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.poc.osm.model.OSMEntity;
import com.poc.osm.model.OSMEntityGeometry;

/**
 * relation to construct
 * 
 * @author pfreydiere
 * 
 */
public class PolygonToConstruct extends BaseEntityToConstruct implements
		Serializable {

	public enum Role {
		OUTER, INNER
	}

	private Role[] idsRole;

	public PolygonToConstruct(long id, long[] refids,
			HashMap<String, Object> fields, Role[] idsRoles) {
		super(id, refids, fields);
		this.idsRole = idsRoles;

		assert idsRoles.length == refids.length;
	}

	@Override
	public OSMEntity constructOSMEntity() {

		Polygon polygon = new Polygon();

		for (int i = 0; i < refids.length; i++) {
			OSMEntity e = associatedEntity[i];
			assert e != null;
			polygon.add((MultiPath) e.getGeometry(), false);
		}

		return new OSMEntityGeometry(id, polygon, fields);
	}

}

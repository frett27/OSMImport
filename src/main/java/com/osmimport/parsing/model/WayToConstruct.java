package com.osmimport.parsing.model;

import java.io.Serializable;
import java.util.Map;

import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.osmimport.model.OSMEntity;
import com.osmimport.model.OSMEntityGeometry;

public class WayToConstruct extends BaseEntityToConstruct implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6618949187593219613L;

	
	/**
	 * Constructor
	 * 
	 * @param refids
	 *            the list of referenced ids
	 * @param fields
	 */
	public WayToConstruct(long id, long[] refids, Map<String, Object> fields) {
		super(id, refids, fields);
	}

	/**
	 * this function construct a OSM Entity if the feature has an area key, then
	 * a polygon is created, else a polyline
	 * 
	 * @return
	 */
	public OSMEntity constructOSMEntity() {

		String area = null;
		if (fields != null) {
			area = (String) fields.get("area");
		}

		MultiPath multiPath;

		if (area != null) {
			multiPath = new Polygon();
		} else {
			multiPath = new Polyline();
		}

		boolean started = false;
		for (OSMEntity e : associatedEntity) {
			assert e != null;
			if (!started) {
				multiPath.startPath((Point) e.getGeometry());
				started = true;
			} else {
				multiPath.lineTo((Point) e.getGeometry());
			}
		}

		return new OSMEntityGeometry(id, multiPath, fields);

	}

}

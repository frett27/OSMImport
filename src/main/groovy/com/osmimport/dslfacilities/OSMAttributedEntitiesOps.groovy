package com.osmimport.dslfacilities

import com.esri.core.geometry.Geometry;
import com.osmimport.model.OSMAttributedEntity;
import com.osmimport.model.OSMEntity;

class OSMAttributedEntitiesOps {
	
	// tests on geometries
	
	static boolean isPoint(OSMAttributedEntity o) {
		return o instanceof OSMEntity && o.geometry != null && o.geometryType == Geometry.Type.Point;
	}

	static boolean isPolygon(OSMAttributedEntity o) {
		return o instanceof OSMEntity && o.geometry != null && o.geometryType == Geometry.Type.Polygon;
	}
	
	static boolean isPolyline(OSMAttributedEntity o) {
		return o instanceof OSMEntity && o.geometry != null && o.geometryType == Geometry.Type.Polyline;
	}
	
	// tests on fields values
	
	static boolean has(OSMAttributedEntity o, String name){
		return o.fields && o.fields[name];
	}
	
	static boolean has(OSMAttributedEntity o, String name, String value){
		return has(o, name) && o.fields[name] == value;
	}
	
	
}

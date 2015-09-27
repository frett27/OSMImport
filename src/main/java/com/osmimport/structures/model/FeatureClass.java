package com.osmimport.structures.model;

import org.fgdbapi.thindriver.xml.EsriGeometryType;

import com.esri.core.geometry.Geometry;

public class FeatureClass extends Table {

	private EsriGeometryType geomType;
	private String srs;

	public FeatureClass(String featureClassName, EsriGeometryType geomType,
			String srs) {
		super(featureClassName);
		assert geomType != null;
		this.geomType = geomType;
		assert srs != null;
		this.srs = srs;

		// add geometry field
		addField(new Field("shape", FieldType.GEOMETRY, null));

	}

	public EsriGeometryType getGeomType() {
		return geomType;
	}

	public Geometry.Type getESRIGeomType() {

		switch (geomType) {
		case ESRI_GEOMETRY_POINT:
			return Geometry.Type.Point;

		case ESRI_GEOMETRY_POLYGON:
			return Geometry.Type.Polygon;

		case ESRI_GEOMETRY_POLYLINE:
			return Geometry.Type.Polyline;

		case ESRI_GEOMETRY_MULTIPOINT:
			return Geometry.Type.MultiPoint;
			
			// multipatch unsupported
		default:
			throw new RuntimeException("unknown geometry " + geomType);

		}

	}

	public String getSrs() {
		return srs;
	}
}

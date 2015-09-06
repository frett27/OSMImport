package com.osmimport.output.model;

import org.fgdbapi.thindriver.xml.EsriGeometryType;

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
	}

	public EsriGeometryType getGeomType() {
		return geomType;
	}

	public String getSrs() {
		return srs;
	}
}

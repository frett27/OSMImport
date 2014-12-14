package com.poc.osm.output.fields;

import org.fgdbapi.thindriver.swig.Row;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;

public class GeometryFieldSetter extends AbstractFieldSetter {

	public GeometryFieldSetter(String fieldName) {
		super(fieldName);
	}

	private byte[] geometry;

	@Override
	public String setValue(Object value) {

		if (!(value instanceof Geometry)) {
			return "value is not a geometry object";
		}

		Geometry g = (Geometry) value;
		try {
			if (g != null) {
				this.geometry = GeometryEngine.geometryToEsriShape(g);
			}
		} catch (Exception ex) {
			return "fail to construct geometry :" + ex.getMessage();
		}

		return null;
	}

	@Override
	public void store(Row row) {

		if (geometry == null) {
			// nothing to do :('
		} else {
			row.setGeometry(geometry);
		}
	}

	@Override
	public AbstractFieldSetter clone() {
		return new GeometryFieldSetter(fieldName);
	}

}

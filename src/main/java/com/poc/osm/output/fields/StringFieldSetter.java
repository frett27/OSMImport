package com.poc.osm.output.fields;

import org.fgdbapi.thindriver.swig.Row;

public class StringFieldSetter extends AbstractFieldSetter {

	public StringFieldSetter(String fieldName) {
		super(fieldName);
	}

	private String value;

	@Override
	public String setValue(Object value) {
		if (value == null) {
			this.value = null;
		} else if (value instanceof String) {
			this.value = (String) value;
		} else {
			this.value = value.toString();
		}

		return null;
	}

	@Override
	public void store(Row row) {
		if (this.value == null)
		{
			row.setNull(fieldName);
		} else 
		{
			row.setString(fieldName, value);
		}
		
	}
	
	@Override
	public AbstractFieldSetter clone() {
		return new StringFieldSetter(fieldName);
	}

}

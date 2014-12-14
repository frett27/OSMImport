package com.poc.osm.output.fields;

import org.fgdbapi.thindriver.swig.Row;

public class IntegerFieldSetter extends AbstractFieldSetter {

	public IntegerFieldSetter(String fieldName) {
		super(fieldName);
	}

	private Integer value = null;

	@Override
	public String setValue(Object value) {

		if (value == null)
			return null;

		try {

			if (value instanceof Integer) {
				this.value = ((Integer) value);
			} else if (value instanceof Short) {
				this.value = ((Short) value).intValue();
			} else if (value instanceof Long) {
				this.value =  ((Long) value).intValue();
			} else if (value instanceof String) {
				this.value = Integer.parseInt((String) value);
			} else {
				throw new Exception("unsupported conversion " + value.getClass() + " to " + Integer.class);
			}

		} catch (Exception ex) {
			return ex.getMessage();
		}
		return null;
	}

	@Override
	public void store(Row row) {
		if (value == null)
		{
			row.setNull(fieldName);
		} else 
		{
			row.setInteger(fieldName, value);
		}
	}
	
	@Override
	public AbstractFieldSetter clone() {
		return new IntegerFieldSetter(fieldName);
	}

}

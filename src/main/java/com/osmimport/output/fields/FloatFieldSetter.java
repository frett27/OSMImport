package com.osmimport.output.fields;

import org.fgdbapi.thindriver.swig.Row;

public class FloatFieldSetter extends AbstractFieldSetter {

	public FloatFieldSetter(String fieldName) {
		super(fieldName);
	}

	private Float value = null;

	@Override
	public String setValue(Object value) {

		if (value == null)
			return null;

		try {

			if (value instanceof Integer) {
				this.value = (float)((Integer) value);
			} else if (value instanceof Short) {
				this.value = (float)((Short) value).intValue();
			} else if (value instanceof Long) {
				this.value =  (float)((Long) value).intValue();
			} else if (value instanceof String) {
				this.value = Float.parseFloat((String) value);
			} else {
				throw new Exception("unsupported conversion " + value.getClass() + " to " + Float.class);
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
			row.setFloat(fieldName, value);
		}
	}
	
	@Override
	public AbstractFieldSetter clone() {
		return new FloatFieldSetter(fieldName);
	}

	
}

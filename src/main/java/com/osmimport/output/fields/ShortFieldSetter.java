package com.osmimport.output.fields;

import org.fgdbapi.thindriver.swig.Row;

public class ShortFieldSetter extends AbstractFieldSetter {

	public ShortFieldSetter(String fieldName) {
		super(fieldName);
	}

	private Short value = null;

	@Override
	public String setValue(Object value) {

		if (value == null)
			return null;

		try {

			if (value instanceof Integer) {
				this.value = ((Integer) value).shortValue();
			} else if (value instanceof Short) {
				this.value = ((Short) value);
			} else if (value instanceof Long) {
				this.value =  ((Long) value).shortValue();
			} else if (value instanceof String) {
				this.value = Short.parseShort((String) value);
			} else {
				throw new Exception("unsupported conversion " + value.getClass() + " to " + Short.class);
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
			row.setShort(fieldName, value);
		}
	}
	
	@Override
	public AbstractFieldSetter clone() {
		return new ShortFieldSetter(fieldName);
	}

}

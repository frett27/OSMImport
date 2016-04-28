package com.osmimport.output.fields;

import org.fgdbapi.thindriver.swig.Row;

public class DoubleFieldSetter extends AbstractFieldSetter {

	public DoubleFieldSetter(String fieldName) {
		super(fieldName);
	}

	private Double value = null;

	@Override
	public String setValue(Object value) {

		if (value == null)
			return null;

		try {

			if (value instanceof Integer) {
				this.value = (double)((Integer) value);
			} else if (value instanceof Short) {
				this.value = (double)((Short) value).intValue();
			} else if (value instanceof Long) {
				this.value =  (double)((Long) value).intValue();
			} else if (value instanceof String) {
				this.value = Double.parseDouble((String) value);
			} else {
				throw new Exception("unsupported conversion " + value.getClass() + " to " + Double.class);
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
			row.setDouble(fieldName, value);
		}
	}
	
	@Override
	public AbstractFieldSetter clone() {
		return new DoubleFieldSetter(fieldName);
	}

	
}

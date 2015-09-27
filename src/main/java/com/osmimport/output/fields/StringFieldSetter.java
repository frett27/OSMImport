package com.osmimport.output.fields;

import org.fgdbapi.thindriver.swig.Row;

public class StringFieldSetter extends AbstractFieldSetter {

	private int sizeOfField;
	
	public StringFieldSetter(String fieldName, int sizeOfField) {
		super(fieldName);
		this.sizeOfField = sizeOfField;
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
		
		// check size
		if (this.value != null) {
			if (this.value.length() > sizeOfField) {
				System.out.println("value " + this.value + " too big for field name " + this.getFieldName() + " truncate");
				this.value = this.value.substring(0, this.sizeOfField);
			}
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
		return new StringFieldSetter(fieldName, sizeOfField);
	}

}

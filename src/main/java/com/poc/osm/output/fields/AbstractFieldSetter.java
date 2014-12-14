package com.poc.osm.output.fields;

import org.fgdbapi.thindriver.swig.Row;

public abstract class AbstractFieldSetter {

	protected String fieldName;

	public AbstractFieldSetter(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName()
	{
		return this.fieldName;
	}
	
	/**
	 * define the value to set
	 * 
	 * @param value
	 * @return
	 */
	public abstract String setValue(Object value);

	/**
	 * store the value in the row
	 * 
	 * @param row
	 */
	public abstract void store(Row row);

	/**
	 * clone the object
	 */
	public abstract AbstractFieldSetter clone();

}

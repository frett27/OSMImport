package com.osmimport.structures.model;


public class Field {

	private String name;
	private FieldType type;
	private Integer length;
	
	public Field(String name, FieldType type, Integer length)
	{
		assert name != null && !name.isEmpty();
		this.name = name;
		this.type = type;
		this.length = length;
	}
	
	public String getName() {
		return name;
	}
	
	public FieldType getType() {
		return type;
	}
	
	public Integer getLength() {
		return length;
	}
	
	@Override
	public String toString() {
		return name + "(type :" + type + ", length:" + length + ")";
	}
	
}

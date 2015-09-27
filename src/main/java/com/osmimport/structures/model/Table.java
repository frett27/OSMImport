package com.osmimport.structures.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Table {

	private String tableName;

	private ArrayList<Field> fields = new ArrayList<>();

	public Table(String tableName) {
		assert tableName != null && !tableName.isEmpty();
		// TODO check invalid characters

		this.tableName = tableName;

	}

	public String getName() {
		return tableName;
	}

	public List<Field> getFields() {
		return Collections.unmodifiableList(fields);
	}
	
	public List<Field> getFieldsRef() {
		return fields;
	}

	public Table addField(Field field) {
		assert field != null;
		fields.add(field);
		return this;
	}

	public Table addStringField(String name, int size) {
		Field f = new Field(name, FieldType.STRING, size);
		return addField(f);
	}

	public Table addIntegerField(String name) {
		Field f = new Field(name, FieldType.INTEGER, null);
		return addField(f);
	}

	public Table addLongField(String name) {
		Field f = new Field(name, FieldType.LONG, null);
		return addField(f);
	}

	public Table addDoubleField(String name) {
		Field f = new Field(name, FieldType.DOUBLE, null);
		return addField(f);
	}

}

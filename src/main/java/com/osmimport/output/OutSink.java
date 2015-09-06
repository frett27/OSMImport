package com.osmimport.output;

import java.util.HashMap;
import java.util.Map;

import org.fgdbapi.thindriver.TableHelper;

import com.osmimport.output.model.Table;

public abstract class OutSink {

	private String path;

	private Map<String, Table> tables = new HashMap<String, Table>();

	public OutSink(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void addTable(Table t) {
		tables.put(t.getName(), t);
	}

	public Table[] listTables() {
		return tables.values().toArray(new Table[0]);
	}

	@Override
	public String toString() {
		return path;
	}

}

package com.poc.osm.output;

import java.util.HashMap;
import java.util.Map;

import org.fgdbapi.thindriver.TableHelper;

/**
 * Reference of a file geodatabase, and associated tables in it
 * 
 * @author pfreydiere
 */
public class GDBReference {

	private String path;
	
	private Map<String, TableHelper> tables = new HashMap<String,TableHelper>();

	public GDBReference(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	
	public void addTable(TableHelper t)
	{
		tables.put(t.getName(), t);
	}
	
	public TableHelper[] listTables(){
		return tables.values().toArray(new TableHelper[0]);
	}
	
	@Override
	public String toString() {
		return path;
	}
	
}

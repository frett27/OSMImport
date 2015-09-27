package com.osmimport.output.dsl;

import com.osmimport.structures.model.Table;

interface TableBuilderConstruct {

	void setCurrentTable(Table table);
	Table getCurrentTable();
	
}

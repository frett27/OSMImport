package com.osmimport.output.dsl

import com.osmimport.structures.model.Structure
import com.osmimport.structures.model.Table

class TStructure extends FactoryBuilderSupport implements TableBuilderConstruct {

	protected Structure currentStructure = new Structure();

	protected Table currentTable;

	void setCurrentTable(Table t) {
		this.currentTable = t;
	}

	Table getCurrentTable() {
		return this.currentTable;
	}


	public TStructure(init = true) {
		super(init);
	}

	def registerSupportNodes() {

		registerFactory("table", new TableFactory());
		registerFactory("featureclass", new FeatureClassFactory());
		registerFactory("_text", new FieldFactory());
		registerFactory("_integer", new FieldFactory());
		registerFactory("_double", new FieldFactory());
		registerFactory("_long", new FieldFactory());
	}


	/**
	 * create a structure (collection of tables and feature classes)
	 *
	 * @param c
	 *
	 * @return
	 */
	Structure structure(Closure c) {

		c.setDelegate(this);
		

		Object ret = c.call(this);

		return this.currentStructure;
	}
}

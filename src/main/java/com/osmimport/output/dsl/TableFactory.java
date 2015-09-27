package com.osmimport.output.dsl;

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;

import java.util.Map;

import org.fgdbapi.thindriver.TableHelper;

import com.osmimport.output.GDBReference;
import com.osmimport.output.OutSink;
import com.osmimport.structures.model.Structure;
import com.osmimport.structures.model.Table;

/**
 * Table factory
 */
public class TableFactory extends AbstractFactory {
	@Override
	public Object newInstance(FactoryBuilderSupport builder, Object name,
			Object value, Map attributes) throws InstantiationException,
			IllegalAccessException {

		System.out.println("TableFactory");

		assert value instanceof String;

		TableBuilderConstruct tb = (TableBuilderConstruct) builder;
		tb.setCurrentTable(new Table((String) value));

		return tb.getCurrentTable();
	}

	@Override
	public void onNodeCompleted(FactoryBuilderSupport builder, Object parent,
			Object node) {

		// attach the table to the gdb
		assert node instanceof Table;

		Table table = (Table) node;

		if (parent != null && parent instanceof OutSink) {
			OutSink r = (OutSink) parent;
			r.addTable(table);
		} else if (builder instanceof TStructure) {
			TStructure s = (TStructure) builder;
			s.currentStructure.put(table.getName(), table);
		} else {
			throw new RuntimeException(
					"error, table mmust be inside a gdb or csv element");
		}

		super.onNodeCompleted(builder, parent, node);

	}

}

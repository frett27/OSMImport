package com.poc.osm.output.dsl;

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;

import java.util.List;
import java.util.Map;

import org.fgdbapi.thindriver.TableHelper;
import org.fgdbapi.thindriver.xml.EsriGeometryType;

import com.poc.osm.output.GDBReference;

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

		TBuilder tb = (TBuilder) builder;
		tb.currentTableHelper = TableHelper.newTable((String)value);

		return tb.currentTableHelper;
	}
	
	@Override
	public void onNodeCompleted(FactoryBuilderSupport builder, Object parent,
			Object node) {
		
		// attach the table to the gdb
				assert node instanceof TableHelper;

				TableHelper th = (TableHelper) node;

				if (parent != null && parent instanceof GDBReference) {
					GDBReference r = (GDBReference) parent;
					r.addTable(th);
				}

				super.onNodeCompleted(builder, parent, node);
		
	}
	
}

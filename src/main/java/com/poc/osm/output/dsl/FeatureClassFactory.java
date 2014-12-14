package com.poc.osm.output.dsl;

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;

import java.util.List;
import java.util.Map;

import org.fgdbapi.thindriver.xml.EsriGeometryType;

import com.poc.osm.output.GDBReference;
import com.poc.osm.output.model.TableHelper;

/**
 * factory for a feature class
 * 
 * @author pfreydiere
 * 
 */
public class FeatureClassFactory extends AbstractFactory {

	@Override
	public Object newInstance(FactoryBuilderSupport builder, Object name,
			Object value, Map attributes) throws InstantiationException,
			IllegalAccessException {

		System.out.println("FeatureClassFactory");

		assert value instanceof List;
		List l = (List) value;
		if (l.size() != 3)
			throw new InstantiationException(
					"featureclass must have TABLE NAME and SRS Defined");

		String srs = (String) l.get(2);
		if (!"WGS84".equals(srs))
			throw new InstantiationException(
					"only WGS84 SRS is supported for the moment");

		TBuilder tb = (TBuilder) builder;
		tb.currentTableHelper = TableHelper.newFeatureClass((String) l.get(0),
				(EsriGeometryType) l.get(1),
				TableHelper.constructW84SpatialReference());

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

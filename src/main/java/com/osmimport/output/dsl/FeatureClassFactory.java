package com.osmimport.output.dsl;

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;

import java.util.List;
import java.util.Map;

import org.fgdbapi.thindriver.xml.EsriGeometryType;

import com.osmimport.output.OutSink;
import com.osmimport.structures.model.FeatureClass;
import com.osmimport.structures.model.Table;

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

		// System.out.println("FeatureClassFactory");

		assert value instanceof List;
		List l = (List) value;
		if (l.size() != 3)
			throw new InstantiationException(
					"featureclass must have TABLE NAME and SRS Defined");

		String srs = (String) l.get(2);
		if (!"WGS84".equals(srs))
			throw new InstantiationException(
					"only WGS84 SRS is supported for the moment");

		TableBuilderConstruct tb = (TableBuilderConstruct) builder;
		try {
			tb.setCurrentTable ( new FeatureClass((String) l.get(0), (EsriGeometryType) l.get(1),
					srs));
			return tb.getCurrentTable();
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}

	}

	@Override
	public void onNodeCompleted(FactoryBuilderSupport builder, Object parent,
			Object node) {

		// attach the table to the gdb
		assert node instanceof Table;

		Table th = (Table) node;

		if (parent != null && parent instanceof OutSink) {
			OutSink r = (OutSink) parent;
			r.addTable(th);
		} else if (builder instanceof TStructure) {
			TStructure s = (TStructure) builder;
			s.currentStructure.put(th.getName(), th);
		} 

		super.onNodeCompleted(builder, parent, node);
	}

}

package com.osmimport.output.dsl;

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;

import java.util.Map;

import com.osmimport.output.CSVFolderReference;
import com.osmimport.output.GDBReference;

/**
 * factory for a GDB description
 * 
 * @author pfreydiere
 * 
 */
public class CsvFactory extends AbstractFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see groovy.util.Factory#newInstance(groovy.util.FactoryBuilderSupport,
	 * java.lang.Object, java.lang.Object, java.util.Map)
	 */
	@Override
	public Object newInstance(FactoryBuilderSupport builder, Object name,
			Object value, Map attributes) throws InstantiationException,
			IllegalAccessException {

		if (!attributes.containsKey("path"))
			throw new InstantiationException("path key must be defined for csv");

		String path = (String) attributes.get("path");

		return new CSVFolderReference(path);

	}

}

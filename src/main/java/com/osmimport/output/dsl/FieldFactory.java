package com.osmimport.output.dsl;

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;

import java.util.Map;

import com.osmimport.output.dsl.TBuilder;

/**
 * factory for a field inside a GDB structure description
 * 
 * @author pfreydiere
 * 
 */
public class FieldFactory extends AbstractFactory {

	@Override
	public Object newInstance(FactoryBuilderSupport builder, Object name,
			Object value, Map attributes) throws InstantiationException,
			IllegalAccessException {

		assert builder instanceof TableBuilderConstruct;
		TableBuilderConstruct b = (TableBuilderConstruct) builder;

		assert b.getCurrentTable() != null;

		if ("_text".equals(name)) {
			int size = 255;
			if (attributes.containsKey("size")) {
				size = (Integer) attributes.get("size");
			}
			b.getCurrentTable().addStringField((String) value, size);

		} else if ("_integer".equals(name)) {
			int size = 4;
			if (attributes.containsKey("size")) {
				// check this is an integer
				size = (Integer) attributes.get("size");
			}

			b.getCurrentTable().addIntegerField((String) value);

		} else if ("_long".equals(name)) {

			b.getCurrentTable().addLongField((String) value);

		} else if ("_double".equals(name)) {

			b.getCurrentTable().addDoubleField((String) value);

		} else if ("_float".equals(name)) {

			b.getCurrentTable().addSingleField((String) value);

		} else if ("_short".equals(name)) {

			b.getCurrentTable().addSmallIntegerField((String) value);

		} else {
			throw new InstantiationException("field type :" + name + " unknown");
		}

		return builder; // handled

	}

}

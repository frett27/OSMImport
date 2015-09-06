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

		assert builder instanceof TBuilder; 
		TBuilder b = (TBuilder) builder;

		assert b.currentTable != null;

		if ("_text".equals(name)) {
			int size = 255;
			if (attributes.containsKey("size")) {
				size = (Integer) attributes.get("size");
			}
			b.currentTable.addStringField((String) value, size);

			return null;

		} else if ("_integer".equals(name)) {
			int size = 4;
			if (attributes.containsKey("size")) {
				// check this is an integer
				size = (Integer) attributes.get("size");
			}

			b.currentTable.addIntegerField((String) value);

			return null;

		} else if ("_long".equals(name)) {

			b.currentTable.addLongField((String) value);

			return null;
			
			
		} else if ("_double".equals(name)) {

			b.currentTable.addDoubleField((String) value);

			return null;

		} else {
			throw new InstantiationException("field type :" + name + " unknown");
		}

	}

}

package com.osmimport.output.actors.gdb;

import java.util.ArrayList;

import org.fgdbapi.thindriver.swig.FieldDef;
import org.fgdbapi.thindriver.swig.FieldType;
import org.fgdbapi.thindriver.swig.Table;
import org.fgdbapi.thindriver.swig.VectorOfFieldDef;
import org.slf4j.LoggerFactory;

import com.osmimport.output.fields.AbstractFieldSetter;
import com.osmimport.output.fields.DoubleFieldSetter;
import com.osmimport.output.fields.FloatFieldSetter;
import com.osmimport.output.fields.GeometryFieldSetter;
import com.osmimport.output.fields.IntegerFieldSetter;
import com.osmimport.output.fields.ShortFieldSetter;
import com.osmimport.output.fields.StringFieldSetter;

public class GDBOutputTools {

	static ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory
			.getLogger(GDBOutputTools.class);

	public static ArrayList<AbstractFieldSetter> createCompiledFieldSetters(
			Table t) {
		ArrayList<AbstractFieldSetter> r = new ArrayList<AbstractFieldSetter>();
		VectorOfFieldDef fields = t.getFields();
		if (fields != null) {

			for (int i = 0; i < fields.size(); i++) {
				FieldDef fieldDef = fields.get(i);

				FieldType fieldType = fieldDef.getType();
				String fieldName = fieldDef.getName();

				if (FieldType.fieldTypeGeometry == fieldType) {
					log.debug("adding geometry field " + fieldName);
					r.add(new GeometryFieldSetter(fieldName));
				} else if (FieldType.fieldTypeString == fieldType) {
					log.debug("adding string field " + fieldName);
					r.add(new StringFieldSetter(fieldName, fieldDef.getLength()));
				} else if (FieldType.fieldTypeInteger == fieldType) {
					log.debug("adding integer field " + fieldName);
					r.add(new IntegerFieldSetter(fieldName));
				} else if (FieldType.fieldTypeSmallInteger == fieldType) {
					log.debug("adding short field " + fieldName);
					r.add(new ShortFieldSetter(fieldName));
				} else if (FieldType.fieldTypeDouble == fieldType) {
					log.debug("adding double field " + fieldName);
					r.add(new DoubleFieldSetter(fieldName));
				} else if (FieldType.fieldTypeSingle == fieldType) {
					log.debug("adding single field " + fieldName);
					r.add(new FloatFieldSetter(fieldName));
				}
				else {
					log.warn("field type "
							+ fieldType
							+ " is unsupported, it will not be added to the table/featureclass");
				}

			}

		}
		return r;
	}

}

package com.poc.osm.output.actors;

import java.util.ArrayList;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esrifrance.fgdbapi.swig.FieldDef;
import com.esrifrance.fgdbapi.swig.FieldType;
import com.esrifrance.fgdbapi.swig.Table;
import com.esrifrance.fgdbapi.swig.VectorOfFieldDef;
import com.poc.osm.model.OSMEntity;
import com.poc.osm.output.actors.messages.CompiledFieldsMessage;
import com.poc.osm.output.fields.AbstractFieldSetter;
import com.poc.osm.output.fields.GeometryFieldSetter;
import com.poc.osm.output.fields.IntegerFieldSetter;
import com.poc.osm.output.fields.StringFieldSetter;

public class FieldsCompilerActor extends UntypedActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	// cloned elements
	private AbstractFieldSetter[] compiledFields;

	private ActorRef compiledOutputActor;

	public FieldsCompilerActor(Table t, ActorRef compiledOutputActor) {

		assert compiledOutputActor != null;

		this.compiledOutputActor = compiledOutputActor;

		log.debug("creating the compiled fields");

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
					r.add(new StringFieldSetter(fieldName));
				} else if (FieldType.fieldTypeInteger == fieldType) {
					log.debug("adding integer field " + fieldName);
					r.add(new IntegerFieldSetter(fieldName));
				} else {
					log.warning("field type "
							+ fieldType
							+ " is unsupported, it will not be added to the table/featureclass");
				}

			}

		}

		compiledFields = r.toArray(new AbstractFieldSetter[0]);

	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof OSMEntity) {

			OSMEntity osme = (OSMEntity) message;

			StringBuilder sb = new StringBuilder();

			AbstractFieldSetter[] n = new AbstractFieldSetter[compiledFields.length];
			for (int i = 0; i < n.length; i++) {

				n[i] = compiledFields[i].clone();

				String rs;
				if (n[i] instanceof GeometryFieldSetter) {
					Geometry geometry = osme
							.getGeometry();
					// log.info("geometry :" + GeometryEngine.geometryToJson(4326, geometry));
					rs = ((GeometryFieldSetter) n[i]).setValue(geometry);
					if (rs != null)
						sb.append(rs).append("\n");

				} else {

					Map<String, Object> flds = osme.getFields();

					if (flds != null) {

						rs = n[i].setValue(flds.get(n[i].getFieldName()));
						if (rs != null)
							sb.append(rs).append("\n");

					}

				}

			}

			if (sb.length() > 0) {
				log.debug("error on entity :" + osme.getId() + " :"
						+ sb.toString());
			}

			compiledOutputActor.tell(new CompiledFieldsMessage(n), getSelf());

		}

	}

}

package com.osmimport.output.actors.csv;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.osmimport.actors.MeasuredActor;
import com.osmimport.model.OSMAttributedEntity;
import com.osmimport.model.OSMEntity;
import com.osmimport.output.model.Field;
import com.osmimport.output.model.FieldType;
import com.osmimport.output.model.Table;
import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * output into CSV fole for a table
 * 
 * @author pfreydiere
 * 
 */
public class CSVOutputActor extends MeasuredActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	/**
	 * table structure
	 */
	private OutputStreamWriter outputStreamWriter;

	
	/**
	 * table Definition
	 */
	private Table t = null;

	/**
	 * the flow regulator
	 */
	private ActorRef flowRegulatorActorRef;

	public CSVOutputActor(Table t, OutputStream out, ActorRef flowRegulator) {
		assert t != null;
		assert out != null;
		this.outputStreamWriter = new OutputStreamWriter(out);
		this.t = t;
		this.flowRegulatorActorRef = flowRegulator;

	}

	@Override
	public void preStart() throws Exception {
		super.preStart();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see akka.actor.UntypedActor#postStop()
	 */
	@Override
	public void postStop() throws Exception {

		log.info("closing  actor" + getSelf());
		if (outputStreamWriter != null) {
			outputStreamWriter.close();
			outputStreamWriter = null;
		}
		super.postStop();
	}

	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		if (message instanceof OSMAttributedEntity) {

			handleSingleMessage((OSMAttributedEntity) message);

		} else if (message instanceof List) {

			List<OSMAttributedEntity> l = (List<OSMAttributedEntity>) message;
			for (OSMAttributedEntity e : l) {
				try {
					handleSingleMessage(e);
				} catch (Exception ex) {
					log.error("error in handling message " + ex.getMessage(),
							ex);
				}
			}

		}

		else {
			unhandled(message);
		}

	}

	private void handleSingleMessage(OSMAttributedEntity osme) throws Exception {

		StringBuilder sb = new StringBuilder();

		List<Field> flds = t.getFieldsRef();
		for (Field f : flds) {
			String curValue = "";

			if (f.getType() == FieldType.GEOMETRY && osme instanceof OSMEntity) {
				Geometry geometry = ((OSMEntity) osme).getGeometry();
				if (geometry != null) {
					byte[] b = GeometryEngine.geometryToEsriShape(geometry);
					curValue = Base64.encode(b).replaceAll("\n", "");
				}

			} else {
				Object fv = osme.getFields().get(f.getName());
				if (f.getType() == FieldType.STRING) {
					if (fv != null) {
						curValue = "\""
								+ fv.toString().replaceAll("\"", "\"\"") + "\"";
					}

				} else {

					if (fv != null) {
						curValue = fv.toString();
					}

				}

			}

			if (sb.length() > 0)
				sb.append(",");

			sb.append(curValue);

		} // for

		// cr
		sb.append('\n');
		
		// write line
		outputStreamWriter.write(sb.toString());

	}
}

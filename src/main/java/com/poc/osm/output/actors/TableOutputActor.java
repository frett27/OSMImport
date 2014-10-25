package com.poc.osm.output.actors;

import java.util.Map.Entry;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esrifrance.fgdbapi.swig.Row;
import com.esrifrance.fgdbapi.swig.Table;
import com.poc.osm.model.OSMEntity;
import com.poc.osm.regulation.MessageRegulation;

/**
 * Actor for inserting datas into a Table or FeatureClass
 * 
 * @author pfreydiere
 * 
 */
public class TableOutputActor extends UntypedActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	/**
	 * table structure
	 */
	private Table table;

	private int oid = 1;

	/**
	 * start time between measures
	 */
	private long start = -1;

	/**
	 * the flow regulator
	 */
	private ActorRef flowRegulatorActorRef;

	public TableOutputActor(Table t, ActorRef flowRegulator) {
		assert t != null;
		this.table = t;
		this.flowRegulatorActorRef = flowRegulator;
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();

		table.setWriteLock();
		table.setLoadOnlyMode(true);
	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see akka.actor.UntypedActor#postStop()
	 */
	@Override
	public void postStop() throws Exception {

		table.setLoadOnlyMode(false);
		table.freeWriteLock();

		super.postStop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	@Override
	public void onReceive(Object message) throws Exception {

		if (!(message instanceof OSMEntity)) {
			return;
		}

		OSMEntity e = (OSMEntity) message;
		try {

			Row r = table.createRowObject();

			// r.setInteger("OBJECTID", oid++);

			Geometry geometry = e.getGeometry();

			if (geometry != null) {
				r.setGeometry(GeometryEngine.geometryToEsriShape(geometry));
			}

			if (e.getFields() != null) {
				for (Entry<String, Object> t : e.getFields().entrySet()) {
					Object v = t.getValue();
					try {
						if (v == null) {
							r.setNull(t.getKey());
						} else {
							r.setString(t.getKey(), v.toString());
						}
					} catch (Exception ex) {
						log.error(
								"error while processing column " + t.getKey()
										+ " with value " + v + " -> "
										+ ex.getMessage(), ex);
					}
				}
			}
			table.insertRow(r);

		} catch (Exception ex) {
			log.error("error handling entity " + e + ":" + ex.getMessage(), ex);
		}

		flowRegulatorActorRef.tell(new MessageRegulation(-1), getSelf());

		if (oid++ % 10000 == 0) {

			long t = -1;

			if (start != -1)
				t = (System.currentTimeMillis() - start);

			log.info("" + oid + " elements, last chunk processed in " + t
					+ "ms -> " + ((10000.0) / t * 1000) + " o/s");
			start = System.currentTimeMillis();
		}

	}

}

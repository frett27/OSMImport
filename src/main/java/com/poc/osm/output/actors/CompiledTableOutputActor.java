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
import com.poc.osm.output.actors.messages.CompiledFieldsMessage;
import com.poc.osm.output.fields.AbstractFieldSetter;
import com.poc.osm.regulation.MessageRegulation;

public class CompiledTableOutputActor extends UntypedActor {

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

	public CompiledTableOutputActor(Table t, ActorRef flowRegulator) {
		assert t != null;
		this.table = t;
		this.flowRegulatorActorRef = flowRegulator;

		table.setWriteLock();
		table.setLoadOnlyMode(true);

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

		log.info("Stopping actor" + getSelf());
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

		if (!(message instanceof CompiledFieldsMessage)) {
			return;
		}
		CompiledFieldsMessage e = (CompiledFieldsMessage) message;
		try {
			Row r = table.createRowObject();

			// r.setInteger("OBJECTID", oid++);

			AbstractFieldSetter[] setters = e.getSetters();
			for (AbstractFieldSetter f : setters) {
				try {
					f.store(r);
				} catch (Exception ex) {
					log.error(
							"error in storing value on " + f + " :"
									+ ex.getMessage(), ex);
				}
			}

			table.insertRow(r);

			flowRegulatorActorRef.tell(new MessageRegulation(-1), getSelf());
		} catch (Exception ex) {
			log.error("error processing entity :" + e + ":" + ex.getMessage(),
					ex);
		}
		if (start != -1) {

			long t = (System.currentTimeMillis() - start);
			if (t > 10000000) {
				log.info("" + oid + " elements, processed for actor "
						+ getSelf().path());
			}

		}

		if (oid++ % 10000 == 0) {

			long t = -1;

			if (start != -1)
				t = (System.currentTimeMillis() - start);

			log.info("" + oid + " elements, last chunk processed in " + t + "ms -> writing speed : "
					+ ((10000.0) / t * 1000) + " o/s");
			start = System.currentTimeMillis();
		}

	}
}

package com.osmimport.output.actors.gdb;

import org.fgdbapi.thindriver.swig.Row;
import org.fgdbapi.thindriver.swig.Table;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.osmimport.actors.MeasuredActor;
import com.osmimport.output.actors.gdb.messages.CompiledFieldsMessage;
import com.osmimport.output.fields.AbstractFieldSetter;

public class CompiledTableOutputActor extends MeasuredActor {

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

	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		if (!(message instanceof CompiledFieldsMessage)) {
			log.warning("message received is not a "
					+ CompiledFieldsMessage.class.getSimpleName());
			return;
		}
		CompiledFieldsMessage e = (CompiledFieldsMessage) message;
		try {
			synchronized (CompiledTableOutputActor.class) {

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
				// r.delete(); 
				
			}

		} catch (Exception ex) {
			log.error("error storing entity :" + e + ":" + ex.getMessage(), ex);
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

			log.info("" + oid + " elements, last chunk processed in " + t
					+ "ms -> writing speed : " + ((10000.0) / t * 1000)
					+ " o/s");
			start = System.currentTimeMillis();
		}

	}
}

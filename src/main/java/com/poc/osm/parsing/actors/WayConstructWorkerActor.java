package com.poc.osm.parsing.actors;

import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.poc.osm.actors.MeasuredActor;
import com.poc.osm.messages.MessageNodes;
import com.poc.osm.messages.MessageWay;
import com.poc.osm.model.OSMEntity;
import com.poc.osm.parsing.actors.messages.MessageClusterRegistration;
import com.poc.osm.parsing.actors.messages.MessageParsingSystemStatus;
import com.poc.osm.parsing.actors.messages.MessageWayToConstruct;
import com.poc.osm.parsing.model.BaseEntityToConstruct;
import com.poc.osm.parsing.model.OSMEntityConstructListener;
import com.poc.osm.parsing.model.OSMEntityConstructRegistry;
import com.poc.osm.parsing.model.WayToConstruct;
import com.poc.osm.regulation.FlowRegulator;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;

/**
 * Actor constructing Ways
 * 
 * @author pfreydiere
 * 
 */
public class WayConstructWorkerActor extends MeasuredActor {

	/**
	 * logger
	 */
	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	/**
	 * the registry to know which points are necessary for a constructing way
	 */
	private OSMEntityConstructRegistry reg = new OSMEntityConstructRegistry();

	/**
	 * handled blocks, list of handled blocks for ways
	 */
	private Set<Long> handledBlocks = new HashSet<Long>();

	/**
	 * max way to handle by reading
	 */
	private long maxWayToConstruct = 80000;

	private enum State {
		REGISTRATION_PHASE, PROCESSING_PHASE
	}

	/**
	 * current actor state
	 */
	private State currentState = State.REGISTRATION_PHASE;

	/**
	 * output stream chain
	 */
	private ActorRef output;

	/**
	 * flag for telling we still have ways to resolve and an other file read is
	 * necessary
	 */
	private boolean needMoreRead = true;

	private boolean hasinformed = false;

	private ActorRef wayDispatcher;

	private ActorRef polygonDispatcher;
	
	private Counter waysMetrics;

	public WayConstructWorkerActor(ActorRef dispatcher,
			ActorRef polygonDispatcher,ActorRef outputRef, long maxWaysToConstruct) {
		this.wayDispatcher = dispatcher;
		this.polygonDispatcher = polygonDispatcher;
		this.maxWayToConstruct = maxWaysToConstruct;
		this.output = outputRef;

		log.info("take " + maxWaysToConstruct + " for ways constructions");

		waysMetrics = Metrics.newCounter(FlowRegulator.class, getSelf().path()
				.name() + " ways number");

	}

	@Override
	public void preStart() throws Exception {
		super.preStart();

		reg.setEntityConstructListener(new OSMEntityConstructListener() {
			@Override
			public void signalOSMEntity(OSMEntity e) {
				if (log.isDebugEnabled())
					log.debug("emit way " + e);

				// tell the output there is a new constructed way
				tell(output, new MessageWay(e), getSelf());
				tell(polygonDispatcher, new MessageWay(e), getSelf());
				waysMetrics.dec();
			}
		});
	}

	@Override
	public void postStop() throws Exception {
		reg.setEntityConstructListener(null);
		
		if (reg.getEntitiesRegistered() >0)
		{
			log.warning("" + reg.getEntitiesRegistered() + " entities are still registered and not finished");
			reg.dumpTS(new OutputStreamWriter(System.out));
		}
		
		super.postStop();
	}

	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		if (currentState == State.REGISTRATION_PHASE) {

			if (message instanceof MessageParsingSystemStatus) {

				MessageParsingSystemStatus m = (MessageParsingSystemStatus) message;
				if (m == MessageParsingSystemStatus.INITIALIZE) {
					// launch registration

					tell(wayDispatcher,
							MessageClusterRegistration.ASK_FOR_WAY_REGISTRATION,
							getSelf());
					
					currentState = State.PROCESSING_PHASE;
				}
				
				

			}  else {
				unhandled(message);
			}

		} else if (currentState == State.PROCESSING_PHASE) {

			if (message instanceof MessageParsingSystemStatus) {

				if (message == MessageParsingSystemStatus.START_READING_FILE) {

					this.needMoreRead = false;
					// a read started

					hasinformed = false;

				} else if (message == MessageParsingSystemStatus.END_READING_FILE) {

					if (needMoreRead || (reg.getEntitiesRegistered() > 0)) {
						tell(wayDispatcher,
								MessageClusterRegistration.NEED_MORE_READ,
								getSelf());
						log.info("I need more read");
					} else {
						tell(wayDispatcher,
								MessageClusterRegistration.ALL_BLOCKS_READ,
								getSelf());
					}

				} else if (message == MessageParsingSystemStatus.TERMINATE) {

					reset();

				} else {
					unhandled(message);
				}

			} else if (message instanceof MessageNodes) {

				MessageNodes mn = (MessageNodes) message;
				reg.giveEntity(mn.getNodes());

				needMoreRead = needMoreRead || reg.getEntitiesRegistered() > 0;

			} else if (message instanceof MessageWayToConstruct) {

				MessageWayToConstruct mw = (MessageWayToConstruct) message;

				// if block is already handled, skip it
				if (handledBlocks.contains(mw.getBlockid())) {
					// skipped, already processed, we save time
					return;
				}

				needMoreRead = needMoreRead || reg.getEntitiesRegistered() > 0;

				// the block is not already handled,
				// check if we have room to handle it
				if (reg.getEntitiesRegistered() > maxWayToConstruct) {
					if (log.isDebugEnabled())
						log.debug("too much ways left - handled blocks :"
								+ handledBlocks.size());

					return;
				}

				// register the block, and prepare for parsing
				List<WayToConstruct> waysToConstruct = mw.getWaysToConstruct();
				for (BaseEntityToConstruct w : waysToConstruct) {
					reg.register(w);
					waysMetrics.inc();
				}

				if (!hasinformed) {
					// in case we miss some ways registration,
					// tell the cluster that we need more reads of the input
					// file
					tell(wayDispatcher, MessageClusterRegistration.NEED_MORE_READ,
							getSelf());
					hasinformed = true;
				}

				// the bloc has been handled
				handledBlocks.add(mw.getBlockid());

				log.info(reg.getEntitiesRegistered()
						+ " ways registered for actor " + getSelf().path());

			} else {
				unhandled(message);
			}

		} else {
			unhandled(message);
		}

	}

	/**
	 * @throws Exception
	 */
	protected void reset() throws Exception {
		reg = new OSMEntityConstructRegistry();
		preStart();
		currentState = State.REGISTRATION_PHASE;
		log.debug("current state :" + currentState);
	}

}

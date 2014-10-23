package com.poc.osm.parsing.actors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.poc.osm.messages.MessageNodes;
import com.poc.osm.messages.MessageWay;
import com.poc.osm.model.OSMEntity;
import com.poc.osm.model.WayConstructListener;
import com.poc.osm.model.WayToConstruct;
import com.poc.osm.model.WayToConstructRegistry;
import com.poc.osm.parsing.actors.messages.MessageClusterRegistration;
import com.poc.osm.parsing.actors.messages.MessageOutputRef;
import com.poc.osm.parsing.actors.messages.MessageParsingSystemStatus;
import com.poc.osm.parsing.actors.messages.MessageWayToConstruct;

/**
 * Actor constructing Ways
 * 
 * @author pfreydiere
 * 
 */
public class WayConstructorActor extends UntypedActor {

	/**
	 * logger
	 */
	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	/**
	 * the registry to know which points are necessary for a constructing way
	 */
	private WayToConstructRegistry reg = new WayToConstructRegistry();

	/**
	 * handled blocks, list of handled blocks for ways
	 */
	private Set<Long> handledBlocks = new HashSet<Long>();

	/**
	 * max way to handle by reading
	 */
	private int maxWayToConstruct = 1000000;

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

	
	private ActorRef dispatcher;
	
	
	public WayConstructorActor(ActorRef dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();

		reg.setWayConstructListener(new WayConstructListener() {
			@Override
			public void signalWayOSMEntity(OSMEntity e) {
				if (log.isDebugEnabled())
					log.debug("emit way " + e);
				// tell the output there is a new constructed way
				output.tell(new MessageWay(e), getSelf());
			}
		});
	}

	@Override
	public void postStop() throws Exception {
		reg.setWayConstructListener(null);
		super.postStop();
	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (currentState == State.REGISTRATION_PHASE) {

			if (message instanceof MessageParsingSystemStatus) {

				MessageParsingSystemStatus m = (MessageParsingSystemStatus) message;
				if (m == MessageParsingSystemStatus.INITIALIZE) {
					// launch registration
					dispatcher
							.tell(MessageClusterRegistration.ASK_FOR_WAY_REGISTRATION,
									getSelf());
				}

			} else if (message instanceof MessageOutputRef) {
				MessageOutputRef o = (MessageOutputRef) message;
				this.output = o.getOutput();
				currentState = State.PROCESSING_PHASE;
				log.debug("current state :" + currentState);

			} else {
				unhandled(message);
			}

		} else if (currentState == State.PROCESSING_PHASE) {

			if (message instanceof MessageParsingSystemStatus) {

				if (message == MessageParsingSystemStatus.START_READING_FILE) {

					this.needMoreRead = false;
					// a read started

				} else if (message == MessageParsingSystemStatus.END_READING_FILE) {

					if (needMoreRead || (reg.getWaysRegistered() > 0)) {
						dispatcher.tell(
								MessageClusterRegistration.NEED_MORE_READ,
								getSelf());
					} else {
						dispatcher.tell(
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
				reg.givePoints(mn.getNodes());
				
				needMoreRead = needMoreRead || reg.getWaysRegistered() > 0;

			} else if (message instanceof MessageWayToConstruct) {

				MessageWayToConstruct mw = (MessageWayToConstruct) message;

				// if block is already handled, skip it
				if (handledBlocks.contains(mw.getBlockid())) {
					// skipped, already processed
					return;
				}

				// the block is not already handled, 
				// check if we have room to handle it
				if (reg.getWaysRegistered() > maxWayToConstruct) {
					if (log.isDebugEnabled())
						log.debug("too much ways left - handled blocks :"
								+ handledBlocks.size());
					
					if (!needMoreRead)
					{
						// inform that this block could not be handled
						// and we need a more read of the input file
						dispatcher.tell(
								MessageClusterRegistration.NEED_MORE_READ,
								getSelf());
					}
					needMoreRead = true;
					return;
				}

				// register the block, and prepare for parsing
				
				List<WayToConstruct> waysToConstruct = mw.getWaysToConstruct();
				for (WayToConstruct w : waysToConstruct) {
					reg.register(w);
				}

				handledBlocks.add(mw.getBlockid());

				log.info("Current nb of registered ways :"
						+ reg.getWaysRegistered());

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
		reg = new WayToConstructRegistry();
		preStart();
		currentState = State.REGISTRATION_PHASE;
		log.debug("current state :" + currentState);
	}


}

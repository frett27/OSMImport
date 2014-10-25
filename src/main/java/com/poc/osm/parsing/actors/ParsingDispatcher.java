package com.poc.osm.parsing.actors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.poc.osm.messages.MessageNodes;
import com.poc.osm.parsing.actors.messages.MessageClusterRegistration;
import com.poc.osm.parsing.actors.messages.MessageOutputRef;
import com.poc.osm.parsing.actors.messages.MessageParsingSystemStatus;
import com.poc.osm.parsing.actors.messages.MessageWayToConstruct;

/**
 * Object that register wayconstruct actors, for each OSM entity, it dispatch
 * the message to the registrees
 * 
 * @author pfreydiere
 * 
 */
public class ParsingDispatcher extends UntypedActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Set<ActorRef> wayDispatcher = new HashSet<ActorRef>();

	private ActorRef outputRef;

	private boolean stillneedmoreread = false;

	public ParsingDispatcher(ActorRef outputRef) {
		assert outputRef != null;
		this.outputRef = outputRef;
	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof MessageClusterRegistration) {

			MessageClusterRegistration m = (MessageClusterRegistration) message;
			if (m == MessageClusterRegistration.ASK_FOR_WAY_REGISTRATION) {

				// worker send a ask for way registration for registration
				// themselves

				wayDispatcher.add(getSender());

				log.info("way dispatcher " + getSender() + " registered");

				// inform for the output ActorRef
				getSender().tell(new MessageOutputRef(outputRef), getSelf());

			} else if (m == MessageClusterRegistration.NEED_MORE_READ) {
				// some workers send NEED_More_READ
				stillneedmoreread = true;

			} else if (m == MessageClusterRegistration.ASK_IF_NEED_MORE_READ) {
				// supervisor tell if all blocks have been completed
				if (stillneedmoreread) {
					// respond to the state
					log.info("workers need more read");
					getSender().tell(MessageClusterRegistration.NEED_MORE_READ,
							getSelf());
				} else {
					log.info("All Blocks read");
					getSender().tell(
							MessageClusterRegistration.ALL_BLOCKS_READ,
							getSelf());
					
					getContext().parent().tell(MessageParsingSystemStatus.END_JOB, getSelf());
					
					// getContext().parent().tell(MessageParsingSystemStatus.TERMINATE, getSelf());
				}

			} else {
				unhandled(message);
			}

		} else if (message instanceof MessageParsingSystemStatus) {

			outputRef.tell(message, getSelf());

			if (message == MessageParsingSystemStatus.INITIALIZE)
				return; // worker are initialized an other way

			if (message == MessageParsingSystemStatus.START_READING_FILE) {
				stillneedmoreread = false;
			}

			log.debug("message to all workers :" + message);

			// send nodes to way construct actors
			for (Iterator iterator = wayDispatcher.iterator(); iterator
					.hasNext();) {
				ActorRef a = (ActorRef) iterator.next();
				a.tell(message, getSelf());
			}

		} else if (message instanceof MessageWayToConstruct) {

			// message way to construct are partitionned

			MessageWayToConstruct mwtc = (MessageWayToConstruct) message;

			// get the partition number for ways
			
			assert wayDispatcher.size() > 0;
			
			int partition = (int) (mwtc.getBlockid() % wayDispatcher.size());

			// inform the right actor
			ActorRef a = wayDispatcher.toArray(new ActorRef[0])[partition];
			// send the way to the proper partition
			a.tell(message, getSelf());

		} else if (message instanceof MessageNodes) {

			outputRef.tell(message, getSelf());

			// send nodes to way construct actors
			for (Iterator iterator = wayDispatcher.iterator(); iterator
					.hasNext();) {
				ActorRef a = (ActorRef) iterator.next();
				a.tell(message, getSelf());
			}

		} else {

			unhandled(message);

		}

	}
}

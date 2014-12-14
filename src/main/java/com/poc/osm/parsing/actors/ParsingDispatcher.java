package com.poc.osm.parsing.actors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.poc.osm.actors.MeasuredActor;
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
public class ParsingDispatcher extends MeasuredActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Set<ActorRef> wayDispatcher = new HashSet<ActorRef>();

	private ActorRef outputRef;

	private boolean stillneedmoreread = false;

	private int startreadingFileCounter = 0;

	public ParsingDispatcher(ActorRef outputRef, ActorRef flowRegulator) {
		assert outputRef != null;
		this.outputRef = outputRef;

	}

	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		if (message instanceof MessageClusterRegistration) {

			MessageClusterRegistration m = (MessageClusterRegistration) message;
			if (m == MessageClusterRegistration.ASK_FOR_WAY_REGISTRATION) {

				// worker send a ask for way registration for registration
				// themselves

				wayDispatcher.add(getSender());

				log.info("way dispatcher " + getSender() + " registered");

				// inform for the output ActorRef
				tell(getSender(), new MessageOutputRef(outputRef), getSelf());

			} else if (m == MessageClusterRegistration.NEED_MORE_READ) {
				// some workers send NEED_More_READ
				stillneedmoreread = true;

			} else if (m == MessageClusterRegistration.ASK_IF_NEED_MORE_READ) {
				// supervisor tell if all blocks have been completed
				if (stillneedmoreread || startreadingFileCounter <= 3) {
					// respond to the state
					log.info("workers need more read");
					tell(getSender(),
							MessageClusterRegistration.NEED_MORE_READ,
							getSelf());
				} else {
					log.info("All Blocks read");
					tell(getSender(),
							MessageClusterRegistration.ALL_BLOCKS_READ,
							getSelf());

					tell(getContext().parent(),
							MessageParsingSystemStatus.END_JOB, getSelf());

				}

			} else {
				unhandled(message);
			}

		} else if (message instanceof MessageParsingSystemStatus) {

			// forward message to output
			tell(outputRef, message, getSelf());

			if (message == MessageParsingSystemStatus.INITIALIZE)
				return; // worker are initialized an other way

			if (message == MessageParsingSystemStatus.START_READING_FILE) {
				startreadingFileCounter++;
				stillneedmoreread = false;
			}

			log.info("message to all workers :" + message);

			// send nodes to way construct actors
			for (Iterator iterator = wayDispatcher.iterator(); iterator
					.hasNext();) {
				ActorRef a = (ActorRef) iterator.next();
				tell(a, message, getSelf());
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
			tell(a, message, getSelf());

		} else if (message instanceof MessageNodes) {

			// only send once the points to the output
			if (startreadingFileCounter <= 1)
				tell(outputRef, message, getSelf());

			// send nodes to way construct actors
			for (Iterator iterator = wayDispatcher.iterator(); iterator
					.hasNext();) {
				ActorRef a = (ActorRef) iterator.next();
				tell(a, message, getSelf());
			}

		} else {

			unhandled(message);

		}

	}
}

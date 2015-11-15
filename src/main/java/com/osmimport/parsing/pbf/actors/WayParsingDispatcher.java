package com.osmimport.parsing.pbf.actors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;

import com.osmimport.actors.MeasuredActor;
import com.osmimport.messages.MessageNodes;
import com.osmimport.messages.MessageRelations;
import com.osmimport.parsing.pbf.actors.messages.MessageClusterRegistration;
import com.osmimport.parsing.pbf.actors.messages.MessageParsingSystemStatus;
import com.osmimport.parsing.pbf.actors.messages.MessagePolygonToConstruct;
import com.osmimport.parsing.pbf.actors.messages.MessageWayToConstruct;

/**
 * Object that register wayconstruct actors, for each OSM entity, it dispatch
 * the message to the registrees
 * 
 * @author pfreydiere
 * 
 */
public class WayParsingDispatcher extends MeasuredActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Set<ActorRef> wayDispatcher = new HashSet<ActorRef>();

	private ActorRef outputRef;

	private ActorRef polygonDispatcher;

	private boolean stillneedmoreread = false;

	/**
	 * number of file read
	 */
	private int startreadingFileCounter = 0;

	
	private int attemptsToAskForNeedMoreRead = MAX_ATTEMPTS_TO_END_PROCESS;

	private static final int MAX_ATTEMPTS_TO_END_PROCESS = 10;


	public WayParsingDispatcher(ActorRef outputRef, ActorRef polygonDispatcher,
			ActorRef flowRegulator) {
		assert outputRef != null;
		this.outputRef = outputRef;
		this.polygonDispatcher = polygonDispatcher;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.osmimport.actors.MeasuredActor#onReceiveMeasured(java.lang.Object)
	 */
	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		if (message instanceof MessageClusterRegistration) {

			MessageClusterRegistration m = (MessageClusterRegistration) message;
			if (m == MessageClusterRegistration.ASK_FOR_WAY_REGISTRATION) {

				// worker send a ask for way registration for registration
				// themselves

				wayDispatcher.add(getSender());

				if (log.isInfoEnabled()) {
					log.info("way dispatcher " + getSender() + " registered");
				}

				

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
					
					// two cases : the workers doesn't have respond yet
					// or nothing to process
					
					if (!stillneedmoreread && attemptsToAskForNeedMoreRead > 0)
					{
						
						getContext()
						.system()
						.scheduler()
						.scheduleOnce(Duration.create(10, TimeUnit.SECONDS), getSelf(),
								MessageClusterRegistration.ASK_IF_NEED_MORE_READ, getContext().dispatcher(), null);
						
					} else 
					{
					
					
						log.info("All Blocks read");
						tell(getSender(),
								MessageClusterRegistration.ALL_BLOCKS_READ,
								getSelf());
	
						tell(getContext().parent(),
								MessageParsingSystemStatus.END_JOB, getSelf());
						
					}

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
				attemptsToAskForNeedMoreRead = MAX_ATTEMPTS_TO_END_PROCESS;
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

		} else if (message instanceof MessageRelations) {

			// only send relations once to the output
			if (startreadingFileCounter <= 1) {
				tell(outputRef, message, getSelf());
			}

		} else if (message instanceof MessagePolygonToConstruct) {

			// only send relations once to the output
			if (startreadingFileCounter <= 1) {
				tell(polygonDispatcher, message, getSelf());
			}

		} else {

			unhandled(message);

		}

	}
}

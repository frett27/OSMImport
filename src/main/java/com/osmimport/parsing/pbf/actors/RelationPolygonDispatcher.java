package com.osmimport.parsing.pbf.actors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.osmimport.actors.MeasuredActor;
import com.osmimport.messages.MessageWay;
import com.osmimport.parsing.pbf.actors.messages.MessageClusterRegistration;
import com.osmimport.parsing.pbf.actors.messages.MessageParsingSystemStatus;
import com.osmimport.parsing.pbf.actors.messages.MessagePolygonToConstruct;

/**
 * construct the polygons
 * 
 * @author pfreydiere
 * 
 */
public class RelationPolygonDispatcher extends MeasuredActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Set<ActorRef> polygonsDispatcher = new HashSet<ActorRef>();

	private ActorRef outputRef;


	public RelationPolygonDispatcher(ActorRef outputRef, ActorRef flowRegulator) {
		assert outputRef != null;
		this.outputRef = outputRef;
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
			if (m == MessageClusterRegistration.ASK_FOR_POLYGONCONSTRUCT_REGISTATION) {

				// worker send a ask for way registration for registration
				// themselves

				polygonsDispatcher.add(getSender());

				if (log.isInfoEnabled()) {
					log.info("polygon dispatcher " + getSender()
							+ " registered");
				}

				

			} else {
				unhandled(message);
			}

		} else if (message instanceof MessageParsingSystemStatus) {

			// forward message to output
			tell(outputRef, message, getSelf());

			if (message == MessageParsingSystemStatus.INITIALIZE)
				return; // worker are initialized an other way


			log.info("message to all workers :" + message);

			// send nodes to way construct actors
			for (Iterator iterator = polygonsDispatcher.iterator(); iterator
					.hasNext();) {
				ActorRef a = (ActorRef) iterator.next();
				tell(a, message, getSelf());
			}

		} else if (message instanceof MessagePolygonToConstruct) {

			// message way to construct are partitionned

			MessagePolygonToConstruct mwtc = (MessagePolygonToConstruct) message;

			// get the partition number for ways

			assert polygonsDispatcher.size() > 0;

			int partition = (int) (mwtc.getBlockid() % polygonsDispatcher
					.size());

			// inform the right actor
			ActorRef a = polygonsDispatcher.toArray(new ActorRef[0])[partition];
			// send the way to the proper partition
			tell(a, message, getSelf());

		} else if (message instanceof MessageWay) {

			// send nodes to way construct actors
			for (Iterator iterator = polygonsDispatcher.iterator(); iterator
					.hasNext();) {
				ActorRef a = (ActorRef) iterator.next();
				tell(a, message, getSelf());
			}

		} else {

			unhandled(message);

		}

	}

}

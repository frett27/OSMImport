package com.poc.osm.parsing.actors;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.poc.osm.messages.MessageNodes;
import com.poc.osm.messages.MessageWay;
import com.poc.osm.parsing.actors.messages.MessageClusterRegistration;
import com.poc.osm.parsing.actors.messages.MessageParsingSystemStatus;

/**
 * Consolidate the parsing results, and send the result to a node
 * 
 * @author use
 * 
 */
public class ParsingOutput extends UntypedActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private ActorSelection resultSendingActors;

	public ParsingOutput(String resultPath) {
		resultSendingActors = getContext().actorSelection(resultPath);
	}

	private enum State {
		READING_POINTS, POINTS_HAVE_BEEN_READ
	}

	private State currentState = State.READING_POINTS;

	/*
	 * (non-Javadoc)
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	@Override
	public void onReceive(Object message) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("message received :" + message);
		}

		if (message instanceof MessageNodes) {

			if (currentState == State.READING_POINTS) {
				// first read, we send the points to the output for handling
				resultSendingActors.tell(message, getSelf());
			}
			// else points have already been sent, don't resend them ...

		} else if (message instanceof MessageWay) {
			
			resultSendingActors.tell(message, getSelf());

		} else if (message instanceof MessageParsingSystemStatus) {
			
			MessageParsingSystemStatus s = (MessageParsingSystemStatus) message;

			if (s == MessageParsingSystemStatus.END_READING_FILE) {
				currentState = State.POINTS_HAVE_BEEN_READ;
				log.info("All Points have been processed");
			}
			
			

		}  else {
			unhandled(message);
		}

	}

}

package com.poc.osm.parsing.actors;

import java.util.concurrent.TimeUnit;

import scala.collection.Iterator;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.poc.osm.actors.MeasuredActor;
import com.poc.osm.parsing.actors.messages.MessageParsingSystemStatus;
import com.poc.osm.parsing.actors.messages.MessageReadFile;

public class ParsingSubSystemActor extends MeasuredActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private ActorRef reading;

	private ActorRef dispatcher;

	public ParsingSubSystemActor(ActorRef flowRegulator, ActorRef output) {

		// setTerminal = true;

		dispatcher = getContext().actorOf(
				Props.create(ParsingDispatcher.class, output, flowRegulator),
				"dispatcher");

		// reading actor
		reading = getContext().actorOf(
				Props.create(ReadingActor.class, dispatcher, flowRegulator),
				"reading");

		// init the worker

		for (int i = 0; i < 5; i++) {
			ActorRef worker = getContext().actorOf(
					Props.create(WayConstructorActor.class, dispatcher),
					"worker" + i);
			tell(worker, MessageParsingSystemStatus.INITIALIZE,
					ActorRef.noSender());

		}

	}

	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		if (message instanceof MessageParsingSystemStatus) {

			Iterator<ActorRef> it = getContext().children().iterator();
			for (ActorRef a = it.next(); it.hasNext(); a = it.next()) {
				tell(a, message, getSelf());
			}

			if (MessageParsingSystemStatus.END_JOB == message) {
				log.info("terminate the process");

				// wait for all remaining events to be handled
				getContext()
						.system()
						.scheduler()
						.scheduleOnce(
								new FiniteDuration(1, TimeUnit.MINUTES),
								new Runnable() {

									@Override
									public void run() {
										System.out.println("ShutDown the process");
										getContext().system().shutdown();
									}
								}, getContext().system().dispatcher());

			}

		} else if (message instanceof MessageReadFile) {

			tell(reading, message, getSelf());

		}

		else {
			unhandled(message);
		}
	}
}

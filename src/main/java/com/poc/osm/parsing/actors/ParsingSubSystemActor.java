package com.poc.osm.parsing.actors;

import scala.collection.Iterator;
import scala.collection.immutable.Iterable;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.poc.osm.parsing.actors.messages.MessageParsingSystemStatus;
import com.poc.osm.parsing.actors.messages.MessageReadFile;

public class ParsingSubSystemActor extends UntypedActor {

	private ActorRef reading;

	private ActorRef dispatcher;

	public ParsingSubSystemActor(ActorRef flowRegulator) {

		// parsing result output actor, will redirect the parsing in result
		ActorRef parsingOutput = getContext().actorOf(
				Props.create(ParsingOutput.class, "/user/result"));

		dispatcher = getContext().actorOf(
				Props.create(ParsingDispatcher.class, parsingOutput),
				"dispatcher");

		// reading actor
		reading = getContext().actorOf(
				Props.create(ReadingActor.class, dispatcher, flowRegulator),
				"reading");

		// init the worker
		ActorRef worker1 = getContext().actorOf(
				Props.create(WayConstructorActor.class,dispatcher), "worker1");
		worker1.tell(MessageParsingSystemStatus.INITIALIZE, ActorRef.noSender());

		ActorRef worker2 = getContext().actorOf(
				Props.create(WayConstructorActor.class,dispatcher), "worker2");
		worker2.tell(MessageParsingSystemStatus.INITIALIZE, ActorRef.noSender());

	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof MessageParsingSystemStatus) {

			Iterator<ActorRef> it = getContext().children().iterator();
			for (ActorRef a = it.next(); it.hasNext(); a = it.next()) {
				a.tell(message, getSelf());
			}
			
		} else if (message instanceof MessageReadFile) {

			reading.tell(message, getSelf());

		}

		else {
			unhandled(message);
		}
	}

}

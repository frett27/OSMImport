package com.poc.osm.output.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.poc.osm.output.actors.messages.CompiledFieldsMessage;
import com.poc.osm.regulation.MessageRegulation;

public class CompiledFieldsMessageCounter extends UntypedActor {
	
	private ActorRef next;
	private ActorRef flowRegulator;

	public CompiledFieldsMessageCounter(ActorRef next, ActorRef flowRegulator) {
		this.next = next;
		this.flowRegulator = flowRegulator;
	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof CompiledFieldsMessage) {
			flowRegulator.tell(new MessageRegulation(1), getSelf());
			next.tell(message, getSelf());
		} else {
			unhandled(message);
		}

	}

}

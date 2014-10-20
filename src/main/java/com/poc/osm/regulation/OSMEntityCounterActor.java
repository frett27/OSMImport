package com.poc.osm.regulation;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.poc.osm.model.OSMEntity;

/**
 * this actor send a MessageRegulation for each Entity to the flowRegulator
 * 
 * @author pfreydiere
 * 
 */
public class OSMEntityCounterActor extends UntypedActor {

	private ActorRef next;
	private ActorRef flowRegulator;

	public OSMEntityCounterActor(ActorRef next, ActorRef flowRegulator) {
		this.next = next;
		this.flowRegulator = flowRegulator;
	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof OSMEntity) {
			flowRegulator.tell(new MessageRegulation(1), getSelf());
			next.tell(message, getSelf());
		} else {
			unhandled(message);
		}

	}

}

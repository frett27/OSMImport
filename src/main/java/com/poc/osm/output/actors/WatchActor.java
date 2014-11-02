package com.poc.osm.output.actors;

import java.util.List;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class WatchActor extends UntypedActor {

	private ActorRef output;

	private List<ActorRef> watchActorList;

	public WatchActor(ActorRef output, List<ActorRef> watchActorList) {
		this.output = output;
		this.watchActorList = watchActorList;

	}

	@Override
	public void preStart() throws Exception {
		super.preStart();

		if (watchActorList != null) {
			for (ActorRef r : watchActorList) {
				getContext().watch(r);
			}
		}

	}

	@Override
	public void onReceive(Object message) throws Exception {
		output.tell(message, getSelf());
	}

}

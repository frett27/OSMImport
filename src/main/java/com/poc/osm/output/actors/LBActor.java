package com.poc.osm.output.actors;

import java.util.List;

import akka.actor.ActorRef;

import com.poc.osm.actors.MeasuredActor;

public class LBActor extends MeasuredActor {

	private List<ActorRef> actors;

	private int current = 0;

	public LBActor(List<ActorRef> lbactors) {
		assert lbactors != null;
		this.actors = lbactors;
	}

	@Override
	public void onReceiveMeasured(Object message) throws Exception {
		current = (current + 1) % actors.size();
		ActorRef a = actors.get(current);
		tell(a, message, getSelf());
	}

}

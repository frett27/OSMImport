package com.osmimport.measured;

import akka.actor.ActorRef;
import akka.actor.Props;

import com.osmimport.actors.MeasuredActor;

public class TestMContainer extends MeasuredActor {

	private ActorRef a3ref;

	public TestMContainer() {

		setTerminal = true;

		ActorRef a1ref = getContext().actorOf(
				Props.create(TestMeasureActor.class, null, 1000L), "a1");

		ActorRef a2ref = getContext().actorOf(
				Props.create(TestMeasureActor.class, a1ref, 1000L), "a2");

		a3ref = getContext().actorOf(
				Props.create(TestMeasureActor.class, a2ref, 1000L), "a3");

	}

	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		for (int i = 0; i < 10; i++) {
			tell(a3ref, message, getSelf());
		}
	}

}

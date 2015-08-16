package com.osmimport.measured;

import akka.actor.ActorRef;

import com.osmimport.actors.MeasuredActor;

public class TestMeasureActor extends MeasuredActor {

	private ActorRef next;
	private long delay;

	public TestMeasureActor(ActorRef next, long delay) {
		this.next = next;
		this.delay = delay;
	}

	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		Thread.sleep(delay);

		if (next != null) {
			for (int i = 0; i < 10; i++) {
				tell(next, message, getSelf());
			}
		}

	}

}

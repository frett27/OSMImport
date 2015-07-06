package com.poc.osm.output.actors;

import java.util.List;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.poc.osm.actors.MeasuredActor;

/**
 * Load balancing actor,
 * 
 * @author pfreydiere
 * 
 */
public class LBActor extends MeasuredActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private List<ActorRef> actors;

	/**
	 * current actor that received a message
	 */
	private int current = 0;

	/**
	 * message counter, for reporting
	 */
	private long count = 0;

	/**
	 * constructor, taking the load balanced childs
	 * 
	 * @param lbactors
	 */
	public LBActor(List<ActorRef> lbactors) {
		assert lbactors != null;
		this.actors = lbactors;
	}

	@Override
	public void onReceiveMeasured(Object message) throws Exception {
		current = (current + 1) % actors.size();

		count++;
		if (count % 1000000 == 0) {
			log.info("" + count + " elements handled");
		}
		
		ActorRef a = actors.get(current);
		tell(a, message, getSelf());
	}

}

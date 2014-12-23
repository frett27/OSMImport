package com.poc.osm.output.actors;

import java.util.List;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.poc.osm.actors.MeasuredActor;
import com.poc.osm.messages.MessageNodes;
import com.poc.osm.messages.MessageWay;
import com.poc.osm.model.OSMEntity;
import com.poc.osm.output.Filter;
import com.poc.osm.output.Transform;

public class StreamProcessingActor extends MeasuredActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Filter filter;

	private Transform transform;

	private List<ActorRef> nextRefs;

	private List<ActorRef> others;
	
	private long handledOuput = 0;

	public StreamProcessingActor(Filter filter, Transform transform,
			List<ActorRef> nextRefs, List<ActorRef> others) throws Exception {

		log.info("create Actor " + getSelf() + " with nexts :" + nextRefs
				+ " , others :" + others);

		this.filter = filter;
		this.transform = transform;
		this.nextRefs = nextRefs;
		this.others = others;
	}

	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		if (message instanceof OSMEntity) {
			handleMessage(message);
		} else if (message instanceof MessageNodes) {

			MessageNodes mn = (MessageNodes) message;
			for (OSMEntity e : mn.getNodes()) {
				handleMessage(e);
			}

			return;
		} else if (message instanceof MessageWay) {
			MessageWay mw = (MessageWay) message;
			handleMessage(mw.getEntity());
		} else {
			unhandled(message);
		}

	}

	private void handleMessage(Object message) {
	
		if (!(message instanceof OSMEntity)) {
			return;
		}
		
		OSMEntity e = (OSMEntity) message;
		
		handledOuput ++;
		if (handledOuput % 1000000 == 0)
			log.info("" + handledOuput + " entity handled");

		try {
			if (filter != null) {
				if (!filter.filter(e)) {

					// log.info("filtered message");

					if (others != null) {
						// call others
						for (ActorRef r : others) {
							tell(r, e, getSelf());
						}

					}
					return;
				}
			}

			if (transform != null) {
				e = transform.transform(e);
			}

			for (ActorRef r : nextRefs) {
				tell(r, e, getSelf());
				// log.info("send message to " + r);
			}

		} catch (Exception ex) {
			log.error(ex,"error in processing the message :" + ex.getMessage() + " on object :" + e);
		}
	}

}



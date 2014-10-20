package com.poc.osm.output.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.poc.osm.messages.MessageNodes;
import com.poc.osm.messages.MessageWay;
import com.poc.osm.model.OSMEntity;
import com.poc.osm.output.Filter;
import com.poc.osm.output.Transform;

public class StreamProcessingActor extends UntypedActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Filter filter;

	private Transform transform;

	private ActorRef nextActor;

	public StreamProcessingActor(Filter filter, Transform transform,
			ActorRef nextRef) throws Exception {

		this.filter = filter;
		this.transform = transform;
		this.nextActor = nextRef;

	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof MessageNodes) {

			MessageNodes mn = (MessageNodes) message;
			for (OSMEntity e : mn.getNodes()) {
				onReceive(e);
			}

			return;
		} else if (message instanceof MessageWay) {
			MessageWay mw = (MessageWay) message;
			onReceive(mw.getEntity());
		}

		if (!(message instanceof OSMEntity)) {
			return;
		}
		OSMEntity e = (OSMEntity) message;

		try {
			if (filter != null) {
				if (!filter.filter(e)) {
					return;
				}
			}

			if (transform != null) {
				e = transform.transform(e);
			}

			nextActor.tell(e, getSelf());
		} catch (Exception ex) {
			log.error("error in processing the message :" + ex.getMessage(), ex);
		}

	}

}

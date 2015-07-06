package com.poc.osm.output.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.poc.osm.actors.MeasuredActor;
import com.poc.osm.messages.MessageNodes;
import com.poc.osm.messages.MessageWay;
import com.poc.osm.model.OSMAttributedEntity;
import com.poc.osm.model.OSMRelation;
import com.poc.osm.output.Filter;
import com.poc.osm.output.Transform;
import com.poc.osm.parsing.actors.messages.MessageRelations;

/**
 * actor supporting filter and transform operation for an OSMEntity
 * 
 * @author pfreydiere
 * 
 */
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

		if (message instanceof OSMAttributedEntity || message instanceof List) {

			handleMessage(message);

		} else if (message instanceof MessageNodes) {

			MessageNodes mn = (MessageNodes) message;
			for (OSMAttributedEntity e : mn.getNodes()) {
				handleSingleMessage(e);
			}
			return;

		} else if (message instanceof MessageWay) {

			MessageWay mw = (MessageWay) message;

			handleSingleMessage(mw.getEntity());

		} else if (message instanceof MessageRelations) {

			MessageRelations mr = (MessageRelations) message;
			List<OSMRelation> rels = mr.getRelations();
			if (rels != null) {
				for (OSMRelation r : rels) {
					if (r != null) {
						handleSingleMessage(r);
					}
				}
			}

		} else {
			unhandled(message);
		}

	}

	private void handleSingleMessage(OSMAttributedEntity message) {

		if (!(message instanceof OSMAttributedEntity)) {
			return;
		}

		OSMAttributedEntity e = (OSMAttributedEntity) message;

		handledOuput++;
		if (handledOuput % 100000 == 0) {
			log.info("" + handledOuput + " entity handled by actor "
					+ getSelf().path());
		}

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

			List<OSMAttributedEntity> l = null;

			if (transform != null) {
				l = transform.transform(e);
			} else {
				l = new ArrayList<OSMAttributedEntity>();
				l.add(e);
			}

			if (l != null) {
				l = Arrays.asList(l.toArray(new OSMAttributedEntity[l.size()]));
			}

			if (l != null) {

				assert l instanceof List;
				for (OSMAttributedEntity entity : l) {
					if (entity != null) {
						for (ActorRef r : nextRefs) {
							tell(r, entity, getSelf());
							// log.info("send message to " + r);
						}
					}
				}
			} else {

				log.warning("transform returned null for transform "
						+ transform);
			}

		} catch (Exception ex) {
			log.error(ex, "error in processing the message :" + ex.getMessage()
					+ " on object :" + e);
		}
	}

	private void handleMessage(Object message) {

		if (message instanceof List) {
			List<OSMAttributedEntity> l = (List<OSMAttributedEntity>) message;
			for (OSMAttributedEntity e : l) {
				handleSingleMessage(e);
			}

		} else if (message instanceof OSMAttributedEntity) {
			handleSingleMessage((OSMAttributedEntity) message);
		} else {
			unhandled(message);
		}

	}

}

package com.osmimport.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.osmimport.messages.MessageNodes;
import com.osmimport.messages.MessageRelations;
import com.osmimport.messages.MessageWay;
import com.osmimport.model.OSMAttributedEntity;
import com.osmimport.model.OSMRelation;
import com.osmimport.output.Filter;
import com.osmimport.output.Transform;

/**
 * actor processing a stream of datas
 * it support filter and transform operation
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

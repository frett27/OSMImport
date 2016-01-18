package com.osmimport.parsing.pbf.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.osmimport.parsing.actors.AbstractParsingSubSystem;
import com.osmimport.tools.polygoncreator.IInvalidPolygonConstructionFeedBack;

public class PbfParsingSubSystemActor extends AbstractParsingSubSystem {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	public PbfParsingSubSystemActor(
			ActorRef flowRegulator,
			ActorRef output,
			Long maxWysToConstruct,
			IInvalidPolygonConstructionFeedBack invalidPolygonConstructionFeedBack) {
		super(flowRegulator, output, maxWysToConstruct,
				invalidPolygonConstructionFeedBack);

	}

	@Override
	protected ActorRef createReadingActor() {
		return getContext().actorOf(
				Props.create(ReadingSubSystemActor.class, dispatcher,
						flowRegulator), "reading");
	}

}

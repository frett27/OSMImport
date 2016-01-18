package com.osmimport.parsing.xml;

import akka.actor.ActorRef;
import akka.actor.Props;

import com.osmimport.parsing.actors.AbstractParsingSubSystem;
import com.osmimport.parsing.pbf.actors.ReadingSubSystemActor;
import com.osmimport.tools.polygoncreator.IInvalidPolygonConstructionFeedBack;

public class XMLParsingSubSystemActor extends AbstractParsingSubSystem {

	public XMLParsingSubSystemActor(
			ActorRef flowRegulator,
			ActorRef output,
			Long maxWysToConstruct,
			IInvalidPolygonConstructionFeedBack invalidPolygonConstructionFeedBack) {
		super(flowRegulator, output, maxWysToConstruct, invalidPolygonConstructionFeedBack);
	}

	@Override
	protected ActorRef createReadingActor() {

		return getContext().actorOf(
				Props.create(XMLReadingActor.class, dispatcher, flowRegulator),
				"reading");

	}

}

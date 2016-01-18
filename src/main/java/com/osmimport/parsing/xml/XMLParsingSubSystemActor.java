package com.osmimport.parsing.xml;

import akka.actor.ActorRef;
import akka.actor.Props;

import com.osmimport.parsing.actors.AbstractParsingSubSystem;
import com.osmimport.parsing.actors.ParsingLevel;
import com.osmimport.tools.polygoncreator.IInvalidPolygonConstructionFeedBack;

public class XMLParsingSubSystemActor extends AbstractParsingSubSystem {

	public XMLParsingSubSystemActor(
			ActorRef flowRegulator,
			ActorRef output,
			Long maxWysToConstruct,
			IInvalidPolygonConstructionFeedBack invalidPolygonConstructionFeedBack,
			ParsingLevel parsingLevel) {
		super(flowRegulator, output, maxWysToConstruct,
				invalidPolygonConstructionFeedBack, parsingLevel);
	}

	@Override
	protected ActorRef createReadingActor() {

		return getContext().actorOf(
				Props.create(XMLReadingActor.class, dispatcher, flowRegulator),
				"reading");

	}

}

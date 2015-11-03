package com.osmimport.parsing.xml;

import akka.actor.ActorRef;
import akka.actor.Props;

import com.osmimport.parsing.actors.AbstractParsingSubSystem;
import com.osmimport.parsing.pbf.actors.ReadingSubSystemActor;

public class XMLParsingSubSystemActor extends AbstractParsingSubSystem {

	
	
	public XMLParsingSubSystemActor(ActorRef flowRegulator, ActorRef output, Long maxWysToConstruct) {
		super(flowRegulator, output, maxWysToConstruct);
	}

	@Override
	protected ActorRef createReadingActor() {
		
		return getContext().actorOf(
				Props.create(XMLReadingActor.class, dispatcher,
						flowRegulator), "reading");
		
	}

}

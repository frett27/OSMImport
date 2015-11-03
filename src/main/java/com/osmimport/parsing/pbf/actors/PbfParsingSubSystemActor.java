package com.osmimport.parsing.pbf.actors;

import java.util.concurrent.TimeUnit;

import scala.collection.Iterator;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.osmimport.actors.MeasuredActor;
import com.osmimport.parsing.actors.AbstractParsingSubSystem;
import com.osmimport.parsing.pbf.actors.messages.MessageParsingSystemStatus;
import com.osmimport.parsing.pbf.actors.messages.MessageReadFile;

public class PbfParsingSubSystemActor extends AbstractParsingSubSystem {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);


	public PbfParsingSubSystemActor(ActorRef flowRegulator, ActorRef output, Long maxWysToConstruct) {
		super(flowRegulator, output, maxWysToConstruct);

	}
	
	@Override
	protected ActorRef createReadingActor(){
		return getContext().actorOf(
				Props.create(ReadingSubSystemActor.class, dispatcher,
						flowRegulator), "reading");
	}


	
}

package com.poc.osm.parsing.actors.newparse;

import java.util.ArrayList;
import java.util.List;

import com.poc.osm.parsing.actors.ParsingSystemActorsConstants;
import com.poc.osm.regulation.FlowRegulator;
import com.poc.osm.regulation.MessageRegulation;

import crosby.binary.Fileformat.Blob;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

public class OSMBlockParsingActor extends UntypedActor {

	private Router generatorRouter;

	private ActorRef flowRegulator;

	public OSMBlockParsingActor(Router digger, ActorRef flowRegulator) {
		this.flowRegulator = flowRegulator;

		List<Routee> routees = new ArrayList<Routee>();
		for (int i = 0; i < 5; i++) {
			ActorRef r = getContext().actorOf(
					Props.create(OSMParser.class, digger));
			getContext().watch(r);
			routees.add(new ActorRefRoutee(r));
		}

		generatorRouter = new Router(new RoundRobinRoutingLogic(), routees);

	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof Blob) {
			flowRegulator.tell(new MessageRegulation(
					ParsingSystemActorsConstants.RECORDS_BLOC_EQUIVALENCE),
					getSelf());
		}
		
		generatorRouter.route(message, getSelf());

	}

}

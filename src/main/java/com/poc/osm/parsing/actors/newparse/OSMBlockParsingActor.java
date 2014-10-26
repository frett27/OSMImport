package com.poc.osm.parsing.actors.newparse;

import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

public class OSMBlockParsingActor extends UntypedActor {

	private Router generatorRouter;

	public OSMBlockParsingActor(Router digger) {

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
		generatorRouter.route(message, getSelf());
	}

}

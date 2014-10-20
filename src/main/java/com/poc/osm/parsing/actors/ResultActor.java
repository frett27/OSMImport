package com.poc.osm.parsing.actors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

/**
 * this actor, forward to a list of actor the messages passed
 * 
 * @author pfreydiere
 * 
 */
public class ResultActor extends UntypedActor {

	private Router router;

	/**
	 * construct the result actor, with a list of the routees
	 * 
	 * @param routee
	 */
	public ResultActor(Collection<ActorRef> routee) {

		assert routee != null;
		assert routee.size() > 0;
		
		List<Routee> routees = new ArrayList<Routee>();
		for (Iterator iterator = routee.iterator(); iterator.hasNext();) {
			ActorRef actorRef = (ActorRef) iterator.next();
			routees.add(new ActorRefRoutee(actorRef));
		}

		router = new Router(new BroadcastRoutingLogic(), routees);

	}

	@Override
	public void onReceive(Object message) throws Exception {
		router.route(message, getSender());
	}

}

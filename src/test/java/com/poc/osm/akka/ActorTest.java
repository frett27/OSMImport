package com.poc.osm.akka;

import org.eclipse.persistence.internal.sessions.remote.SequencingFunctionCall.GetNextValue;

import scala.Option;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;

public class ActorTest extends UntypedActor {

	@Override
	public void onReceive(Object message) throws Exception {

		System.out.println("message :" + message);

		if ("pp".equals(message)) {
			System.out.println(getContext().parent().path());
			getContext().system().shutdown();
			return;
		}

		unhandled(message);

	}

	@Override
	public void preStart() throws Exception {
		super.preStart();
		System.out.println("prestart " + getSelf().path());
	}

	@Override
	public void postStop() throws Exception {
		System.out.println("stop " + getSelf().path());
		super.postStop();
	}

}

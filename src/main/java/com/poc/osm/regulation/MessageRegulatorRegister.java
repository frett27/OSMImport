package com.poc.osm.regulation;

import akka.actor.ActorRef;

/**
 * message for registering an actor to the Regulator System
 * @author use
 *
 */
public class MessageRegulatorRegister {
	
	private ActorRef actor;
	
	public MessageRegulatorRegister(ActorRef a)
	{
		this.actor = a;
	}

	public ActorRef getActor() {
		return actor;
	}
	
}

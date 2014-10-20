package com.poc.osm.parsing.actors.messages;

import java.io.Serializable;

import akka.actor.ActorRef;

/**
 * this message dispatch the output actor
 * 
 * @author pfreydiere
 * 
 */
public class MessageOutputRef implements Serializable {

	private ActorRef output;

	public MessageOutputRef(ActorRef output) {
		this.output = output;
	}

	public ActorRef getOutput() {
		return output;
	}

}

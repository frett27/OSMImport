package com.osmimport.actors;

import akka.dispatch.ControlMessage;

/**
 * control message for message measure, and regulation
 * 
 * @author pfreydiere
 * 
 */
public class AskMetrics implements ControlMessage {

	/**
	 * special message send by the internal timer
	 */
	public static final AskMetrics CHILDREN_ASK = new AskMetrics(-1);

	private long correlationId;

	/**
	 * ask for metrics, with a correlation id
	 * 
	 * @param correlationId
	 */
	public AskMetrics(long correlationId) {
		this.correlationId = correlationId;
	}

	public long getCorrelationId() {
		return correlationId;
	}

}

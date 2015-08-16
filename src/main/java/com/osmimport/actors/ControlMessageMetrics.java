package com.osmimport.actors;

import java.io.Serializable;

import akka.dispatch.ControlMessage;

public class ControlMessageMetrics implements ControlMessage, Serializable {

	private MessageMetrics messageMetrics = new MessageMetrics();

	private long correlated;

	public ControlMessageMetrics(long correlated) {
		this.correlated = correlated;
	}

	public ControlMessageMetrics(long correlated, MessageMetrics messageMetrics) {
		this(correlated);
		if (messageMetrics == null)
			return;
		this.messageMetrics = new MessageMetrics(messageMetrics);
	}

	public MessageMetrics getMessageMetrics() {
		return messageMetrics;
	}

	public long getCorrelated() {
		return correlated;
	}

}

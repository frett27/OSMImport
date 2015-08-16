package com.osmimport.actors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * internal object for collecting metrics
 * 
 * @author use
 * 
 */
public class CollectingTransaction {

	private Set<String> allChildren = new HashSet<String>();

	private MessageMetrics allMetrics = new MessageMetrics();

	private long transaction;

	CollectingTransaction(long transaction) {
		this.transaction = transaction;
	}

	public void registerPath(String actorPath) {
		assert actorPath != null;
		assert !actorPath.isEmpty();
		allChildren.add(actorPath);
	}

	public long getTransaction() {
		return transaction;
	}

	public void collect(String actorPath, MessageMetrics messageMetrics) {
		assert allChildren.contains(actorPath);
		allMetrics.mergeWith(messageMetrics);
		allChildren.remove(actorPath);

	}

	public boolean areAllCollected() {
		return allChildren.isEmpty();
	}

	public MessageMetrics getMergedMetrics() {
		assert areAllCollected();
		return allMetrics;
	}

}

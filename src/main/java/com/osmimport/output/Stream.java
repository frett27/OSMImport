package com.osmimport.output;

import akka.actor.ActorRef;

/**
 * Stream for handling entity modifications
 * 
 * @author pfreydiere
 * 
 */
public class Stream extends ModelElement {

	public Stream() {
	}

	/**
	 * Parent stream for the objects
	 */
	public Stream parentStream;

	/**
	 * other stream for skipped filtered objects
	 */
	public Stream other;

	/**
	 * is this stream other ?
	 */
	public boolean isOther = false;

	/**
	 * Stream filter
	 */
	public Filter filter;

	/**
	 * Stream tranform
	 */
	public Transform transform;

	/**
	 * internal actor reference (when the script is compiled)
	 */
	public ActorRef _actor;

	/**
	 * Stream label
	 */
	public String label = "Label " + System.currentTimeMillis();

	public String actorName = null;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Stream(").append(label);
		if (filter != null) {
			sb.append(",filter:").append(filter.getClass().getSimpleName());
		}
		if (transform != null) {
			sb.append(",transform:").append(
					transform.getClass().getSimpleName());
		}
		sb.append(")");

		return sb.toString();
	}

	/**
	 * return a key for the actor
	 * 
	 * @return
	 */
	public String getKey() {
		return actorName != null ? actorName : "S" + hashCode() + "_" + label;
	}

}

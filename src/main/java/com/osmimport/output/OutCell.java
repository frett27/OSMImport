package com.osmimport.output;

import java.util.Arrays;

import akka.actor.ActorRef;

/**
 * Cell definition for the output
 * 
 * @author pfreydiere
 *
 */
public class OutCell extends ModelElement {

	/**
	 * associated upstreams
	 */
	public Stream[] streams;

	/**
	 * reference to the output sink
	 */
	public OutSink sink;

	/**
	 * output table name or featureclass
	 */
	public String tablename;
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OutCell / Streams :").append(Arrays.asList(streams)).append(" push in :")
				.append(sink).append('(').append(tablename).append(')');
		return sb.toString();
	}

}

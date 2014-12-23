package com.poc.osm.output;

import java.util.Arrays;

import akka.actor.ActorRef;

/**
 * Cell definition for the output
 * 
 * @author pfreydiere
 *
 */
public class OutCell extends ModelElement {

	public Stream[] streams;

	public GDBReference gdb;

	public String tablename;
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OutCell / Streams :").append(Arrays.asList(streams)).append(" push in :")
				.append(gdb).append('(').append(tablename).append(')');
		return sb.toString();
	}

}

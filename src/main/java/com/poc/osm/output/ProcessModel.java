package com.poc.osm.output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Model of the processing chain
 * 
 * @author pfreydiere
 * 
 */
public class ProcessModel {

	private static final String SEPARATOR = "\n  ";
	
	public Stream mainStream;

	public Collection<Stream> streams = new ArrayList<Stream>();

	public Collection<OutCell> outs = new ArrayList<OutCell>();

	public void addStream(Stream s) {
		assert s != null;
		streams.add(s);
	}

	public void addOut(OutCell r) {
		assert r != null;
		outs.add(r);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ProcessModel :").append(SEPARATOR)
				.append("streams :").append(streams).append(SEPARATOR)
				.append("outs :").append(outs);
		return sb.toString();
	}

}

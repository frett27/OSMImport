package com.poc.osm.parsing.actors.messages;

import java.io.Serializable;
import java.util.List;

import com.poc.osm.parsing.model.WayToConstruct;

/**
 * 
 * tell the ways to construct, with an associated blockid
 * 
 * @author pfreydiere
 *
 */
public class MessageWayToConstruct implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5652561315312411333L;

	private List<WayToConstruct> waysToConstruct;

	private long blockid;

	public MessageWayToConstruct(long blockid,
			List<WayToConstruct> waysToConstruct) {
		this.blockid = blockid;
		this.waysToConstruct = waysToConstruct;
	}

	public List<WayToConstruct> getWaysToConstruct() {
		return waysToConstruct;
	}

	public long getBlockid() {
		return blockid;
	}

}

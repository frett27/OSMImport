package com.poc.osm.messages;

import java.io.Serializable;
import java.util.List;

import com.poc.osm.model.OSMEntity;

/**
 * Message handling 
 * @author pfreydiere
 *
 */
public class MessageNodes implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 521510126270071270L;
	
	
	private List<OSMEntity> nodes;

	public MessageNodes(List<OSMEntity> nodes) {
		this.nodes = nodes;
	}

	public List<OSMEntity> getNodes() {
		return nodes;
	}

}

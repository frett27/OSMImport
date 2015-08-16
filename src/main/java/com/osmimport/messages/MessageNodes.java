package com.osmimport.messages;

import java.io.Serializable;
import java.util.List;

import com.osmimport.model.OSMEntity;

/**
 * Message contaning a list of entity nodes
 * 
 * @author pfreydiere
 * 
 */
public class MessageNodes implements Serializable {

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

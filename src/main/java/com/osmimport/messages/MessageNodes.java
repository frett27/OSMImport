package com.osmimport.messages;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.osmimport.model.OSMEntity;
import com.osmimport.model.OSMEntityPoint;

/**
 * Message contaning a list of entity nodes
 * 
 * @author pfreydiere
 * 
 */
public class MessageNodes implements Serializable, ParsingObjects {

	/**
	 * 
	 */
	private static final long serialVersionUID = 521510126270071270L;

	private List<OSMEntityPoint> nodes;

	public MessageNodes(List<OSMEntityPoint> nodes) {
		this.nodes = nodes;
	}

	public List<OSMEntityPoint> getNodes() {
		return nodes;
	}
	
	

}

package com.osmimport.parsing.pbf.actors.messages;

import java.io.Serializable;
import java.util.List;

import com.osmimport.messages.ParsingObjects;
import com.osmimport.parsing.model.PolygonToConstruct;
import com.osmimport.parsing.model.WayToConstruct;

public class MessagePolygonToConstruct implements Serializable, ParsingObjects {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1451932029152932627L;

	private List<PolygonToConstruct> polygonToConstruct;

	private long blockid;

	public MessagePolygonToConstruct(long blockid,
			List<PolygonToConstruct> polygonToConstruct) {
		this.blockid = blockid;
		this.polygonToConstruct = polygonToConstruct;
	}

	public List<PolygonToConstruct> getPolygonsToConstruct() {
		return polygonToConstruct;
	}

	public long getBlockid() {
		return blockid;
	}

}

package com.poc.osm.messages;

import java.io.Serializable;

import com.poc.osm.model.OSMEntity;

/**
 * Message containing a way
 * 
 * @author use
 * 
 */
public class MessageWay implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6643032739961435923L;

	private OSMEntity entity;

	public MessageWay(OSMEntity entity) {
		this.entity = entity;
	}

	public OSMEntity getEntity() {
		return entity;
	}
}

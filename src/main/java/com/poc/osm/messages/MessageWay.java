package com.poc.osm.messages;

import java.io.Serializable;

import com.poc.osm.model.OSMEntity;

public class MessageWay implements Serializable {

	private OSMEntity entity;
	
	public MessageWay(OSMEntity entity)
	{
		this.entity = entity;
	}
	
	public OSMEntity getEntity() {
		return entity;
	}
}

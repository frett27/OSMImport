package com.poc.osm.parsing.actors.messages;

import java.io.Serializable;
import java.util.List;

import com.poc.osm.model.OSMRelation;

public class MessageRelations implements Serializable {

	private List<OSMRelation> relations;

	public MessageRelations(List<OSMRelation> relations) {
		this.relations = relations;
	}

	public List<OSMRelation> getRelations() {
		return relations;
	}
	
}

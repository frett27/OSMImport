package com.poc.osm.output;

import java.util.List;

import com.poc.osm.model.OSMAttributedEntity;

public abstract class Transform {

	public abstract List<OSMAttributedEntity> transform(OSMAttributedEntity e);

}

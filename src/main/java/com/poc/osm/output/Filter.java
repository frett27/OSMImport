package com.poc.osm.output;

import com.poc.osm.model.OSMEntity;

public abstract class Filter {

	public abstract boolean filter(OSMEntity e);

}

package com.poc.osm.output;

import com.poc.osm.model.OSMEntity;

/**
 * definition of a filter
 * 
 * @author pfreydiere
 * 
 */
public abstract class Filter {

	public abstract boolean filter(OSMEntity e);

}

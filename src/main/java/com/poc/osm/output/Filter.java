package com.poc.osm.output;

import com.poc.osm.model.OSMAttributedEntity;

/**
 * definition of a filter
 * 
 * @author pfreydiere
 * 
 */
public abstract class Filter {

	public abstract boolean filter(OSMAttributedEntity e);

}

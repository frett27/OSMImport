package com.osmimport.output;

import com.osmimport.model.OSMAttributedEntity;

/**
 * definition of a filter
 * 
 * @author pfreydiere
 * 
 */
public abstract class Filter {

	public abstract boolean filter(OSMAttributedEntity e);

}

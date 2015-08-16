package com.osmimport.output;

import java.util.List;

import com.osmimport.model.OSMAttributedEntity;

/**
 * transform abstract class
 * 
 * @author pfreydiere
 * 
 */
public abstract class Transform {

	/**
	 * transform the entity
	 * 
	 * @param e
	 *            the given entity
	 * @return a list of entities in return
	 */
	public abstract List<OSMAttributedEntity> transform(OSMAttributedEntity e);

}

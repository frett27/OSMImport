package com.osmimport.output;

import groovy.lang.Closure;

import java.util.Arrays;
import java.util.List;

import com.osmimport.model.OSMAttributedEntity;

/**
 * transform for a groovy closure
 * 
 * @author pfreydiere
 * 
 */
public class ClosureTransform extends Transform {

	/**
	 * current closure
	 */
	private Closure current;

	/**
	 * define the associated closure
	 * @param c
	 */
	public void setClosure(Closure c) {
		this.current = c;
	}

	/*
	 * (non-Javadoc)
	 * @see com.osmimport.output.Transform#transform(com.osmimport.model.OSMAttributedEntity)
	 */
	@Override
	public List<OSMAttributedEntity> transform(OSMAttributedEntity e) {
		assert current != null;

		Object ret = current.call(e);

		if (ret == null) {
			return null;
		}

		if (ret instanceof OSMAttributedEntity) {
			return Arrays
					.asList(new OSMAttributedEntity[] { (OSMAttributedEntity) ret });
		}

		if (ret instanceof List) {
			return (List) ret;
		}

		throw new RuntimeException("Transform closure " + current
				+ " must return a list or an "
				+ OSMAttributedEntity.class.getSimpleName()
				+ " returned value " + ret);

	}

}

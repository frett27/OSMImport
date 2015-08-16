package com.osmimport.output;

import com.osmimport.model.OSMAttributedEntity;

import groovy.lang.Closure;

/**
 * filter based on a groovy closure
 * 
 * @author pfreydiere
 * 
 */
public class ClosureFilter extends Filter {

	/**
	 * the associated closure
	 */
	private Closure filterClosure;

	/**
	 * constructor
	 */
	public ClosureFilter() {

	}

	/**
	 * define the associated closure
	 * 
	 * @param c
	 */
	public void setClosure(Closure c) {
		this.filterClosure = c;
	}

	/*
	 * (non-Javadoc)
	 * @see com.osmimport.output.Filter#filter(com.osmimport.model.OSMAttributedEntity)
	 */
	@Override
	public boolean filter(OSMAttributedEntity e) {
		return (Boolean) filterClosure.call(e);
	}

}

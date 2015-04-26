package com.poc.osm.output;

import com.poc.osm.model.OSMAttributedEntity;

import groovy.lang.Closure;

public class ClosureFilter extends Filter {

	private Closure filterClosure;

	public ClosureFilter() {
		
	}

	public void setClosure(Closure c)
	{
		this.filterClosure = c;
	}
	
	
	@Override
	public boolean filter(OSMAttributedEntity e) {
		return (Boolean) filterClosure.call(e);
	}

}

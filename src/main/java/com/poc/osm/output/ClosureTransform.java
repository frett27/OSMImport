package com.poc.osm.output;

import groovy.lang.Closure;

import com.poc.osm.model.OSMAttributedEntity;

public class ClosureTransform extends Transform {

	private Closure current;

	public void setClosure(Closure c) {
		this.current = c;
	}

	@Override
	public OSMAttributedEntity transform(OSMAttributedEntity e) {
		assert current != null;
		return (OSMAttributedEntity) current.call(e);
	}

}

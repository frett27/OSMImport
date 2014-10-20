package com.poc.osm.output;

import groovy.lang.Closure;

import com.poc.osm.model.OSMEntity;

public class ClosureTransform extends Transform {

	private Closure current;

	public void setClosure(Closure c) {
		this.current = c;
	}

	@Override
	public OSMEntity transform(OSMEntity e) {
		assert current != null;
		return (OSMEntity) current.call(e);
	}

}

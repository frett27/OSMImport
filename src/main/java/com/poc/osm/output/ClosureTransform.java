package com.poc.osm.output;

import groovy.lang.Closure;

import java.util.Arrays;
import java.util.List;

import com.poc.osm.model.OSMAttributedEntity;

public class ClosureTransform extends Transform {

	private Closure current;

	public void setClosure(Closure c) {
		this.current = c;
	}

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

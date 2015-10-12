package com.osmimport.dslfacilities

import com.osmimport.model.OSMAttributedEntity;

/**
 * transform DSL facility for simplify the writing of scripts
 * @author pfreydiere
 *
 */
class Transform {

	OSMAttributedEntity o

	static Transform on(OSMAttributedEntity o) {
		new Transform(o:o)
	}

	private Object currentValue;

	private Map h = [:];

	Transform map(String field) {
		currentValue = o.fields?.get(field)
		this
	}

	Transform into(String field) {
		h[field] = currentValue
		this
	}

	Transform keep(String field) {
		if (o.fields) {
			h[field] = o.fields[field]
		} else {
			h[field] = null
		}
		this
	}

	Transform keep(List<String> fields) {
		if (!fields) {
			return this
		}

		fields.each { keep(it) }

		this
	}

	Transform newValue(String field, Object value) {
		if (field == null) {
			return this
		}
		h[field] = value
		this
	}

	OSMAttributedEntity end() {
		if (!h) {
			o.fields = null
			return o;
		}
		// reset the fields on the entity
		o.fields = new HashMap<>();
		h.each { k,v ->
			o.fields[k] = v
		}
		o // return the entity
	}

}




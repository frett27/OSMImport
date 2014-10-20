package com.poc.osm.output.dsl

import java.util.Map

import com.poc.osm.output.ClosureFilter;

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;

/**
 * factory referencing a closure
 * 
 * @author pfreydiere
 *
 */
class UnaryClosureFactory extends AbstractFactory {


	Class clazz;
	String memberName;


	public boolean isHandlesNodeChildren() {
		return true
	}

	@Override
	public Object newInstance(FactoryBuilderSupport builder, Object name,
	Object value, Map attributes) throws InstantiationException,
	IllegalAccessException {
		return clazz.newInstance()
	}

	boolean onNodeChildren(FactoryBuilderSupport builder,  currentObject, Closure closure) {
		currentObject.setClosure closure
		return false;
	}

	void onNodeCompleted(FactoryBuilderSupport builder,  parent,  child) {
		if (parent != null && parent.hasProperty(memberName)) {
			parent[memberName] = child;
		}
	}
}

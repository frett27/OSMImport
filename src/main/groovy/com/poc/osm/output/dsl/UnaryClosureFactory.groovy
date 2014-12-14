package com.poc.osm.output.dsl

import java.util.Map

import com.poc.osm.output.ClosureFilter;
import com.poc.osm.output.Filter;
import com.poc.osm.output.Transform;

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;

/**
 * factory referencing a closure
 * 
 * @author pfreydiere
 *
 */
class UnaryClosureFactory extends AbstractFactory {


	/**
	 * underlying class
	 */
	Class clazz;
	
	/**
	 * factory member name
	 */
	String memberName;


	public boolean isHandlesNodeChildren() {
		return true
	}

	@Override
	public Object newInstance(FactoryBuilderSupport builder, Object name,
	Object value, Map attributes) throws InstantiationException,
	IllegalAccessException {
		
		if (value != null && (value instanceof Filter || value instanceof Transform))
		{
			return value;
		}
		
		return clazz.newInstance()
	}

	boolean onNodeChildren(FactoryBuilderSupport builder,  currentObject, Closure closure) {
		currentObject.setClosure closure
		return false;
	}

	void onNodeCompleted(FactoryBuilderSupport builder,  parent,  child) {
		if (parent != null && parent.hasProperty(memberName) ) {
			parent[memberName] = child;
		}
	}
}

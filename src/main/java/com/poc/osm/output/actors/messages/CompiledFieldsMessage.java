package com.poc.osm.output.actors.messages;

import java.io.Serializable;

import com.poc.osm.output.fields.AbstractFieldSetter;

public class CompiledFieldsMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1328469833150082987L;
	
	private AbstractFieldSetter[] setters;

	public CompiledFieldsMessage(AbstractFieldSetter[] setters) {
		this.setters = setters;
	}

	public AbstractFieldSetter[] getSetters() {
		return setters;
	}

}

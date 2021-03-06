package com.osmimport.output.actors.gdb.messages;

import java.io.Serializable;

import com.osmimport.output.fields.AbstractFieldSetter;

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

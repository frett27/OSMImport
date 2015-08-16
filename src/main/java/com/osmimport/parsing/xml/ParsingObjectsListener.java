package com.osmimport.parsing.xml;

import com.osmimport.messages.ParsingObjects;
import com.osmimport.model.OSMAttributedEntity;

public interface ParsingObjectsListener {

	public void emit(ParsingObjects objects);
	
}

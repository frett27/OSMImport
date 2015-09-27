package com.osmimport.input.csv;

import com.osmimport.model.OSMAttributedEntity;

public interface ParserCallBack {

	public void lineParsed(long lineNumber, OSMAttributedEntity entity);
	
	public void invalidLine(long lineNumber, String line);


}

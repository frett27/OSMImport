package com.osmimport.output;

import java.io.Closeable;
import java.io.IOException;

import com.osmimport.model.OSMAttributedEntity;

public abstract class AbstractWriter implements Closeable {
	
	public abstract void write(OSMAttributedEntity entity) throws Exception;

	public abstract void close() throws IOException;

	
}

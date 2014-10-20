package com.poc.osm.parsing.actors.messages;

import java.io.File;
import java.io.Serializable;

/**
 * tell the file to read
 * @author use
 *
 */
public class MessageReadFile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2544270380842205274L;
	
	private File fileToRead;
	
	public MessageReadFile(File fileToRead)
	{
		this.fileToRead = fileToRead;
	}
	
	public File getFileToRead() {
		return fileToRead;
	}
	
}

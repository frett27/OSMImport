package com.osmimport.parsing.pbf.actors.messages;

import java.io.File;
import java.io.Serializable;
import java.net.URL;

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
	
	private File urlToRead;
	
	public MessageReadFile(File urlToRead)
	{
		this.urlToRead = urlToRead;
	}
	
	public File getFileToRead() {
		return urlToRead;
	}
	
}

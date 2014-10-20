package com.poc.osm.regulation;

import java.io.Serializable;

/**
 * give measures for the regulation
 * 
 * @author pfreydiere
 * 
 */
public class MessageRegulation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7789950003032578986L;
	
	
	private long counter;

	public MessageRegulation(int counter) {
		this.counter = counter;
	}

	public long getCounter() {
		return counter;
	}

}

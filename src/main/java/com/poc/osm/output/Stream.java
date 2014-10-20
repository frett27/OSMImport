package com.poc.osm.output;


/**
 * Stream for handling entity modifications
 * 
 * @author pfreydiere
 * 
 */
public class Stream {

	public Stream() {
	}

	/**
	 * Parent stream for the objects
	 */
	public Stream parentStream;

	/**
	 * Stream filter
	 */
	public Filter filter;

	/**
	 * Stream tranform
	 */
	public Transform transform;

	/**
	 * Stream label
	 */
	public String label = "Label " + System.currentTimeMillis();

	@Override
	public String toString() {
		return "Stream(" + label + ")";
	}
	
	/**
	 * return a key for the actor
	 * @return
	 */
	public String getKey() {
		return "S" + hashCode() + "_" + label;
	}
	
}

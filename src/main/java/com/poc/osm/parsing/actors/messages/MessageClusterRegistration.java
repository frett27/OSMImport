package com.poc.osm.parsing.actors.messages;

/**
 * message on the parsing cluster behaviour
 * 
 * @author pfreydiere
 * 
 */
public enum MessageClusterRegistration {

	ASK_FOR_WAY_REGISTRATION, NEED_MORE_READ, ASK_IF_NEED_MORE_READ, 
	ALL_BLOCKS_READ

}

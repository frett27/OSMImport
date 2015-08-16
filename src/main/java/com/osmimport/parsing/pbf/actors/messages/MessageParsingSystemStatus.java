package com.osmimport.parsing.pbf.actors.messages;

/**
 * Message on server parsing phases
 * 
 * @author pfreydiere
 * 
 */
public enum MessageParsingSystemStatus {
	INITIALIZE, START_READING_FILE, END_READING_FILE, START_JOB, END_JOB, TERMINATE
}

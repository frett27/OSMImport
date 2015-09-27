package com.osmimport;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * definition of a command to separate the differents commands implementation
 * 
 * @author pfreydiere
 * 
 */
public interface CLICommand {

	/**
	 * name of the command
	 */
	String getCommandName();

	/**
	 * description of the command
	 * 
	 * @return
	 */
	String getCommandDescription();

	/**
	 * launching options
	 * 
	 * @return
	 */
	Options getOptions();

	/**
	 * get the command line structure for the help
	 * 
	 * @return
	 */
	String getCommandLineStructure();

	/**
	 * execute the command with the parsed command line
	 * 
	 * @param cmdline
	 * @throws Exception
	 */
	void execute(CommandLine cmdline) throws Exception;

}

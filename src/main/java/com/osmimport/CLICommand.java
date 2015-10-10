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
	 * description of the command for enrich commandline help
	 * 
	 * @return
	 */
	String getCommandDescription();

	/**
	 * launching options, used for parsing elements and check all elements are syntaxically correct
	 * these options are also used for help description for the command
	 * 
	 * @return
	 */
	Options getOptions();

	/**
	 * get the command line structure for the help
	 * 
	 * @return return the command line structure for the command
	 */
	String getCommandLineStructure();

	/**
	 * execute the command with command line, 
	 * the command is responsible to parse and grammatically check the options and parameters
	 * 
	 * @param cmdline the command line
	 * @throws Exception
	 */
	void execute(CommandLine cmdline) throws Exception;

}

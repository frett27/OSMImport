package com.osmimport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.LoggerFactory;

/**
 * Multiplexer for commands associated to OSMImport
 * 
 * @author pfreydiere
 * 
 */
public class MCLI {

	private static final org.slf4j.Logger logger = LoggerFactory
			.getLogger(MCLI.class);

	private static CLICommand[] registeredCommands = { new HelpCLI(),
			new ImportCLI(), new CopyCSV() };

	public static class HelpCLI implements CLICommand {

		@Override
		public String getCommandName() {
			return "help";
		}

		@Override
		public String getCommandDescription() {
			return "get help on commands";
		}

		@Override
		public String getCommandLineStructure() {
			return " " + getCommandName() + " [Command for dedicated help]";
		}
		
		@Override
		public Options getOptions() {

			Option o = OptionBuilder.create('c');
			o.setArgName("command");
			o.setRequired(false);
			o.setLongOpt("command-name");
			o.setDescription("command for a dedicated help, inform the usage and options");

			Options opts = new Options();
			opts.addOption(o);
			return opts;
		}

		@Override
		public void execute(CommandLine cmdline) throws Exception {

			List l = cmdline.getArgList();
			if (l.size() == 0)
			{
				printGeneralUsage();
			}
			
			String command = cmdline.getOptionValue("c");
			if (command == null)
			{
				command = (String)l.get(0);
			}
			
			CLICommand c = commandsReference.get(command);
			if (c == null)
			{
				System.out.println("Command " + command  + " not found for help");
				System.exit(1);
			}
			
			new HelpFormatter().printHelp("osmimport " + c.getCommandLineStructure(), c.getOptions());
			
		}

	}

	private static Map<String, CLICommand> commandsReference = new HashMap<>();
	static {
		for (CLICommand c : registeredCommands) {
			commandsReference.put(c.getCommandName(), c);
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("OSM Import");

		if (args.length < 1) {

			printGeneralUsage();
		}

		String command = args[0];

		if (!commandsReference.containsKey(command)) {
			System.out.println(" error : command " + command
					+ " not recognized");
			printGeneralUsage();
		}

		CLICommand c = commandsReference.get(command);
		logger.debug("launching command :" + command);

		GnuParser p = new GnuParser();
		CommandLine r = null;
		try {
			r = p.parse(c.getOptions(),
					Arrays.copyOfRange(args, 1, args.length));

		} catch (MissingOptionException ex) {
			System.out.println(" error in arguments :" + ex.getMessage());
			new HelpFormatter().printHelp("osmimport", c.getOptions());
			System.exit(1);
		}

		assert r != null;
		c.execute(r);

	}

	public static void printGeneralUsage() {
		System.out.println("   Available Commands :");
		for (CLICommand c : registeredCommands) {
			System.out.println("    " + c.getCommandName() + " : "
					+ c.getCommandDescription());
		}
		System.exit(1); // Error
	}

}

package com.osmimport;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class ImportCLI implements CLICommand {

	@Override
	public String getCommandName() {
		return "import";
	}

	@Override
	public String getCommandLineStructure() {
		return " " + getCommandName();
	}

	@Override
	public String getCommandDescription() {
		return "import osm file with a transformation script";
	}

	@Override
	public Options getOptions() {
		Options options = new Options();
		Option input = OptionBuilder
				.withArgName("input")
				.hasArg()
				.withLongOpt("input")
				.isRequired()
				.withDescription(
						"[REQUIRED] input PBF or OSM file, this can be .pbf or .osm files ")
				.create('i');

		Option script = OptionBuilder
				.withArgName("streams")
				.hasArg()
				.withLongOpt("streams")
				.isRequired()
				.withDescription(
						"[REQUIRED] script file describing the filtering and transformations, (.groovy files)")
				.create('s');

		Option variables = OptionBuilder
				.withArgName("var")
				.hasArg()
				.withDescription(
						"[OPTIONAL] additional variables definition that are mapped into var[name] in the script")
				.withValueSeparator(',').create('v');

		Option eventBuffer = OptionBuilder.withArgName("eventbuffer").hasArg()
				.withDescription("Buffer of events to maintain")
				.withType(Long.class).create('e');

		Option maxways = OptionBuilder.withArgName("maxways").hasArg()
				.withDescription("Number of ways to handle by pass")
				.withType(Long.class).create("m");

		options.addOption(input);
		options.addOption(script);
		options.addOption(variables);
		options.addOption(eventBuffer);
		return options;
	}

	@Override
	public void execute(CommandLine c) throws Exception {

		if (Runtime.getRuntime().maxMemory() < 5000000000L)
			throw new Exception(
					"Command line must be launched with a least 5go of memory, using -Xmx5g");

		Map<String, String> variableMap = new HashMap<>();

		String inputfile = c.getOptionValue('i');
		File inputpbfosmfile = new File(inputfile);
		if (!inputpbfosmfile.exists())
			throw new Exception("" + inputpbfosmfile + " doesn't exist");

		String streamfile = c.getOptionValue('s');
		File sf = new File(streamfile);
		if (!sf.exists())
			throw new Exception("" + sf + " doesn't exist");

		String[] v = c.getOptionValues('v');
		if (v != null && v.length > 0) {
			for (String s : v) {
				// split key=value
				int i = s.indexOf('=');
				if (i >= 0) {
					String sk = s.substring(0, i).trim();
					String sv = s.substring(i + 1).trim();
					if (!sk.isEmpty()) {
						System.out.println("Adding variable " + sk + "=" + sv);
						variableMap.put(sk, sv);
					}
				}
			}
		}

		OSMImport osmImport = new OSMImport();

		String maxWys = c.getOptionValue("m");
		if (maxWys != null) {
			osmImport.setMaxWaysToRemember(Long.parseLong(maxWys));
		}

		String events = c.getOptionValue("e");
		if (events != null) {
			osmImport.setOverridenEventBuffer(Long.parseLong(events));
		}

		// load and compile script to be sure there are no errors in it
		osmImport.loadAndCompileScript(sf, variableMap);

		// run the import
		osmImport.run(inputpbfosmfile);

	}

}

package com.poc.osm;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * command line for launching the process
 * 
 * @author pfreydiere
 * 
 */
public class CLI {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		System.out.println("Launching OSM Import Tools");

		if (Runtime.getRuntime().maxMemory() < 5000000000L)
			throw new Exception(
					"Command line must be launched with a least 5go of memory, using -Xmx5g");

		GnuParser p = new GnuParser();
		Options options = new Options();
		Option input = OptionBuilder.withArgName("inputpbf").hasArg()
				.withLongOpt("inputpbf").isRequired()
				.withDescription("input PBF OSM file").create('i');

		Option script = OptionBuilder.withArgName("streams").hasArg()
				.withLongOpt("streams").isRequired()
				.withDescription("script file describing the streams")
				.create('s');

		Option variables = OptionBuilder.withArgName("var").hasArg()
				.withDescription("variable").withValueSeparator(',').create('v');

		options.addOption(input);
		options.addOption(script);
		options.addOption(variables);
		
		Map<String,String> variableMap = new HashMap<>();
		
		File inputpbffile = null;
		File sf = null;
		try {
			CommandLine c = p.parse(options, args);

			String inputfile = c.getOptionValue('i');
			inputpbffile = new File(inputfile);
			if (!inputpbffile.exists())
				throw new Exception("" + inputpbffile + " doesn't exist");

			String streamfile = c.getOptionValue('s');
			sf = new File(streamfile);
			if (!sf.exists())
				throw new Exception("" + sf + " doesn't exist");

			
			String[] v = c.getOptionValues('v');
			if (v != null && v.length > 0)
			{
				for (String s : v)
				{
					// split key=value
					int i = s.indexOf('=');
					if (i >= 0)
					{
						String sk = s.substring(0,i).trim();
						String sv = s.substring(i+1).trim();
						if (!sk.isEmpty())
						{
							System.out.println("Adding variable " + sk + "=" + sv);
							variableMap.put(sk, sv);
						}
					}
				}
				
				
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
			new HelpFormatter().printHelp("osmimport", options);
			System.exit(1);
		}

		OSMImport osmImport = new OSMImport();
		
		

		// load and compile script to be sur there are no errors in it
		osmImport.loadAndCompileScript(sf, variableMap);

		osmImport.run(inputpbffile);

	}
}

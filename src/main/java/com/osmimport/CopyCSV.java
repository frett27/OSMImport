package com.osmimport;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.fgdbapi.thindriver.TableHelper;
import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;
import org.fgdbapi.thindriver.swig.Row;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.osmimport.input.csv.CSVParser;
import com.osmimport.input.csv.ParserCallBack;
import com.osmimport.model.OSMAttributedEntity;
import com.osmimport.output.actors.gdb.GDBOutputTools;
import com.osmimport.output.dsl.TStructure;
import com.osmimport.output.fields.AbstractFieldSetter;
import com.osmimport.structures.model.Structure;
import com.osmimport.structures.model.Table;
import com.osmimport.tools.Tools;

public class CopyCSV implements CLICommand {

	static ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
			.getLogger(Logger.ROOT_LOGGER_NAME);

	@Override
	public String getCommandName() {
		return "copycsv";
	}

	@Override
	public String getCommandDescription() {
		return "copy a csv file into a file geodatabase";
	}

	@Override
	public Options getOptions() {

		Options options = new Options();
		Option input = OptionBuilder.withArgName("input").hasArg()
				.withLongOpt("input").isRequired().withDescription("input")
				.create('i');

		Option output = OptionBuilder.withArgName("output").hasArg()
				.withLongOpt("output").isRequired()
				.withDescription("destination output file geodatabase").create('o');

		Option structure = OptionBuilder
				.withArgName("structure")
				.hasArg()
				.withLongOpt("structure")
				.isRequired()
				.withDescription(
						"script file describing the structures of tables")
				.create('s');

		options.addOption(input);
		options.addOption(output);
		options.addOption(structure);

		return options;
	}

	@Override
	public void execute(CommandLine cmdline) throws Exception {

		System.out.println("Launching Copy Tools");

		File finput = null;
		File foutput = null;

		File structureFile = null;

		String inputfile = cmdline.getOptionValue('i');
		finput = new File(inputfile);

		String outputfile = cmdline.getOptionValue('o');
		foutput = new File(outputfile);

		String sfile = cmdline.getOptionValue('s');
		structureFile = new File(sfile);

		assert finput != null;
		assert foutput != null;
		assert structureFile != null;

		CopyCSV c = new CopyCSV();
		c.readStructure(structureFile);

		c.input = finput;
		c.output = foutput;

		c.copy();

	}

	private File input;
	private File output;
	private Structure structure;

	public Structure readStructure(File structureFile) throws Exception {
		assert structureFile != null;
		assert structureFile.exists();

		CompilerConfiguration compilerConfiguration = new CompilerConfiguration();

		// custom imports for ease the use of the geometry types
		ImportCustomizer icz = new ImportCustomizer();
		icz.addStarImports("com.osmimport", "com.esri.core.geometry",
				"com.osmimport.model");
		icz.addStaticImport("org.fgdbapi.thindriver.xml.EsriGeometryType",
				"ESRI_GEOMETRY_POINT");
		icz.addStaticImport("org.fgdbapi.thindriver.xml.EsriGeometryType",
				"ESRI_GEOMETRY_MULTIPOINT");
		icz.addStaticImport("org.fgdbapi.thindriver.xml.EsriGeometryType",
				"ESRI_GEOMETRY_POLYLINE");
		icz.addStaticImport("org.fgdbapi.thindriver.xml.EsriGeometryType",
				"ESRI_GEOMETRY_POLYGON");
		icz.addStaticImport("org.fgdbapi.thindriver.xml.EsriGeometryType",
				"ESRI_GEOMETRY_MULTI_PATCH");

		compilerConfiguration.addCompilationCustomizers(icz);

		File parentFile = structureFile.getParentFile();

		if (parentFile == null) {
			parentFile = new File("."); // fix, the file is null when a name is
										// directly passed
		}

		GroovyScriptEngine gse = new GroovyScriptEngine(
				new URL[] { parentFile.toURL() });
		gse.setConfig(compilerConfiguration);
		Binding binding = new Binding();
		binding.setVariable("builder", new TStructure());
		binding.setVariable("out", System.out);

		Object result = gse.run(structureFile.getName(), binding);

		if (result == null || !(result instanceof Structure)) {
			throw new Exception(
					"invalid script, it must return the structure, the script return :"
							+ result);
		}

		structure = (Structure) result;
		return structure;

	}

	public void copy() throws Exception {

		assert input != null;
		assert input.isDirectory();
		assert output != null;

		// create the GDB
		Geodatabase geodatabase = FGDBJNIWrapper.createGeodatabase(output
				.getAbsolutePath());

		for (Table t : structure.values()) {

			TableHelper h = Tools.convertTable(t);

			String tableDef = h.buildAsString();
			System.out.println("creating table " + h.getName()
					+ " with definition : \n" + tableDef);

			org.fgdbapi.thindriver.swig.Table newTable = geodatabase
					.createTable(tableDef, "");

			System.out.println("table " + h.getName() + " created");

			System.out.println("open the table " + h.getName());

			final org.fgdbapi.thindriver.swig.Table innerTable = geodatabase
					.openTable(h.getName());
			try {
				innerTable.setWriteLock();
				innerTable.setLoadOnlyMode(true);
				try {
					final ArrayList<AbstractFieldSetter> setters = GDBOutputTools
							.createCompiledFieldSetters(innerTable);

					final AtomicLong atomicLong = new AtomicLong();

					File csvfile = new File(input, t.getName() + ".csv");
					CSVParser p = new CSVParser(t, new ParserCallBack() {

						@Override
						public void lineParsed(long lineNumber,
								OSMAttributedEntity entity) {

							try {

								Row r = innerTable.createRowObject();

								for (AbstractFieldSetter f : setters) {
									try {
										f.setValue(entity.getFields().get(
												f.getFieldName()));
										// System.out.println(lineNumber);
										f.store(r);
									} catch (Exception ex) {
										root.error("error in storing value on "
												+ f + " :" + ex.getMessage(),
												ex);
									}
								}

								innerTable.insertRow(r);
								r.delete();
								long c = atomicLong.getAndAdd(1);
								if (c % 100000 == 0) {
									root.info("" + c + " elements copied");
								}

							} catch (Exception ex) {
								root.error("error in storing entity " + entity);
							}
						}

						@Override
						public void invalidLine(long lineNumber, String line) {
							root.error("error parsing {}", line);
						}
					});

					p.parse(new BufferedInputStream(
							new FileInputStream(csvfile)));

					System.out.println("successfully created");

				} finally {

					newTable.freeWriteLock();
					newTable.setLoadOnlyMode(false);

				}
			} finally {
				geodatabase.closeTable(newTable);
			}

		} // for

	}
	
	@Override
	public String getCommandLineStructure() {
		return " " + getCommandName();
	}

}

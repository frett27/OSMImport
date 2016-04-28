package com.osmimport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.fgdbapi.thindriver.swig.EnumRows;
import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;
import org.fgdbapi.thindriver.swig.Row;
import org.fgdbapi.thindriver.swig.Table;
import org.slf4j.LoggerFactory;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.osmimport.structures.model.FeatureClass;
import com.osmimport.structures.model.Field;
import com.osmimport.structures.model.FieldType;
import com.osmimport.structures.model.Structure;
import com.osmimport.tools.StructureTools;
import com.osmimport.tools.Tools;

/**
 * export the FGDB to other format
 * 
 * @author pfreydiere
 * 
 */
public class ExportFGDB implements CLICommand {

	private static ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
			.getLogger(ExportFGDB.class);

	@Override
	public String getCommandName() {
		return "export";
	}

	@Override
	public String getCommandDescription() {
		return "export content of FGDB table or feature class to an other format";
	}

	@Override
	public Options getOptions() {

		Options options = new Options();

		Option input = OptionBuilder.withArgName("input").hasArg()
				.withLongOpt("input").isRequired().withDescription("input")
				.create('i');

		Option output = OptionBuilder.withArgName("output").hasArg()
				.withLongOpt("output").isRequired()
				.withDescription("destination output file geodatabase")
				.create('o');

		Option structureFile = OptionBuilder.withArgName("structurefile")
				.hasArg().withLongOpt("structure").isRequired()
				.withDescription("table structure file").create("s");

		Option format = OptionBuilder.withArgName("format").hasArg()
				.withLongOpt("format").isRequired()
				.withDescription("output format ").create('f');

		options.addOption(input);
		options.addOption(output);
		options.addOption(structureFile);
		options.addOption(format);

		return options;
	}

	@Override
	public String getCommandLineStructure() {
		return " " + getCommandName();
	}

	@Override
	public void execute(CommandLine c) throws Exception {

		String inputfile = c.getOptionValue('i');
		File inputpbfosmfile = new File(inputfile);
		if (!inputpbfosmfile.exists())
			throw new Exception("" + inputpbfosmfile + " doesn't exist");

		Geodatabase g = FGDBJNIWrapper.openGeodatabase(inputpbfosmfile
				.toString());
		try {

			String ouput = c.getOptionValue('o');
			File outputFile = new File(ouput);
			if (!outputFile.exists())
				throw new Exception("" + outputFile + " doesn't exist");

			String streamfile = c.getOptionValue('s');
			File sf = new File(streamfile);
			if (!sf.exists())
				throw new Exception("" + sf + " doesn't exist");

			logger.debug("read structure");
			Structure structure = StructureTools.readStructure(sf);

			String format = c.getOptionValue('f');
			if (!"geojson".equals(format)) {
				throw new Exception("format " + format
						+ " not supported, supported formats : geojson");
			}

			List<String> tablenameToExport = new ArrayList<>(structure.keySet());

			for (String t : tablenameToExport) {

				logger.debug("converting " + t);

				Table table = g.openTable(t);
				try {
					File outputTableFile = new File(outputFile, t + ".geojson");
					logger.debug("opening output file :" + outputTableFile);

					com.osmimport.structures.model.Table structureTable = structure
							.get(t);

					FileOutputStream fos = new FileOutputStream(outputTableFile);
					OutputStreamWriter w = new OutputStreamWriter(fos);
					try {

						w.write("{\n");
						w.write("	\"type\":\"FeatureCollection\",\n");
						w.write("	\"features\":[\n");

						List<Field> flds = structureTable.getFields();

						StringBuilder sb = new StringBuilder();
						for (Field f : flds) {
							if (sb.length() > 0)
								sb.append(",");

							sb.append(f.getName());
						}

						Geometry.Type fcType = null;
						if (structureTable instanceof FeatureClass) {
							fcType = ((FeatureClass) structureTable)
									.getESRIGeomType();
						}

						EnumRows cur = table.search(sb.toString(), "", true);
						try {
							StringBuilder ssb = new StringBuilder();
							Tools.space(ssb, 10);
							String space = ssb.toString();

							boolean firstEntity = true;

							Row r = cur.next();
							while (r != null) {

								if (!firstEntity)
									w.write(",");

								firstEntity = false;

								w.write(space);
								w.write("{\n");
								try {

									w.write(space + "\"type\":\"Feature\",");

									for (Field f : flds) {
										if (f.getType() == FieldType.GEOMETRY) {
											assert fcType != null;
											w.write(space
													+ "\"geometry\":"
													+ GeometryEngine
															.geometryToGeoJson(
																	4326,
																	GeometryEngine
																			.geometryFromEsriShape(
																					r.getGeometry(),
																					fcType)));

										}
									}

									w.write("\n" + space
											+ ",\"properties\":{\n");

									boolean firstField = true;
									for (Field f : flds) {

										if (f.getType() != FieldType.GEOMETRY) {
											if (!firstField) {
												w.write(",");
											}

											String v = "null";
											try {

												if (!r.isNull(f.getName())) {
													switch (f.getType()) {
													case DOUBLE:
														v = ""
																+ r.getDouble(f
																		.getName());
														break;
													case INTEGER:
														v = ""
																+ r.getInteger(f
																		.getName());
														break;
													case LONG:
														v = ""
																+ r.getInteger(f
																		.getName());
														break;
													case SHORT:
														v = ""
																+ r.getShort(f
																		.getName());
														break;
													case SINGLE:
														v = ""
																+ r.getFloat(f
																		.getName());
													case STRING:

														String s = r
																.getString(f
																		.getName());
														if (s != null) {
															v = "\""
																	+ s.replaceAll(
																			"\"",
																			"\\\"")
																	+ "\"";
														}

														break;

													}
												}
												w.write("\"" + f.getName()
														+ "\":" + v);
												firstField = false;

											} catch (Exception ex) {
												logger.error(
														"error while reading field "
																+ f, ex);
											}
										}
									}

									w.write(space + "}\n");

								} finally {
									w.write("}\n");
								}
								assert r != null;
								r.delete();
								r = cur.next();
							}

						} finally {
							cur.Close();
						}

						w.write("    ]");
						w.write("}\n");

					} finally {
						w.close();
					}

				} finally {
					g.closeTable(table);
				}

				logger.debug("done for " + t);
			}

		} finally {
			g.delete();
		}

	}

}

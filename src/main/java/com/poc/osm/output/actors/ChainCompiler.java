package com.poc.osm.output.actors;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import com.poc.osm.output.OutCell;
import com.poc.osm.output.ProcessModel;
import com.poc.osm.output.Stream;
import com.poc.osm.output.dsl.TBuilder;

/**
 * construct the output chain
 * 
 * @author pfreydiere
 * 
 */
public class ChainCompiler {

	public ChainCompiler() {

	}

	/**
	 * compile the script into processing model, to be able to construct the
	 * pipeline
	 * 
	 * @param processFile
	 * @param osmStream
	 * @return
	 * @throws Exception
	 */
	public ProcessModel compile(File processFile, Stream mainStream)
			throws Exception {

		CompilerConfiguration compilerConfiguration = new CompilerConfiguration();

		ImportCustomizer icz = new ImportCustomizer();
		icz.addStarImports("com.esrifrance.osm", "com.esri.core.geometry");
		icz.addStaticImport("com.esrifrance.fgdbapi.xml.EsriGeometryType",
				"ESRI_GEOMETRY_POINT");
		icz.addStaticImport("com.esrifrance.fgdbapi.xml.EsriGeometryType",
				"ESRI_GEOMETRY_MULTIPOINT");
		icz.addStaticImport("com.esrifrance.fgdbapi.xml.EsriGeometryType",
				"ESRI_GEOMETRY_POLYLINE");
		icz.addStaticImport("com.esrifrance.fgdbapi.xml.EsriGeometryType",
				"ESRI_GEOMETRY_POLYGON");
		icz.addStaticImport("com.esrifrance.fgdbapi.xml.EsriGeometryType",
				"ESRI_GEOMETRY_MULTI_PATCH");
		

		compilerConfiguration.addCompilationCustomizers(icz);

		GroovyScriptEngine gse = new GroovyScriptEngine(new URL[] { processFile
				.getParentFile().toURL() });
		gse.setConfig(compilerConfiguration);
		Binding binding = new Binding();
		binding.setVariable("builder", new TBuilder());
		binding.setVariable("osmstream", mainStream);
		binding.setVariable("out", System.out);
		

		Object result = gse.run(processFile.getName(), binding);

		if (result == null || !(result instanceof ProcessModel)) {
			throw new Exception(
					"invalid script, it must return the constructed process model, the script return :"
							+ result);
		}

		ProcessModel pm = (ProcessModel) result;
		pm.mainStream = mainStream;

		return pm;
	}

	public static class ValidateResult {
		public String[] warnings;
		public Stream[] frontStreams;
	}

	/**
	 * Validate the process model, export warning messages errors in the model
	 * throw Exception
	 * 
	 * @return object that contain warnings, and front streams
	 */
	public ValidateResult validateProcessModel(ProcessModel pm)
			throws Exception {

		ArrayList<String> warnings = new ArrayList<String>();

		HashSet<Stream> markedStreams = new HashSet<Stream>();

		HashSet<Stream> frontStreams = new HashSet<Stream>();

		Collection<OutCell> outs = pm.outs;
		if (outs == null || outs.size() == 0)
			throw new Exception("no out specified, invalid model");

		for (OutCell c : outs) {
			Stream[] ss = c.streams;
			if (ss == null) {
				warnings.add("unused out, because there are no streams :" + c);
				continue;
			}

			for (Stream s : ss) {
				if (s == null) {
					warnings.add("null stream in collection for " + c);
					continue;
				}

				// mark stream
				markedStreams.add(s);

				Collection<Stream> analyzedStack = new ArrayList<Stream>();

				Stream current = s;
				while (current.parentStream != null
						&& current.parentStream != pm.mainStream) {
					markedStreams.add(current);
					analyzedStack.add(current);
					current = current.parentStream;
				}

				if (current.parentStream == null) {
					warnings.add("null parent, and the following streams [ "
							+ analyzedStack
							+ " ]are not connected to main stream");
				} else {
					// front stream
					frontStreams.add(current);
				}

			} // for streams

		} // outcells

		ValidateResult r = new ValidateResult();
		r.frontStreams = frontStreams.toArray(new Stream[0]);
		r.warnings = warnings.toArray(new String[0]);
		return r;
	}

}

package com.osmimport.tools;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import java.io.File;
import java.net.URL;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import com.osmimport.output.dsl.TStructure;
import com.osmimport.structures.model.Structure;

public class StructureTools {

	/**
	 * read a structure file and return a structure object
	 * 
	 * @param structureFile
	 *            the structure file to read
	 * @return associated structure object
	 * @throws Exception
	 */
	public static Structure readStructure(File structureFile) throws Exception {
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

		return (Structure) result;

	}

}

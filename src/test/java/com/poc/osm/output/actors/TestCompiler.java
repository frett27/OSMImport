package com.poc.osm.output.actors;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.poc.osm.output.ProcessModel;
import com.poc.osm.output.Stream;

/**
 * test the compilation options
 * 
 * @author pfreydiere
 * 
 */
public class TestCompiler extends TestCase {

	public void testCompile() throws Exception {

		ChainCompiler cc = new ChainCompiler();

		Stream osmStream = new Stream();
		osmStream.label = "result";
		ProcessModel pm = cc
				.compile(
						new File(
								"scripts/test_compiler.groovy"),
						osmStream, null);

		
		pm.computeChildrens();
		pm.compactAndExtractOthers();

		System.out.println(pm);

	}
	
	public void testCompileWithVars() throws Exception {

		ChainCompiler cc = new ChainCompiler();

		Stream osmStream = new Stream();
		osmStream.label = "result";
		
		Map<String,String> m = new HashMap<>();
		m.put("outputgdb", "c:\\temp\\b.gdb");
		
		ProcessModel pm = cc
				.compile(
						new File(
								"scripts/test_compiler_withvars.groovy"),
						osmStream, m);

		
		pm.computeChildrens();
		pm.compactAndExtractOthers();

		System.out.println(pm);

	}

}

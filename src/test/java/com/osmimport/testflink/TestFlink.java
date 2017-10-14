package com.osmimport.testflink;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
//
//import org.apache.flink.api.common.functions.MapFunction;
//import org.apache.flink.api.java.DataSet;
//import org.apache.flink.api.java.ExecutionEnvironment;
//import org.apache.flink.api.java.operators.DataSource;
import org.junit.Test;

import com.osmimport.output.ClosureFilter;
import com.osmimport.output.ModelElement;
import com.osmimport.output.ProcessModel;
import com.osmimport.output.Stream;
import com.osmimport.output.actors.gdb.ChainCompiler;

import groovy.lang.Closure;

public class TestFlink {

	@Test
	public void testOSMCompileWithFlink() throws Exception {

		// experiment on hosting actors in flink
		// as of 2017, the hydratation of Groovy Closure must be analyzed
		
		
		
//		ExecutionEnvironment env = ExecutionEnvironment
//				.getExecutionEnvironment();
//		
//	
//		ArrayList<String> a = new ArrayList<String>();
//		a.add("hello");
//		DataSource<String> fromCollection = env.fromCollection(a);
//
//		ChainCompiler cc = new ChainCompiler();
//
//		Stream osmStream = new Stream();
//		osmStream.label = "result";
//
//		Map<String, String> variables = new HashMap<>();
//		variables.put("gdb", "v");
//
//		final ProcessModel pm = cc.compile(new File("scripts/fire_tests.groovy"),
//				osmStream, variables);
//
//		pm.computeChildrens();
//		pm.compactAndExtractOthers();
//		
//		final Set<ModelElement> children = pm.getChildren(pm.mainStream);
//		
//		// take first
//		final ModelElement first = children.toArray(new ModelElement[0])[0];
//
//		Stream s = (Stream)first;
//		final ClosureFilter f = (ClosureFilter)s.filter;
//		final Closure closure = f.getClosure();
//		
//		f.setClosure(closure.dehydrate());
//		final Closure unhydrate = closure.dehydrate();
//		
//		DataSet<String> d = fromCollection
//				.map(new MapFunction<String, String>() {
//					@Override
//					public String map(String value) throws Exception {
//
//						unhydrate.getClass();
//
//						return null;
//					}
//				});
//
//		d.print();
//		// env.execute();

	}

}

package com.poc.osm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.fgdbapi.thindriver.TableHelper;
import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;
import org.fgdbapi.thindriver.swig.Table;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.poc.osm.actors.StreamProcessingActor;
import com.poc.osm.output.GDBReference;
import com.poc.osm.output.OutCell;
import com.poc.osm.output.ProcessModel;
import com.poc.osm.output.Stream;
import com.poc.osm.output.actors.ChainCompiler;

public class TestClusterParsing extends TestCase {

	private void recurseDelete(File f) {
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for (File fi : files) {
				recurseDelete(fi);
			}

		}
		f.delete();

	}
//
//	public void testClusterParsing() throws Exception {
//
//		recurseDelete(new File("c:\\temp\\t.gdb"));
//
//		Config config = ConfigFactory.load();
//
//		ActorSystem sys = ActorSystem.create("osmcluster",
//				config.getConfig("osmcluster"));
//
//		ActorRef flowRegulator = sys.actorOf(Props.create(FlowRegulator.class,
//				"output", 200000L)); // consigne à 200k
//
//		// parsing result output actor, will redirect the parsing in result
//		ActorRef parsingOutput = sys.actorOf(Props.create(ParsingOutput.class,
//				"/user/result"));
//
//		ActorRef dispatcher = sys.actorOf(
//				Props.create(ParsingDispatcher.class, parsingOutput),
//				"dispatcher");
//
//		// reading actor
//		ActorRef reading = sys.actorOf(
//				Props.create(ReadingActor.class, dispatcher, flowRegulator),
//				"reading");
//
//		// init the worker
//		ActorRef worker1 = sys.actorOf(Props.create(WayConstructorActor.class),
//				"worker1");
//		worker1.tell(MessageParsingSystemStatus.INITIALIZE, ActorRef.noSender());
//
//		// ActorRef worker2 =
//		// sys.actorOf(Props.create(WayConstructorActor.class),
//		// "worker2");
//		// worker2.tell(MessageParsingSystemStatus.INITIALIZE,
//		// ActorRef.noSender());
//
//		// init the output
//		List<ActorRef> abonnees = constructOutputModel(sys, flowRegulator);
//
//		sys.actorOf(Props.create(ResultActor.class, abonnees), "result");
//
//		// init the reading
//		reading.tell(MessageParsingSystemStatus.INITIALIZE, ActorRef.noSender());
//
//		reading.tell(new MessageReadFile(new File(
//				"C:\\Projets\\osmimport\\rhone-alpes-latest.osm.pbf")),
//				ActorRef.noSender());
//
//		while (true) {
//			Thread.sleep(2000);
//
//			System.out.println("current time :" + System.currentTimeMillis());
//		}
//
//	}

	private List<ActorRef> constructOutputModel(ActorSystem sys,
			ActorRef flowRegulator) throws Exception {

		// front stream that will receive the OSMEntity stream
		List<ActorRef> resultWished = new ArrayList<ActorRef>();

		// call th compiler
		ChainCompiler cc = new ChainCompiler();

		// creating the result stream of the 
		Stream osmStream = new Stream();
		osmStream.label = "result";
		
		// compile the process model
		ProcessModel pm = cc
				.compile(
						new File(
								"src/test/resources/com/esrifrance/osm/output/actors/testchain.groovy"),
						osmStream, null);

		// dump it
		System.out.println(pm);

		/**
		 * geodatabase references by path
		 */
		Map<String, Geodatabase> geodatabaseRefs = new HashMap<String, Geodatabase>();

		for (OutCell oc : pm.outs) {

			// get output
			GDBReference r = oc.gdb;
			String path = r.getPath();

			ActorRef tableOutput = null;

			Geodatabase geodatabase;
			
			if (!geodatabaseRefs.containsKey(path)) {
				// geodatabase as not been created yet
				
				System.out.println("create geodatabase " + path);
				// create the GDB
				
				geodatabase = FGDBJNIWrapper.createGeodatabase(path);
				geodatabaseRefs.put(path, geodatabase);

				for (TableHelper h : r.listTables()) {
					
					String tableDef = h.buildAsString();
					System.out.println("creating table " + h.getName()
							+ " with definition : \n" + tableDef);
					Table newTable = geodatabase.createTable(tableDef, "");
					
					System.out.println("successfully created");

					// create the actor
//					ActorRef actorOf = sys.actorOf(Props.create(
//							TableOutputActor.class, newTable, flowRegulator),
//							toActorName("T__" + h.getName()));
					
					
//					ActorRef tableCompiledOutputActor = sys.actorOf(Props.create(
//							CompiledTableOutputActor.class, newTable, flowRegulator),
//							toActorName("T__" + h.getName()));
//					
//					// wrap counter at front of the table output
//					ActorRef counter = sys.actorOf(Props
//							.create(CompiledFieldsMessageCounter.class, tableCompiledOutputActor,
//									flowRegulator));
//					
//					
//					ActorRef fieldsCompiler = sys.actorOf(Props.create(
//							FieldsCompilerActor.class, newTable, counter),
//							toActorName("FC__" + h.getName()));
					

//					if (oc.tablename.equals(h.getName())) {
//						// wrap the counter for the output
//						tableOutput = fieldsCompiler;
//					}

				}

			} else {
				
				geodatabase = geodatabaseRefs.get(path);

				// TODO refactor

			}

			assert tableOutput != null;

			System.out.println("creating the streams");
			
			for (Stream s : oc.streams) {
				Stream current = s;
				ActorRef o = tableOutput;

				while (current != null && !"result".equals(current.label)) {
					System.out.println("creation du flux " + s.label);

					o = sys.actorOf(Props.create(StreamProcessingActor.class,
							s.filter, s.transform, o), toActorName(s.label));
					current = current.parentStream;

					if ("result".equals(current.label)) {
						resultWished.add(o);
						System.out.println("register " + o + " with result");
					}

				}

			}

		}// for outs

		return resultWished;
	}

	public String toActorName(String s) {
		assert s != null;
		return s.replaceAll("[\\ \\:\\\\]", "_");
	}

}

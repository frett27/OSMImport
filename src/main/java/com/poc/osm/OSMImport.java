package com.poc.osm;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.esrifrance.fgdbapi.swig.EsriFileGdb;
import com.esrifrance.fgdbapi.swig.Geodatabase;
import com.esrifrance.fgdbapi.swig.Table;
import com.poc.osm.output.GDBReference;
import com.poc.osm.output.OutCell;
import com.poc.osm.output.ProcessModel;
import com.poc.osm.output.Stream;
import com.poc.osm.output.actors.ChainCompiler;
import com.poc.osm.output.actors.ChainCompiler.ValidateResult;
import com.poc.osm.output.actors.CompiledFieldsMessageCounter;
import com.poc.osm.output.actors.CompiledTableOutputActor;
import com.poc.osm.output.actors.FieldsCompilerActor;
import com.poc.osm.output.model.TableHelper;
import com.poc.osm.parsing.actors.ParsingSubSystemActor;
import com.poc.osm.parsing.actors.messages.MessageParsingSystemStatus;
import com.poc.osm.parsing.actors.messages.MessageReadFile;
import com.poc.osm.regulation.FlowRegulator;
import com.poc.osm.tools.Tools;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Main class for the system
 * 
 * @author pfreydiere
 * 
 */
public class OSMImport {

	public OSMImport() {

	}

	private Map<String, OpenedGeodatabase> geodatabases;
	private ProcessModel pm;
	private Set<Stream> frontStreams;

	public void loadAndCompileScript(File script) throws Exception {
		assert script != null;
		assert script.exists();

		ChainCompiler c = new ChainCompiler();
		Stream mainStream = new Stream(); // fake for getting reference
		mainStream.actorName = "result";
		ProcessModel pm = c.compile(script, mainStream);

		ValidateResult v = c.validateProcessModel(pm);
		if (v.warnings != null && v.warnings.length > 0) {
			System.out.println("WARNINGS in the model :");
			System.out.println(Arrays.asList(v.warnings));
		}

		this.pm = pm;
		this.frontStreams = new HashSet<Stream>(Arrays.asList(v.frontStreams));
	}

	private static class OpenedGeodatabase {
		public Geodatabase geodatabase;
		public Map<String, Table> tables = new HashMap<String, Table>();
	}

	/**
	 * create the needed tabled
	 * 
	 * @param pm
	 *            the processing model
	 * @return a hash with all opened geodatabases
	 * @throws Exception
	 */
	private void createGeodatabasesAndTables() throws Exception {

		assert pm != null;

		Map<String, OpenedGeodatabase> g = new HashMap<String, OpenedGeodatabase>();

		for (OutCell oc : pm.outs) {

			// get output
			GDBReference r = oc.gdb;
			String path = r.getPath();

			ActorRef tableOutput = null;

			Geodatabase geodatabase;

			if (!g.containsKey(path)) {
				// geodatabase as not been created yet

				OpenedGeodatabase og = new OpenedGeodatabase();

				System.out.println("create geodatabase " + path);
				// create the GDB

				geodatabase = EsriFileGdb.createGeodatabase(path);
				og.geodatabase = geodatabase;

				for (TableHelper h : r.listTables()) {

					String tableDef = h.buildAsString();
					System.out.println("creating table " + h.getName()
							+ " with definition : \n" + tableDef);

					Table newTable = geodatabase.createTable(tableDef, "");

					System.out.println("table " + h.getName() + " created");

					og.tables.put(h.getName(), newTable);

					System.out.println("successfully created");

				}

				g.put(path, og);

			}

		}// for outs

		this.geodatabases = g;
	}

	
	
	public void run(File osmInputFile) throws Exception {

		assert pm != null;
		assert frontStreams != null && frontStreams.size() > 0;
		// constructing the actor system

		Config config = ConfigFactory.load();

		ActorSystem sys = ActorSystem.create("osmcluster",
				config.getConfig("osmcluster"));

		ActorRef flowRegulator = sys.actorOf(Props.create(FlowRegulator.class,
				"output", 1000L)); // consigne

		ActorRef parsingSubSystem = sys.actorOf(Props.create(
				ParsingSubSystemActor.class, flowRegulator));

		createGeodatabasesAndTables();

		// for each out, create the output actor
		for (OutCell oc : pm.outs) {

			// get output
			GDBReference r = oc.gdb;
			String path = r.getPath();

			

			if (!geodatabases.containsKey(path)) {
				throw new Exception("geodatabase " + path + " not found");
			}

			OpenedGeodatabase openedGeodatabase = geodatabases.get(path);

			Table table = openedGeodatabase.tables.get(oc.tablename);

			String keyname = path + "_" + oc.tablename;

			// ///////////////////////////////////////////////////////////////
			// create output actors

			ActorRef tableCompiledOutputActor = sys.actorOf(
					Props.create(CompiledTableOutputActor.class, table,
							flowRegulator).withDispatcher("pdisp"),
					Tools.toActorName("T__" + keyname));

			// wrap counter at front of the table output
			ActorRef counter = sys.actorOf(Props.create(
					CompiledFieldsMessageCounter.class,
					tableCompiledOutputActor, flowRegulator));

			ActorRef fieldsCompiler = sys.actorOf(
					Props.create(FieldsCompilerActor.class, table, counter),
					Tools.toActorName("FC__" + keyname));

			// all tables has been processed

			// each output tables have a router actor and the streams connect to
			// it

			oc._actorRef = fieldsCompiler;

		}// for outs

		pm.computeChildrens();
		pm.compactAndExtractOthers();

		ActorRef resultActor = pm.getOrCreateActorRef(sys, pm.mainStream);

		// init the reading
		parsingSubSystem.tell(MessageParsingSystemStatus.INITIALIZE,
				ActorRef.noSender());

		Thread.sleep(2000);

		System.out.println("launch the reading");
		parsingSubSystem.tell(new MessageReadFile(osmInputFile),
				ActorRef.noSender());

		sys.awaitTermination();

	}

}

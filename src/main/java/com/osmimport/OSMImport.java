package com.osmimport;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.fgdbapi.thindriver.TableHelper;
import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;
import org.fgdbapi.thindriver.swig.Table;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.osmimport.output.GDBReference;
import com.osmimport.output.OutCell;
import com.osmimport.output.ProcessModel;
import com.osmimport.output.Stream;
import com.osmimport.output.actors.ChainCompiler;
import com.osmimport.output.actors.CompiledTableOutputActor;
import com.osmimport.output.actors.FieldsCompilerActor;
import com.osmimport.output.actors.ChainCompiler.ValidateResult;
import com.osmimport.parsing.pbf.actors.PbfParsingSubSystemActor;
import com.osmimport.parsing.pbf.actors.messages.MessageParsingSystemStatus;
import com.osmimport.parsing.pbf.actors.messages.MessageReadFile;
import com.osmimport.parsing.xml.XMLParsingSubSystemActor;
import com.osmimport.regulation.FlowRegulator;
import com.osmimport.regulation.MessageRegulatorRegister;
import com.osmimport.tools.Tools;
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

	public void loadAndCompileScript(File script,
			Map<String, String> additionalVariables) throws Exception {
		assert script != null;
		assert script.exists();

		ChainCompiler c = new ChainCompiler();
		Stream mainStream = new Stream(); // fake for getting reference
		mainStream.actorName = "result";
		ProcessModel pm = c.compile(script, mainStream, additionalVariables);

		ValidateResult v = c.validateProcessModel(pm);
		if (v.warnings != null && v.warnings.length > 0) {
			System.out.println("WARNINGS in the model :");
			System.out.println(Arrays.asList(v.warnings));
		}

		this.pm = pm;

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

			Geodatabase geodatabase;

			if (!g.containsKey(path)) {
				// geodatabase as not been created yet

				OpenedGeodatabase og = new OpenedGeodatabase();

				System.out.println("create geodatabase " + path);

				// create the GDB
				geodatabase = FGDBJNIWrapper.createGeodatabase(path);
				og.geodatabase = geodatabase;
				for (TableHelper h : r.listTables()) {

					String tableDef = h.buildAsString();
					System.out.println("creating table " + h.getName()
							+ " with definition : \n" + tableDef);

					Table newTable = geodatabase.createTable(tableDef, "");

					System.out.println("table " + h.getName() + " created");

					// closing table to be sure the definition is correctly
					// stored

					geodatabase.closeTable(newTable);

					System.out.println("open the table " + h.getName());

					newTable = geodatabase.openTable(h.getName());

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
		// constructing the actor system

		Config config = ConfigFactory.load();

		Config osmclusterconfig = config.getConfig("osmcluster");

		ActorSystem sys = ActorSystem.create("osmcluster", osmclusterconfig);

		ActorRef flowRegulator = sys.actorOf(Props.create(FlowRegulator.class,
				"output", osmclusterconfig.getLong("eventbuffer")));

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

			if (table == null)
				throw new Exception("table " + oc.tablename + " not found");

			String keyname = path + "_" + oc.tablename;

			// ///////////////////////////////////////////////////////////////
			// create output actors

			ActorRef tableCompiledOutputActor = sys.actorOf(
					Props.create(CompiledTableOutputActor.class, table,
							flowRegulator).withDispatcher("pdisp"),
					Tools.toActorName("T__" + keyname));
			flowRegulator.tell(new MessageRegulatorRegister(
					tableCompiledOutputActor), ActorRef.noSender());

			ActorRef fieldsCompiler = sys.actorOf(
					Props.create(FieldsCompilerActor.class, table,
							tableCompiledOutputActor), Tools.toActorName("FC__"
							+ keyname));
			flowRegulator.tell(new MessageRegulatorRegister(fieldsCompiler),
					ActorRef.noSender());

			oc._actorRef = fieldsCompiler;

		}// for outs

		pm.computeChildrens();
		pm.compactAndExtractOthers();

		ActorRef resultActor = pm.getOrCreateActorRef(sys, pm.mainStream,
				flowRegulator);

		Class parsingSubSystemClass = XMLParsingSubSystemActor.class;

		String lowerCaseFileName = osmInputFile.getName().toLowerCase();
		if (lowerCaseFileName.endsWith(".pbf")) {
			System.out.println("Using pbf parser subsystem");
			parsingSubSystemClass = PbfParsingSubSystemActor.class;
		} else {
			System.out.println("Use xml parsing subsystem");
		}

		ActorRef parsingSubSystem = sys.actorOf(Props.create(
				parsingSubSystemClass, flowRegulator, resultActor));
		flowRegulator.tell(new MessageRegulatorRegister(parsingSubSystem),
				ActorRef.noSender());

		// init the reading
		parsingSubSystem.tell(MessageParsingSystemStatus.INITIALIZE,
				ActorRef.noSender());

		// wait a bit
		Thread.sleep(2000);

		System.out.println("launch the reading");
		parsingSubSystem.tell(new MessageReadFile(osmInputFile),
				ActorRef.noSender());

		sys.awaitTermination();

	}

}

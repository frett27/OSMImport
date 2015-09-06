package com.osmimport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fgdbapi.thindriver.TableHelper;
import org.fgdbapi.thindriver.swig.FGDBJNIWrapper;
import org.fgdbapi.thindriver.swig.Geodatabase;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.osmimport.output.CSVFolderReference;
import com.osmimport.output.GDBReference;
import com.osmimport.output.OutCell;
import com.osmimport.output.OutSink;
import com.osmimport.output.ProcessModel;
import com.osmimport.output.Stream;
import com.osmimport.output.actors.csv.CSVOutputActor;
import com.osmimport.output.actors.gdb.ChainCompiler;
import com.osmimport.output.actors.gdb.ChainCompiler.ValidateResult;
import com.osmimport.output.actors.gdb.CompiledTableOutputActor;
import com.osmimport.output.actors.gdb.FieldsCompilerActor;
import com.osmimport.output.model.FeatureClass;
import com.osmimport.output.model.Field;
import com.osmimport.output.model.FieldType;
import com.osmimport.output.model.Table;
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

	/**
	 * reference destinations by path
	 */
	private Map<String, OutDestination> outDestinations;

	/**
	 * the process model
	 */
	private ProcessModel pm;

	/**
	 * load and copile the import script
	 * 
	 * @param script
	 * @param additionalVariables
	 * @throws Exception
	 */
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

	// base class for out destination
	private static abstract class OutDestination {

	}

	// geodatabase destination
	private static class OpenedGeodatabase extends OutDestination {
		public Geodatabase geodatabase;
		public Map<String, org.fgdbapi.thindriver.swig.Table> tables = new HashMap<>();
	}

	private static class TableOutputStream {
		public Table table;
		public OutputStream outputStream;
	}

	// csv folder destination
	private static class CsvFolder extends OutDestination {
		public File folder;
		public Map<String, TableOutputStream> files = new HashMap<>();
	}

	/**
	 * create the needed tabled, and open all files
	 * 
	 * @param pm
	 *            the processing model
	 * 
	 * @return a hash with all opened outSinks
	 * @throws Exception
	 */
	private void openOutputDestinations() throws Exception {

		assert pm != null;

		Map<String, OutDestination> g = new HashMap<String, OutDestination>();

		for (OutCell oc : pm.outs) {

			// get output
			OutSink r = oc.sink;
			String path = r.getPath();

			if (!g.containsKey(path)) {

				OutDestination outDest = null;

				if (r instanceof GDBReference) {

					// geodatabase as not been created yet

					OpenedGeodatabase og = new OpenedGeodatabase();
					outDest = og;

					System.out.println("create geodatabase " + path);

					// create the GDB
					Geodatabase geodatabase = FGDBJNIWrapper
							.createGeodatabase(path);
					og.geodatabase = geodatabase;
					for (Table t : r.listTables()) {

						TableHelper h = Tools.convertTable(t);

						String tableDef = h.buildAsString();
						System.out.println("creating table " + h.getName()
								+ " with definition : \n" + tableDef);

						org.fgdbapi.thindriver.swig.Table newTable = geodatabase
								.createTable(tableDef, "");

						System.out.println("table " + h.getName() + " created");

						// closing table to be sure the definition is correctly
						// stored

						geodatabase.closeTable(newTable);

						System.out.println("open the table " + h.getName());

						newTable = geodatabase.openTable(h.getName());

						og.tables.put(h.getName(), newTable);

						System.out.println("successfully created");

					} // for

				} else if (r instanceof CSVFolderReference) {

					// work on the CSV reference
					CsvFolder ref = new CsvFolder();

					File folder = new File(path);

					// create the file
					if (!folder.exists()) {
						folder.mkdirs();
						if (!folder.exists()) {
							throw new Exception("folder "
									+ folder.getAbsolutePath()
									+ " cannot be created");
						}
					}

					ref.folder = folder;

					for (Table t : r.listTables()) {
						TableOutputStream c = new TableOutputStream();
						c.table = t;

						File outFile = new File(folder, t.getName() + ".csv");

						OutputStream fs = new BufferedOutputStream(
								new FileOutputStream(outFile));

						c.outputStream = fs;

						ref.files.put(t.getName(), c);

					}

					outDest = ref;

				} else {
					throw new Exception();
				}

				assert outDest != null;
				g.put(path, outDest);

			}

		}// for outs

		this.outDestinations = g;
	}

	

	/**
	 * main procedure
	 * 
	 * @param osmInputFile
	 *            the input file
	 * @throws Exception
	 */
	public void run(File osmInputFile) throws Exception {

		assert pm != null;
		// constructing the actor system

		Config config = ConfigFactory.load();

		Config osmclusterconfig = config.getConfig("osmcluster");

		ActorSystem sys = ActorSystem.create("osmcluster", osmclusterconfig);

		long eventbuffer = osmclusterconfig.getLong("eventbuffer");
		// scale for the number of available processors
		eventbuffer = eventbuffer * Runtime.getRuntime().availableProcessors() / 4;
		
		ActorRef flowRegulator = sys.actorOf(Props.create(FlowRegulator.class,
				"output", eventbuffer));

		openOutputDestinations();

		// for each out, create the output actor
		for (OutCell oc : pm.outs) {

			// get output
			OutSink r = oc.sink;
			String path = r.getPath();

			if (!outDestinations.containsKey(path)) {
				throw new Exception("geodatabase " + path + " not found");
			}

			OutDestination outDest = outDestinations.get(path);

			if (outDest instanceof OpenedGeodatabase) {

				OpenedGeodatabase openedGeodatabase = (OpenedGeodatabase) outDest;

				org.fgdbapi.thindriver.swig.Table table = openedGeodatabase.tables
						.get(oc.tablename);

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

				ActorRef fieldsCompiler = sys.actorOf(Props.create(
						FieldsCompilerActor.class, table,
						tableCompiledOutputActor), Tools.toActorName("FC__"
						+ keyname));
				flowRegulator.tell(
						new MessageRegulatorRegister(fieldsCompiler),
						ActorRef.noSender());

				oc._actorRef = fieldsCompiler;

			} else if (outDest instanceof CsvFolder) {

				// //
				CsvFolder f = (CsvFolder) outDest;
				TableOutputStream t = f.files.get(oc.tablename);

				if (t == null)
					throw new Exception("file " + oc.tablename + " not found");

				assert t.outputStream != null;
				assert t.table != null;

				String keyname = path + "_" + oc.tablename;

				// ///////////////////////////////////////////////////////////////
				// create output actors

				ActorRef csvOutputActor = sys.actorOf(
						Props.create(CSVOutputActor.class, t.table,
								t.outputStream, flowRegulator).withDispatcher(
								"pdisp"),
						// TODO prefs, refactor, for multithreaded
						// output, each table must have a separate
						// execution thread
						Tools.toActorName("CSV__" + keyname));
				flowRegulator.tell(
						new MessageRegulatorRegister(csvOutputActor),
						ActorRef.noSender());

				oc._actorRef = csvOutputActor;

			} else {
				throw new Exception("unsupported out destination :" + outDest);
			}

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

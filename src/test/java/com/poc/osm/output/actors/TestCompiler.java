package com.poc.osm.output.actors;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
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
import com.poc.osm.output.actors.StreamProcessingActor;
import com.poc.osm.output.actors.TableOutputActor;
import com.poc.osm.output.model.TableHelper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

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
								"src/test/resources/com/esrifrance/osm/output/actors/testchain.groovy"),
						osmStream);

		System.out.println(pm);

		Config config = ConfigFactory.load();

		ActorSystem sys = ActorSystem.create("osmcluster",
				config.getConfig("osmcluster"));

		Map<String, Geodatabase> geodatabaseRefs = new HashMap<String, Geodatabase>();

		for (OutCell oc : pm.outs) {

			GDBReference r = oc.gdb;
			String path = r.getPath();

			ActorRef tableOutput = null;

			Geodatabase geodatabase;
			if (!geodatabaseRefs.containsKey(path)) {
				System.out.println("create geodatabase " + path);
				// create the GDB
				geodatabase = EsriFileGdb.createGeodatabase(path);
				geodatabaseRefs.put(path, geodatabase);

				for (TableHelper h : r.listTables()) {
					String tableDef = h.buildAsString();
					System.out.println("creating table " + h.getName()
							+ " with definition : \n" + tableDef);
					Table newTable = geodatabase.createTable(tableDef, "");
					System.out.println("successfully created");

					// create the actor
					ActorRef actorOf = sys.actorOf(
							Props.create(TableOutputActor.class, newTable),
							h.getName());

					if (oc.tablename.equals(h.getName())) {
						tableOutput = actorOf;
					}

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
							s.filter, s.transform, o), s.label);
					current = current.parentStream;
				}
			}

		}// for outs

	}

}

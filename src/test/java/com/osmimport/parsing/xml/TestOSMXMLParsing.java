package com.osmimport.parsing.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;

import com.osmimport.messages.ParsingObjects;
import com.osmimport.tools.polygoncreator.ConsoleInvalidPolygonFeedBackReporter;

public class TestOSMXMLParsing extends TestCase {

	// public void testParsingFragment() throws Exception {
	//
	// OSMXmlParsing p = new OSMXmlParsing();
	//
	// InputStream is = getClass().getResourceAsStream("osmfragment.xml");
	// assertNotNull(is);
	//
	// p.parseFragment(is, new ParsingObjectsListener() {
	//
	// @Override
	// public void emit(ParsingObjects objects) {
	// System.out.println("objets received :" + objects);
	// }
	// });
	//
	// }
	
	private static void log(String message)
	{
		System.out.println(new SimpleDateFormat("DD/MM/YYYY - hh:mm:ss").format(new Date()) + " " + message);
	}

	public void testPerformance() throws Exception {

		OSMXmlParsing o = new OSMXmlParsing();
		while (true) {
			log(" parsing file once ...");
			File f = new File("C:\\projets\\osm\\rhone-alpes-latest.osm");
			FileInputStream fis = new FileInputStream(f);
			try {
				final AtomicLong ai = new AtomicLong(0);
				o.parseFile(new BufferedInputStream(fis),
						new ParsingObjectsListener() {

							@Override
							public void emit(ParsingObjects objects) {
								// System.out.println("objets received :" +
								// objects);
								if (ai.addAndGet(1) % 10000 == 0) {
									log("" + ai.get() +  " messages processed");
									
								}
							}
						});

			} finally {
				fis.close();
			}
		}
	}

}

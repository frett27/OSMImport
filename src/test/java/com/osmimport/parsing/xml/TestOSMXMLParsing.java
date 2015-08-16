package com.osmimport.parsing.xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.osmimport.messages.ParsingObjects;

import junit.framework.TestCase;

public class TestOSMXMLParsing extends TestCase {

//	public void testParsingFragment() throws Exception {
//
//		OSMXmlParsing p = new OSMXmlParsing();
//
//		InputStream is = getClass().getResourceAsStream("osmfragment.xml");
//		assertNotNull(is);
//
//		p.parseFragment(is, new ParsingObjectsListener() {
//
//			@Override
//			public void emit(ParsingObjects objects) {
//				System.out.println("objets received :" + objects);
//			}
//		});
//
//	}

	public void testPerformance() throws Exception {

		File f = new File("C:\\projets\\osm\\rhone-alpes-latest.osm");
		FileInputStream fis = new FileInputStream(f);
		try {

			OSMXmlParsing o = new OSMXmlParsing();
			o.parseFile(new BufferedInputStream(fis),
					new ParsingObjectsListener() {

						@Override
						public void emit(ParsingObjects objects) {
							System.out.println("objets received :" + objects);
						}
					});

		} finally {
			fis.close();
		}
	}

}

package com.osmimport.parsing.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.osmimport.messages.ParsingObjects;
import com.osmimport.model.OSMAttributedEntity;

public class OSMXmlParsing {

//	/**
//	 * parse a fragment of XML (may not have a root element)
//	 * 
//	 * @param is
//	 *            input stream
//	 * @throws Exception
//	 */
//	public void parseFragment(InputStream is) throws Exception {
//
//		assert is != null;
//
//		SAXParser parser = createParser();
//
//		// add a fake root for parsing with sax
//		Enumeration<InputStream> streams = Collections.enumeration(Arrays
//				.asList(new InputStream[] {
//						new ByteArrayInputStream("<root>".getBytes()), is,
//						new ByteArrayInputStream("</root>".getBytes()), }));
//
//		SequenceInputStream seqStream = new SequenceInputStream(streams);
//
//		parser.parse(seqStream, new SaxOSMXMLHandler(0,
//				new ParsingObjectsListener() {
//					@Override
//					public void emit(ParsingObjects objects) {
//						System.out.println("constructed objects :" + objects);
//					}
//				}));
//
//	}

	/**
	 * create the instance of SAXParser
	 * 
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	protected SAXParser createParser() throws Exception {

		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

		SAXParser parser = saxParserFactory.newSAXParser();

		return parser;
	}

	/**
	 * parse the XML with a root element
	 * 
	 * @param is
	 * @throws Exception
	 */
	public void parseFile(InputStream is, ParsingObjectsListener listener)
			throws Exception {
		assert is != null;
		SAXParser parser = createParser();
		SaxOSMXMLHandler handler = new SaxOSMXMLHandler(0, listener);
		parser.parse(is, handler);
		handler.flush();
	}

}

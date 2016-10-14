package com.osmimport.input.csv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import junit.framework.TestCase;

import com.osmimport.model.OSMAttributedEntity;
import com.osmimport.structures.model.Table;

public class TestCSVParser extends TestCase {

	public void testQuote() throws Exception {
		Table t = new Table("toto");
		t.addIntegerField("t");
		CSVParser p = new CSVParser(t, new ParserCallBack() {

			@Override
			public void lineParsed(long lineNumber, OSMAttributedEntity entity) {
				// TODO Auto-generated method stub

			}

			@Override
			public void invalidLine(long lineNumber, String line) {
				// TODO Auto-generated method stub

			}
		});

		StringBuilder sb = new StringBuilder("\"toto titi\"");
		String r = p.parseStringWithQuotes(sb);
		assertEquals(r, "toto titi");

		sb = new StringBuilder("\"toto titi\", ");
		r = p.parseStringWithQuotes(sb);
		assertEquals("toto titi", r);
		assertEquals(" ", sb.toString());

		sb = new StringBuilder("\"toto titi, ");
		r = p.parseStringWithQuotes(sb);
		assertEquals("toto titi, ", r);
		assertEquals("", sb.toString());

		sb = new StringBuilder("\"toto ,titi\", ");
		r = p.parseStringWithQuotes(sb);
		assertEquals("toto ,titi", r);
		assertEquals(" ", sb.toString());

		sb = new StringBuilder("\"toto titi\",\"titi\" ");
		r = p.parseStringWithQuotes(sb);
		String r2 = p.parseStringWithQuotes(sb);

		assertEquals("toto titi", r);
		assertEquals("titi", r2);

		assertEquals("", sb.toString());

		// ////// test readField
		
		sb = new StringBuilder("\"toto \"\"titi\",\"titi\" ");
		r = p.parseStringWithQuotes(sb);
		r2 = p.parseStringWithQuotes(sb);
		assertEquals("toto \"titi", r);

	}

	public void testReadField() throws Exception {
		Table t = new Table("toto");
		t.addIntegerField("t");
		CSVParser p = new CSVParser(t, new ParserCallBack() {

			@Override
			public void lineParsed(long lineNumber, OSMAttributedEntity entity) {
			}

			@Override
			public void invalidLine(long lineNumber, String line) {
			}
		});

		StringBuilder sb = new StringBuilder("toto titi,2486 ");
		String r = p.readNextField(sb);
		assertEquals("toto titi", r);
		assertEquals("2486 ", sb.toString());

		r = p.readNextField(sb);
		assertEquals("2486 ", r);
		assertEquals("", sb.toString());

	}

	public void testIntegration() throws Exception {
		Table t = new Table("toto");
		t.addIntegerField("t");
		CSVParser p = new CSVParser(t, new ParserCallBack() {

			@Override
			public void lineParsed(long lineNumber, OSMAttributedEntity entity) {
				// TODO Auto-generated method stub

			}

			@Override
			public void invalidLine(long lineNumber, String line) {
				// TODO Auto-generated method stub

			}
		});

		StringBuilder sb = new StringBuilder("toto titi,2486 ");
		for (int i = 0; i < 10; i++) {
			String r = p.readNextField(sb);
			System.out.println("-->" + r + "<--");
		}
	}

	public void testPerfs() throws Exception {

		Table t = new Table("toto");
		t.addIntegerField("t");
		CSVParser p = new CSVParser(t, new ParserCallBack() {

			@Override
			public void lineParsed(long lineNumber, OSMAttributedEntity entity) {
				// TODO Auto-generated method stub

			}

			@Override
			public void invalidLine(long lineNumber, String line) {
				// TODO Auto-generated method stub

			}
		});

		FileInputStream fileInputStream = new FileInputStream(
				new File(
						"C:\\projets\\OSMImport\\build\\distributions\\osmimport-0.5-SNAPSHOT\\database\\test_FranceEntiere_31082015.gdb\\Buildings.csv"));
		InputStreamReader r = new InputStreamReader(new BufferedInputStream(
				fileInputStream));
		LineNumberReader lnr = new LineNumberReader(r);

		int cpt = 0;
		String s = null;
		while ((s = lnr.readLine()) != null) {
			StringBuilder sb = new StringBuilder(s);
			for (int i = 0; i < 10; i++) {
				String tmp = p.readNextField(sb);
				// if (cpt++ % 10000 == 0) {
				// System.out.print(" " + i + " " + tmp );
				// }
			}
			if (cpt++ % 10000 == 0) {
				System.out.println(cpt);
			}
		}

	}

}

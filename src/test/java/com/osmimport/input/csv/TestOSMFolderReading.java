package com.osmimport.input.csv;

import java.io.File;
import java.util.Map;

import org.junit.Test;

import com.osmimport.model.OSMAttributedEntity;
import com.osmimport.parsing.csv.MapStringTools;
import com.osmimport.parsing.csv.RawCSVEntitiesGenerator;
import com.osmimport.parsing.csv.SplittedFileCSVParser;
import com.osmimport.structures.model.Table;

public class TestOSMFolderReading {

	@Test
	public void testOSMReadingFolder() throws Exception {

		Table t = new Table("compositeGeometryRecord");
		t.addIntegerField("id");
		t.addStringField("geometry", 1000000);
		t.addStringField("fields", 1000000);

		ParserCallBack pcb = new ParserCallBack() {

			int cpt = 0;

			@Override
			public void lineParsed(long lineNumber, OSMAttributedEntity entity) {
				if (cpt++ < 10) {
					System.out.println(entity);

					Map<String, String> h = MapStringTools
							.fromString((String) entity.getFields().get(
									"fields"));
					System.out.println(h);
				}
				if (cpt % 100 == 0)
					System.out.println(cpt);

			}

			@Override
			public void invalidLine(long lineNumber, String line) {
				System.err.println("error line :" + lineNumber + " -> " + line);
			}
		};
		SplittedFileCSVParser parser = new SplittedFileCSVParser(t, pcb);
		parser.parse(new File("C:\\temp\\flink_rhone\\ways.csv"));
	}

	@Test
	public void testRawCSV() throws Exception {
		RawCSVEntitiesGenerator eg = new RawCSVEntitiesGenerator(
				new ParserCallBack() {
					int cpt = 0;

					@Override
					public void lineParsed(long lineNumber,
							OSMAttributedEntity entity) throws Exception {
						if (cpt++ % 1000 == 0)
							System.out.println(cpt);
						// System.out.println(entity);
					}

					@Override
					public void invalidLine(long lineNumber, String line) {
						System.err.println("invalidLine " + lineNumber + " :"
								+ line);
					}
				});

		eg.parse(new File("C:\\temp\\flink_rhone"));
	}

}

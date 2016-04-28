package com.osmimport.input.csv;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.osmimport.model.OSMAttributedEntity;
import com.osmimport.model.OSMEntityGeometry;
import com.osmimport.structures.model.FeatureClass;
import com.osmimport.structures.model.Field;
import com.osmimport.structures.model.Table;

/**
 * naive parser for CSV files using a structure table
 * 
 * @author use
 * 
 */
public class CSVParser {

	ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
			.getLogger(CSVParser.class);

	private Table table;
	
	private ParserCallBack pcb;

	/**
	 * constructor, take the table structure (either the table or featureclass)
	 * 
	 * @param table
	 * @param is
	 *            the input stream
	 */
	public CSVParser(Table table, ParserCallBack pcb) {
		assert table != null;
		this.table = table;
		assert pcb != null;
		this.pcb = pcb;

		assert table.getFields() != null;
		assert table.getFields().size() > 0;

	}

	public void parse(InputStream is) throws Exception {

		InputStreamReader r = new InputStreamReader(is);
		LineNumberReader lnr = new LineNumberReader(r);
		String currentLine = null;

		Field[] fields = table.getFieldsRef().toArray(new Field[0]);

		while ((currentLine = lnr.readLine()) != null) {

			StringBuilder sb = new StringBuilder(currentLine);
			OSMAttributedEntity ae = null;

			try {
				Geometry geometry = null;

				HashMap<String, Object> fldsValue = new HashMap<String, Object>();

				for (Field f : fields) {

					String s = readNextField(sb);

					switch (f.getType()) {

					case DOUBLE:
						if (s != null && !"".equals(s)) {
							try {
								fldsValue.put(f.getName(),
										Double.parseDouble(s));
							} catch (Exception ex) {
								logger.debug("fail to parse double {}", s);
							}
						}
						break;
					case GEOMETRY:

						if (s != null && !"".equals(s)) {
							try {
								byte[] b = Base64.decodeBase64(s);
								FeatureClass fc = (FeatureClass) table;
								geometry = GeometryEngine
										.geometryFromEsriShape(b,
												fc.getESRIGeomType());

							} catch (Exception ex) {
								logger.info("line " + lnr.getLineNumber()
										+ ", on field " + f.getName()
										+ " -> fail to parse Double " + s);
							}
						}
						break;
					case INTEGER:
						if (s != null && !"".equals(s)) {
							try {
								fldsValue.put(f.getName(), Integer.parseInt(s));
							} catch (Exception ex) {
								logger.info("line " + lnr.getLineNumber()
										+ ", on field " + f.getName()
										+ " -> fail to parse Integer " + s);
							}
						}
						break;
					case LONG:
						if (s != null && !"".equals(s)) {
							try {
								fldsValue.put(f.getName(), Long.parseLong(s));
							} catch (Exception ex) {
								logger.info(
										"line {}, on field {} -> fail to parse Integer {}",
										lnr.getLineNumber(), f.getName(), s);
							}
						}
						break;
					case STRING:
						if (s != null) {
							fldsValue.put(f.getName(), s);
						}
					}
				}

				if (table instanceof FeatureClass) {
					ae = new OSMEntityGeometry(0, geometry, fldsValue);
				} else {
					ae = new OSMAttributedEntity(0, fldsValue);
				}
			} catch (Exception ex) {
				if (logger.isDebugEnabled()) {
					logger.debug(ex.getMessage(), ex);
				}
				pcb.invalidLine(lnr.getLineNumber(), currentLine);
			}

			pcb.lineParsed(lnr.getLineNumber(), ae);

		}

	}

	public String readNextField(StringBuilder sb) throws Exception {

		if (sb.length() == 0)
			return "";

		if (sb.charAt(0) == '"') {
			return parseStringWithQuotes(sb);
		}

		return parseCSVField(sb);

	}

	public String parseCSVField(StringBuilder sb) throws Exception {

		StringBuilder s = new StringBuilder();

		for (int i = 0; i < sb.length(); i++) {
			char c = sb.charAt(i);

			if (c == ',') {
				sb = sb.replace(0, sb.length(),
						sb.substring(i + (i == sb.length() ? 0 : 1)));
				return s.toString();
			} else {
				s.append(c);
			}
		}

		sb = sb.replace(0, sb.length(), "");
		return s.toString();

	}

	public String parseStringWithQuotes(StringBuilder sb) throws Exception {
		StringBuilder s = new StringBuilder();
		assert sb.length() > 0;
		assert sb.charAt(0) == '"';

		int state = 0;
		// 1 " recognize
		// 2 " recognized and perhaps a comma, of empty string
		for (int i = 1; i < sb.length(); i++) {
			char c = sb.charAt(i);
			if (c == '"' && state == 0) {
				state = 1; // one recognized
			} else if (c == '"' && state == 1) {
				state = 2; // two recognized
			} else if (c == ' ' && state == 2) {
				// wait, don't emit
			} else if ((c == ',' || i == sb.length() - 1) && state == 2) {
				// end
				s.append('"');
				sb = sb.replace(0, sb.length(),
						sb.substring(i + (i == sb.length() ? 0 : 1)));
				return s.toString();

			} else if ((c == ',' || i == sb.length() - 1) && state == 1) {

				sb = sb.replace(0, sb.length(),
						sb.substring(i + (i == sb.length() ? 0 : 1)));
				return s.toString();

			} else {
				s.append(c);
			}

		}

		sb = sb.replace(0, sb.length(), "");
		return s.toString();
	}

}

package com.osmimport.parsing.csv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.GeometryEngine;
import com.osmimport.input.csv.ParserCallBack;
import com.osmimport.model.OSMAttributedEntity;
import com.osmimport.model.OSMEntityGeometry;
import com.osmimport.model.OSMEntityPoint;
import com.osmimport.model.OSMRelatedObject;
import com.osmimport.model.OSMRelation;
import com.osmimport.structures.model.Table;

public class RawCSVEntitiesGenerator {

	private static final String ATTRIBUTES_F = "attributes";
	private static final String GEOMETRY_F = "geometry";
	private static final String RELS = "rels";
	private static final String Y_F = "y";
	private static final String X_F = "x";
	private static final String ID_F = "id";
	private ParserCallBack output;

	public RawCSVEntitiesGenerator(ParserCallBack output) {
		assert output != null;
		this.output = output;
	}

	protected void parse(File file, boolean hasGeometry, final boolean isPoint, final boolean isLine,
			final boolean isPolygon, final boolean isRels) throws Exception {

		assert file.exists();

		// define the structure read in the CSV
		Table t = new Table("osmentities");

		// always an id
		t.addLongField(ID_F);
		if (isPoint) {
			t.addDoubleField(X_F);
			t.addDoubleField(Y_F);

		} else if (isPolygon || isLine) {
			t.addStringField(GEOMETRY_F, -1);
		}

		t.addStringField(ATTRIBUTES_F, -1);

		if (isRels) {
			// after attributes, we have the rels definition
			t.addStringField(RELS, 10000);
		}

		SplittedFileCSVParser parser = new SplittedFileCSVParser(t, new ParserCallBack() {

			@Override
			public void lineParsed(long lineNumber, OSMAttributedEntity entity) throws Exception {

				OSMAttributedEntity e = null;

				Map<String, Object> flds = entity.getFields();

				// read attributes
				String attributeString = (String) flds.get(ATTRIBUTES_F);
				assert attributeString != null;
				Map<String, String> h = MapStringTools.fromString(attributeString);

				if (flds == null) {
					return;
				}

				// read id
				Long id = (Long) (flds.get(ID_F));

				if (id == null)
					return;

				if (isPoint) {

					Double xfield = (Double) flds.get(X_F);
					Double yfield = (Double) flds.get(Y_F);

					if (xfield == null || yfield == null) {
						throw new Exception("null x or y for " + entity + " cannot import this line");
					}

					e = new OSMEntityPoint(id, xfield, yfield, (Map) h);
				} else if (isPolygon || isLine) {

					byte[] b = GeometryTools.fromAscii((String) flds.get(GEOMETRY_F));

					Geometry g = null;
					if (b != null)
						g = GeometryEngine.geometryFromEsriShape(b, isPolygon ? Type.Polygon : Type.Polyline);

					e = new OSMEntityGeometry(id, g, (Map) h);

				} else if (isRels) {

					// change the object definition
					String relsDefinition = (String) flds.get(RELS);
					String[] allRels = relsDefinition.split("\\|\\|");

					if (allRels == null)
						allRels = new String[0];
					List<OSMRelatedObject> l = new ArrayList<>();
					for (String related : allRels) {
						try {
							if (related == null || related.isEmpty())
								continue;
							
							Map<String, String> hrels = MapStringTools.fromString(related);

							String rrelsid = hrels.get("relid");
							String rrole = hrels.get("role");
							String rtype = hrels.get("type");

							l.add(new OSMRelatedObject(Long.parseLong(rrelsid), rrole, rtype));
							
						} catch (Exception ex) {
							output.invalidLine(lineNumber, " rel " + related + " cannot be parsed -> " + ex.getMessage());
						}
					}

					output.lineParsed(lineNumber, new OSMRelation(id, flds, l));
					return;

				} else {

					throw new RuntimeException("bad record");
				}

				// emit result
				output.lineParsed(lineNumber, e);

			}

			@Override
			public void invalidLine(long lineNumber, String line) {
				output.invalidLine(lineNumber, line);
			}
		});

		parser.parse(file);
	}

	public void parse(final File folder) throws Exception {

		ExecutorService p = Executors.newCachedThreadPool();

		p.execute(new Runnable() {
			@Override
			public void run() {
				try {
					parse(new File(folder, "rels.csv"), true, false, false, false, true);
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		});

		p.execute(new Runnable() {
			@Override
			public void run() {
				try {
					parse(new File(folder, "nodes.csv"), true, true, false, false, false);
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		});

		p.execute(new Runnable() {
			@Override
			public void run() {
				try {
					parse(new File(folder, "ways.csv"), true, false, true, false, false);
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		});
		p.execute(new Runnable() {
			@Override
			public void run() {
				try {
					parse(new File(folder, "polygons.csv"), true, false, false, true, false);
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		});

		p.shutdown();
		p.awaitTermination(10, TimeUnit.DAYS);

	}

}

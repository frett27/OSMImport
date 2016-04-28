package com.osmimport.parsing.csv;

import java.io.File;
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
import com.osmimport.structures.model.Table;

public class RawCSVEntitiesGenerator {

	private static final String ATTRIBUTES_F = "attributes";
	private static final String GEOMETRY_F = "geometry";
	private static final String Y_F = "y";
	private static final String X_F = "x";
	private static final String ID_F = "id";
	private ParserCallBack output;

	public RawCSVEntitiesGenerator(ParserCallBack output) {
		assert output != null;
		this.output = output;
	}

	protected void parse(File file, boolean hasGeometry, final boolean isPoint,
			final boolean isLine, final boolean isPolygon) throws Exception {

		assert file.exists();

		Table t = new Table("osmentities");
		t.addLongField(ID_F);
		if (isPoint) {
			t.addDoubleField(X_F);
			t.addDoubleField(Y_F);

		} else {
			t.addStringField(GEOMETRY_F, -1);
		}

		t.addStringField(ATTRIBUTES_F, -1);

		SplittedFileCSVParser parser = new SplittedFileCSVParser(t,
				new ParserCallBack() {

					@Override
					public void lineParsed(long lineNumber,
							OSMAttributedEntity entity) throws Exception {

						OSMAttributedEntity e = null;

						Map<String, String> h = MapStringTools
								.fromString((String) entity.getFields().get(
										ATTRIBUTES_F));

						Long id = (Long) (entity.getFields().get(ID_F));

						if (isPoint) {
							e = new OSMEntityPoint(id, (Double) entity
									.getFields().get(X_F), (Double) entity
									.getFields().get(Y_F), (Map) h);
						} else if (isPolygon || isLine) {

							byte[] b = GeometryTools.fromAscii((String) entity
									.getFields().get(GEOMETRY_F));

							Geometry g = GeometryEngine
									.geometryFromEsriShape(b,
											isPolygon ? Type.Polygon
													: Type.Polyline);

							e = new OSMEntityGeometry(id, g, (Map) h);
						} else {

							throw new RuntimeException("bad record");
						}

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
					parse(new File(folder, "nodes.csv"), true, true, false,
							false);
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		});

		p.execute(new Runnable() {
			@Override
			public void run() {
				try {
					parse(new File(folder, "ways.csv"), true, false, true,
							false);
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		});
		p.execute(new Runnable() {
			@Override
			public void run() {
				try {
					parse(new File(folder, "polygons.csv"), true, false, false,
							true);
				} catch (Exception ex) {
					ex.printStackTrace(System.err);
				}
			}
		});

		p.shutdown();
		p.awaitTermination(10, TimeUnit.DAYS);

	}

}

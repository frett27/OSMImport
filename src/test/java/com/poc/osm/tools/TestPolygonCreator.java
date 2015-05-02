package com.poc.osm.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.codehaus.jackson.JsonParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.poc.osm.parsing.model.PolygonToConstruct.Role;

public class TestPolygonCreator extends TestCase {

	protected MultiPath createMultiPath(double[][] path) {

		Polyline p = new Polyline();

		boolean first = true;
		for (double[] coords : path) {
			assert coords.length == 2;
			if (first) {
				p.startPath(coords[0], coords[1]);
				first = false;
			} else {
				p.lineTo(coords[0], coords[1]);
			}
		}

		return p;

	}

	public void dump(MultiPath p) {
		assert p != null;

		String jsong = GeometryEngine.geometryToJson(4623, p);
		System.out.println(jsong);
	}

	public void testSimpleCloseMultiPath() throws Exception {

		MultiPath p1 = createMultiPath(new double[][] { { 0, 0 }, { 0, 1 } });
		MultiPath p2 = createMultiPath(new double[][] { { 0, 1 }, { 1, 1 },
				{ 0, 0 } });

		Polygon r = PolygonCreator.createPolygon(new MultiPath[] { p1, p2 },
				new Role[] { Role.OUTER, Role.OUTER });

		dump(r);
	}

	public void testSimpleCloseMultiPath2() throws Exception {

		MultiPath p1 = createMultiPath(new double[][] { { 0, 0 }, { 0, 1 } });
		MultiPath p2 = createMultiPath(new double[][] { { 0, 1 }, { 1, 1 } });
		MultiPath p3 = createMultiPath(new double[][] { { 1, 1 }, { 1, 0 } });
		MultiPath p4 = createMultiPath(new double[][] { { 1, 0 }, { 0, 0 } });

		Polygon r = PolygonCreator.createPolygon(new MultiPath[] { p1, p2, p3,
				p4 }, new Role[] { Role.OUTER, Role.OUTER, Role.OUTER,
				Role.OUTER });

		dump(r);
	}

	public void testSimpleCloseReversedMultiPath() throws Exception {

		MultiPath p1 = createMultiPath(new double[][] { { 0, 0 }, { 0, 1 } });
		MultiPath p2 = createMultiPath(new double[][] { { 0, 1 }, { 1, 1 },
				{ 0, 0 } });
		p2.reverseAllPaths();

		Polygon r = PolygonCreator.createPolygon(new MultiPath[] { p1, p2 },
				new Role[] { Role.OUTER, Role.OUTER });

		dump(r);
	}

	public void testOnlyOne() throws Exception {
		MultiPath p1 = createMultiPath(new double[][] { { 0, 0 }, { 0, 1 },
				{ 0, 1 }, { 1, 1 }, { 0, 0 } });

		Polygon r = PolygonCreator.createPolygon(new MultiPath[] { p1 },
				new Role[] { Role.OUTER });

		dump(r);
	}

	public void testInner() throws Exception {
		MultiPath p1 = createMultiPath(new double[][] { { 0, 0 }, { 0, 1 },
				{ 1, 1 }, { 0, 0 } });
		MultiPath p2 = createMultiPath(new double[][] { { 0, 0.5 },
				{ 0.5, 0.5 }, { 0, 0.5 } });

		Polygon r = PolygonCreator.createPolygon(new MultiPath[] { p1, p2 },
				new Role[] { Role.OUTER, Role.INNER });

		dump(r);
	}

	public void testCase1() throws Exception {

		MultiPath p1 = parse(" {\"paths\":[[[4.8904140000000010,44.931376700000000],[4.8899489000000000,44.931489600000006]]],\"spatialReference\":{\"wkid\":4623}}");

		MultiPath p2 = (MultiPath) GeometryEngine
				.jsonToGeometry(
						"{\"paths\":[[[4.8902554000000000,44.931894500000006],[4.8899489000000000,44.931489600000006]]],\"spatialReference\":{\"wkid\":4623}}")
				.getGeometry();

		MultiPath p3 = (MultiPath) GeometryEngine
				.jsonToGeometry(
						"{\"paths\":[[[4.8905370000000000,44.931818500000006],[4.8902554000000000,44.931894500000006]]],\"spatialReference\":{\"wkid\":4623}}")
				.getGeometry();

		MultiPath p4 = (MultiPath) GeometryEngine
				.jsonToGeometry(
						"{\"paths\":[[[4.8904765000000000,44.931601200000000],[4.8905370000000000,44.931818500000006]]],\"spatialReference\":{\"wkid\":4623}}")
				.getGeometry();

		MultiPath p5 = (MultiPath) GeometryEngine
				.jsonToGeometry(
						"{\"paths\":[[[4.8904140000000010,44.931376700000000],[4.8904765000000000,44.931601200000000]]],\"spatialReference\":{\"wkid\":4623}}")
				.getGeometry();

		Polygon r = PolygonCreator.createPolygon(new MultiPath[] { p1, p2, p3,
				p4, p5 }, new Role[] { Role.OUTER, Role.OUTER, Role.OUTER,
				Role.OUTER, Role.OUTER });

		dump(r);

	}

	public void testCase2() throws Exception {

		MultiPath p1 = parse("{\"paths\":[[[4.8904140000000010,44.931376700000000],[4.8904765000000000,44.931601200000000]]],\"spatialReference\":{\"wkid\":4623}}");

		MultiPath p2 = parse("{\"paths\":[[[4.8904765000000000,44.931601200000000],[4.8908921000000000,44.931528000000000],[4.8909855000000000,44.931537200000000]]],\"spatialReference\":{\"wkid\":4623}}");

		MultiPath p3 = parse("{\"paths\":[[[4.8909855000000000,44.931537200000000],[4.8910356000000000,44.931402600000006],[4.8910929000000000,44.931321600000004],[4.8908259000000000,44.931205800000000]]],\"spatialReference\":{\"wkid\":4623}}");

		MultiPath p4 = parse("{\"paths\":[[[4.8908259000000000,44.931205800000000],[4.8906546000000000,44.931293800000006],[4.8904140000000010,44.931376700000000]]],\"spatialReference\":{\"wkid\":4623}}");

		Polygon r = PolygonCreator.createPolygon(new MultiPath[] { p1, p2, p3,
				p4 }, new Role[] { Role.OUTER, Role.OUTER, Role.OUTER,
				Role.OUTER });

		dump(r);

	}

	private MultiPath parse(String a) throws JsonParseException, IOException {
		MultiPath p1 = (MultiPath) GeometryEngine.jsonToGeometry(a)
				.getGeometry();
		return p1;
	}

	public void testFileCase2() throws Exception {

		// BasicConfigurator.configureDefaultContext();

		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ALL);

		InputStream is = getClass().getResourceAsStream("case2.json");
		assertNotNull(is);
		JSONTokener t = new JSONTokener(new InputStreamReader(is));
		JSONObject o = new JSONObject(t);

		Object a = o.get("origin");

		JSONArray arr = (JSONArray) a;

		MultiPath[] paths = new MultiPath[arr.length()];
		Role[] roles = new Role[arr.length()];

		for (int i = 0; i < arr.length(); i++) {
			JSONObject element = (JSONObject) arr.get(i);
			paths[i] = (MultiPath) GeometryEngine.jsonToGeometry(
					element.get("geometry").toString()).getGeometry();

			roles[i] = Role.valueOf(element.getString("role").toString());
		}

		PolygonCreator.createPolygon(paths, roles);

	}

}

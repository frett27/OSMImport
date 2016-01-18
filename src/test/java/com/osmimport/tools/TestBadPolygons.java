package com.osmimport.tools;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Polyline;
import com.osmimport.parsing.model.PolygonToConstruct.Role;
import com.osmimport.tools.polygoncreator.MultiPathAndRole;
import com.osmimport.tools.polygoncreator.PolygonCreator;

public class TestBadPolygons extends TestCase {

	/**
	 * @throws Exception
	 */
	public void dumpAllElements(String casetest) throws Exception {
		JSONArray ga = readCaseGeometriesAsJson(casetest);

		for (int i = 0; i < ga.length(); i++) {
			JSONObject e = (JSONObject) ga.get(i);

			JSONObject jg = (JSONObject) e.get("geometry");
			jg.put("type", "polyline");

			MapGeometry p = GeometryEngine.jsonToGeometry(jg.toString());

			Polyline g = (Polyline) p.getGeometry();
			System.out.println("" + i + " , start :" + g.getPoint(0) + " -> "
					+ g.getPoint(g.getPointCount() - 1));

		}

	}

	/**
	 * read all the geometries from the recorded bad tests
	 * 
	 * @param casetest
	 * @return
	 * @throws JSONException
	 */
	public JSONArray readCaseGeometriesAsJson(String casetest)
			throws JSONException {
		InputStream is = getClass().getResourceAsStream(casetest);
		assert is != null;
		JSONTokener t = new JSONTokener(new InputStreamReader(is));
		JSONObject o = new JSONObject(t);

		JSONArray ga = (JSONArray) o.get("origin");
		System.out.println(ga);
		return ga;
	}

	/**
	 * read the case geometry and return the list of objects
	 * 
	 * @param caseTest
	 * @return
	 * @throws Exception
	 */
	public MultiPathAndRole[] readCaseGeometries(String caseTest)
			throws Exception {
		JSONArray j = readCaseGeometriesAsJson(caseTest);

		ArrayList<MultiPathAndRole> l = new ArrayList<>();

		for (int i = 0; i < j.length(); i++) {
			JSONObject o = (JSONObject) j.get(i);
			Role r = Role.valueOf(o.getString("role"));
			MapGeometry g = GeometryEngine.jsonToGeometry(o
					.getString("geometry"));
			l.add(new MultiPathAndRole((Polyline) g.getGeometry(), r));
		}

		return l.toArray(new MultiPathAndRole[0]);
	}

	/**
	 * launch the constructcase on the given case
	 * 
	 * @param caseTest
	 *            the case name
	 * @throws Exception
	 */
	public void testConstructCase(String caseTest) throws Exception {

		MultiPathAndRole[] elts = readCaseGeometries(caseTest);

		PolygonCreator pc = new PolygonCreator();

		pc.createPolygon(new ArrayList<MultiPathAndRole>(Arrays.asList(elts)));

	}

	/**
	 * test Case 3
	 */
	public void testCase3() throws Exception {
		testConstructCase("case3.json");
	}

}

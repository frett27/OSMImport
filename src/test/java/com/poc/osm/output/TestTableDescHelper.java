package com.poc.osm.output;

import java.io.File;
import java.util.Random;

import com.esrifrance.fgdbapi.swig.EsriFileGdb;
import com.esrifrance.fgdbapi.swig.Geodatabase;
import com.esrifrance.fgdbapi.xml.DETable;
import com.esrifrance.fgdbapi.xml.EsriGeometryType;
import com.poc.osm.output.model.TableHelper;

import junit.framework.TestCase;

public class TestTableDescHelper extends TestCase {

	private Geodatabase g;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		File f = File.createTempFile("testgdb", ".gdb");
		f.delete();

		g = EsriFileGdb.createGeodatabase(f.getAbsolutePath());

		System.out.println("geodatabase created in :" + f.getAbsolutePath());

	}

	public void testCreateSimplePointFeatureClass() throws Exception {

		String definition = TableHelper.newFeatureClass("m",
				EsriGeometryType.ESRI_GEOMETRY_POINT,
				TableHelper.constructW84SpatialReference()).buildAsString();
		System.out.println(definition);

		g.createTable(definition, "");

	}

	/**
	 * creation aleatoire de champs dans une couche, en utilisant le TableHelper
	 * 
	 * @throws Exception
	 */
	public void testCreateSimplePointFeatureClassWithFields() throws Exception {

		Random r = new Random();
		for (int i = 0; i < 100; i++) {

			TableHelper th = TableHelper.newFeatureClass("fc" + i,
					EsriGeometryType.ESRI_GEOMETRY_POINT,
					TableHelper.constructW84SpatialReference());

			for (int j = 0; j < 20; j++) {

				double alea = r.nextDouble();
				if (alea < 0.1) {
					th.addIntegerField("i" + j);
				} else if (alea < 0.3) {
					th.addDoubleField("d" + j);
				} else if (alea < 0.5) {
					th.addStringField("s" + j, j);
				}
			}

			System.out.println("Creating featureclass" + "fc" + i);
			String definition = th.buildAsString();
			System.out.println(definition);

			g.createTable(definition, "");

		}

	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

}

package com.osmimport.tools.polygoncreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Polyline;

/**
 * reporter that make a per file text file with all the informations
 * 
 * @author pfreydiere
 */
public class FolderFeedbackReporter implements
		IInvalidPolygonConstructionFeedBack {

	private File logFolder;

	public FolderFeedbackReporter(File folder) throws Exception {
		assert folder != null;
		if (!folder.exists() || !folder.isDirectory())
			throw new Exception("log folder " + folder
					+ " does not exists or is not a directory");

		this.logFolder = folder;
	}

	@Override
	public void polygonCreationFeedBackReport(List<MultiPathAndRole> elements,
			String reason) throws IOException {

		String filename = new SimpleDateFormat("YYYY_MM_DD_hh_mm_ss")
				.format(new Date()) + "_" + System.nanoTime() + ".txt";
		File f = new File(logFolder, filename);
		Writer w = new OutputStreamWriter(new FileOutputStream(f),
				Charset.forName("UTF-8"));
		try {
			w.write(reason);
			w.write("\n");
			w.write("GEOMETRIES:\n");
			// write geojson objects, with property
			w.write("{");
			
			w.write("\"type\": \"FeatureCollection\", \"features\":");
			w.write("[");
			
			boolean first = true;
			for(MultiPathAndRole r:elements) {
				if (!first) {
					w.write(",");
				} else 
				{
					first = false;
				}
					
				w.write("{");
				w.write("\"type\": \"Feature\", \"geometry\":");
				Polyline p = new Polyline();
				p.add(r.getMultiPath(), false);
				w.write(GeometryEngine.geometryToGeoJson( p));
				w.write(",\"properties\":{");
				w.write("\"role\":\"" + r.getRole() + "\"");
				w.write("}");
				w.write("}");
				
			}
			
			w.write("]");
			w.write("}");
			
			// w.write(elements.toString());
		} finally {
			w.close();
		}
	}

}

package com.osmimport.parsing.model;

import java.io.Serializable;
import java.util.HashMap;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Polygon;
import com.osmimport.model.OSMEntity;
import com.osmimport.model.OSMEntityGeometry;
import com.osmimport.tools.PolygonCreator;

/**
 * relation to construct
 * 
 * @author pfreydiere
 * 
 */
public class PolygonToConstruct extends BaseEntityToConstruct implements
		Serializable {

	public enum Role {
		OUTER, INNER
	}

	private Role[] idsRole;

	public PolygonToConstruct(long id, long[] refids,
			HashMap<String, Object> fields, Role[] idsRoles) {
		super(id, refids, fields);
		this.idsRole = idsRoles;

		assert idsRoles.length == refids.length;
	}

	@Override
	public OSMEntity constructOSMEntity() {

		try {

			MultiPath[] g = new MultiPath[associatedEntity.length];
			for (int i = 0; i < g.length; i++) {
				g[i] = (MultiPath) associatedEntity[i].getGeometry();
			}
			try {

				return new OSMEntityGeometry(this.id,
						PolygonCreator.createPolygon(g, idsRole), this.fields);

			} catch (Exception ex) {
				
				System.out.println("fail to create polygon for entity " + this.id);
				ex.printStackTrace();

//				System.out.println(" Dumping faulty Polygon / Polyline :");
//				for (int j = 0; j < g.length; j++) {
//					MultiPath p = g[j];
//					System.out.println("   " + idsRole[j] + " -> " + p.getClass().getSimpleName() + " "
//							+ GeometryEngine.geometryToJson(4623, p));
//				}

				return fallBackConstructPolygon();
			}

		} catch (Exception ex) {
			System.err.println("Fail to create polygon " + this);
			ex.printStackTrace(System.err);
			throw new RuntimeException(ex.getMessage(), ex);
		}

	}

	private OSMEntity fallBackConstructPolygon() {
		Polygon polygon = new Polygon();

		for (int i = 0; i < associatedEntity.length; i++) {
			OSMEntity e = associatedEntity[i];
			assert e != null;
			polygon.add((MultiPath) e.getGeometry(), false);
		}

		return new OSMEntityGeometry(id, polygon, fields);
	}

}

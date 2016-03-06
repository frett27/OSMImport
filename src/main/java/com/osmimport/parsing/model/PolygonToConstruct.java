package com.osmimport.parsing.model;

import java.io.Serializable;
import java.util.Map;

import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Polygon;
import com.osmimport.model.OSMEntity;
import com.osmimport.model.OSMEntityGeometry;
import com.osmimport.tools.IReport;
import com.osmimport.tools.polygoncreator.IInvalidPolygonConstructionFeedBack;
import com.osmimport.tools.polygoncreator.PolygonCreator;

/**
 * relation to construct
 * 
 * @author pfreydiere
 * 
 */
public class PolygonToConstruct extends BaseEntityToConstruct implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 744578977292873100L;

	public enum Role {
		OUTER, INNER, UNDEFINED
	}

	private Role[] idsRole;


	public PolygonToConstruct(long id, long[] refids,
			Map<String, Object> currentConstructedFields, Role[] idsRoles) {
		super(id, refids, currentConstructedFields);
		this.idsRole = idsRoles;
		assert idsRoles.length == refids.length;
	}

	@Override
	public OSMEntity constructOSMEntity(IReport invalidPolygonFeedBack) {

		try {

			MultiPath[] g = new MultiPath[associatedEntity.length];
			for (int i = 0; i < g.length; i++) {
				g[i] = (MultiPath) associatedEntity[i].getGeometry();
			}
			try {
				IInvalidPolygonConstructionFeedBack f = null;
				if (invalidPolygonFeedBack instanceof IInvalidPolygonConstructionFeedBack) {
					f = (IInvalidPolygonConstructionFeedBack)invalidPolygonFeedBack;
				}

				return new OSMEntityGeometry(this.id,
						PolygonCreator.createPolygon(g, idsRole,
								f), this.fields);

			} catch (Exception ex) {

				System.err.println("bad polygon " + this.id);
				ex.printStackTrace(System.err);

				// System.out.println(" Dumping faulty Polygon / Polyline :");
				// for (int j = 0; j < g.length; j++) {
				// MultiPath p = g[j];
				// System.out.println("   " + idsRole[j] + " -> " +
				// p.getClass().getSimpleName() + " "
				// + GeometryEngine.geometryToJson(4623, p));
				// }

				return fallBackConstructPolygon();
			}

		} catch (Exception ex) {
			System.err.println("Exception in creating polygon " + this);
			ex.printStackTrace(System.err);
			throw new RuntimeException(ex.getMessage(), ex);
		}

	}

	/**
	 * add all the lines in the polygon geometry, as is
	 * 
	 * @return
	 */
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

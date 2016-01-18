package com.osmimport.parsing.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.osmimport.messages.MessageNodes;
import com.osmimport.messages.MessageRelations;
import com.osmimport.model.OSMEntityPoint;
import com.osmimport.model.OSMRelatedObject;
import com.osmimport.model.OSMRelation;
import com.osmimport.parsing.model.PolygonToConstruct;
import com.osmimport.parsing.model.PolygonToConstruct.Role;
import com.osmimport.parsing.model.WayToConstruct;
import com.osmimport.parsing.pbf.actors.messages.MessagePolygonToConstruct;
import com.osmimport.parsing.pbf.actors.messages.MessageWayToConstruct;
import com.osmimport.tools.polygoncreator.IInvalidPolygonConstructionFeedBack;

/**
 * sax handler for osm objects, a simple state machine for parsing the osm
 * objects
 * 
 * @author pfreydiere
 * 
 */
public class SaxOSMXMLHandler extends DefaultHandler {

	// //////////////////////////////////////////////////////////////
	// objects construction

	// for all objects
	private Long currentObjectId = null;
	private Map<String, Object> currentConstructedFields = null;

	// for nodes
	private Double currentLon = null;
	private Double currentLat = null;

	// for way construction
	private LongArray wayNodeReferences = null;

	// for relations
	private List<OSMRelatedObject> currentRelatedObjects = null;

	// //////////////////////////////////////////////////////////////
	// object bags

	// points
	private ArrayList<OSMEntityPoint> pts = null;

	private ArrayList<WayToConstruct> ways = null;

	private ArrayList<PolygonToConstruct> polygons = null;

	private ArrayList<OSMRelation> relations = null;

	/**
	 * listener for emitting the parsing objects
	 */
	private ParsingObjectsListener listener;

	private long blockid;

	public SaxOSMXMLHandler(long blockid, ParsingObjectsListener listener) {
		assert listener != null;
		this.listener = listener;
		this.blockid = blockid;

	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if ("tag".equals(qName)) {

			Map<String, Object> flds = currentConstructedFields;
			if (flds == null) {
				flds = new HashMap<String, Object>();
			}
			String key = attributes.getValue("k");
			String value = attributes.getValue("v");

			flds.put(key, value);

			currentConstructedFields = flds;

		} else if ("node".equals(qName)) {

			assert currentObjectId == null;

			currentObjectId = Long.parseLong(attributes.getValue("id"));
			currentLon = Double.parseDouble(attributes.getValue("lon"));
			currentLat = Double.parseDouble(attributes.getValue("lat"));

		} else if ("way".equals(qName)) {

			assert currentObjectId == null;

			long id = Integer.parseInt(attributes.getValue("id"));
			currentObjectId = id;

		} else if ("nd".equals(qName)) {
			// relation to node in a way

			if (wayNodeReferences == null) {
				wayNodeReferences = new LongArray(30);
			}

			long ref = Long.parseLong(attributes.getValue("ref"));
			wayNodeReferences.add(ref);

		} else if ("relation".equals(qName)) {
			assert currentObjectId == null;

			long id = Integer.parseInt(attributes.getValue("id"));
			currentObjectId = id;

		} else if ("member".equals(qName)) {

			// member of a relation

			long rel = Long.parseLong(attributes.getValue("ref"));
			String type = attributes.getValue("type");
			String role = attributes.getValue("role");

			if (currentRelatedObjects == null) {
				currentRelatedObjects = new ArrayList<>();
				currentRelatedObjects
						.add(new OSMRelatedObject(rel, role, type));
			}

		} else if ("root".equals(qName) || "bounds".equals(qName)
				|| "osm".equals(qName)) {
			// nothing to do, fake element
		} else {
			throw new SAXException("unexpected " + qName + " element");
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		if ("node".equals(qName)) {

			if (pts == null) {
				pts = new ArrayList<>();
			}

			assert currentLon != null;
			assert currentLat != null;
			assert currentObjectId != null;

			pts.add(new OSMEntityPoint(currentObjectId, currentLon, currentLat,
					currentConstructedFields));

			currentLon = null;
			currentLat = null;
			currentObjectId = null;

			checkFlush();

		} else if ("way".equals(qName)) {
			flushPointsCollection();
			assert wayNodeReferences != null;

			if (ways == null) {
				ways = new ArrayList<>();
			}

			ways.add(new WayToConstruct(currentObjectId, wayNodeReferences
					.getArray(), currentConstructedFields));

			// note : if this is polygon, it will be transformed by the parsing
			// phase

			currentObjectId = null;
			wayNodeReferences = null;
			currentConstructedFields = null;

			checkFlush();

		} else if ("relation".equals(qName)) {

			assert currentObjectId != null;
			if (currentRelatedObjects != null) {
				LongArray rella = new LongArray(30);
				ArrayList<Role> roles = new ArrayList<>();
				for (OSMRelatedObject o : currentRelatedObjects) {
					if ("inner".equals(o.getRole())
							|| "outer".equals(o.getRole())) {
						rella.add(o.getRelatedId());

						Role r = Role.OUTER;
						if ("inner".equals(o.getRole())) {
							r = Role.INNER;
						}

						roles.add(r);
					}

				}

				if (roles.size() > 0) {
					// emit polygon
					if (polygons == null) {
						polygons = new ArrayList<>();
					}

					Map<String, Object> polyfields = currentConstructedFields;
					if (polyfields != null) {
						polyfields = new HashMap<>();
						polyfields.putAll(currentConstructedFields);
					}

					polygons.add(new PolygonToConstruct(currentObjectId, rella
							.getArray(), polyfields, roles
							.toArray(new Role[roles.size()])));
				}
			}

			if (relations == null) {
				relations = new ArrayList<>();
			}

			relations.add(new OSMRelation(currentObjectId,
					currentConstructedFields, currentRelatedObjects));

			currentObjectId = null;
			currentConstructedFields = null;
			currentRelatedObjects = null;

			checkFlush();

		}

	}

	@Override
	public void endDocument() throws SAXException {
		flush();
	}

	public void flush() {
		flushPointsCollection();
		flushWaysCollection();
		flushPolygonsCollection();
		flushRelationsCollection();
	}

	private void flushPointsCollection() {
		if (pts != null) {
			listener.emit(new MessageNodes(pts));
			pts = null;
		}
	}

	private void flushWaysCollection() {
		if (ways != null) {
			listener.emit(new MessageWayToConstruct(blockid++, ways));
			ways = null;
		}
	}

	private void flushPolygonsCollection() {
		if (polygons != null) {
			listener.emit(new MessagePolygonToConstruct(blockid++, polygons));
			polygons = null;
		}
	}

	private void flushRelationsCollection() {
		if (relations != null) {
			listener.emit(new MessageRelations(relations));
			relations = null;
		}
	}

	int cptObject = 0;

	private void checkFlush() {
		if ((cptObject++ % 100) == 0) {
			flush();
		}
	}

}

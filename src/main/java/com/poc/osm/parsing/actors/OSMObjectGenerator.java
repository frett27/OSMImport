package com.poc.osm.parsing.actors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.BoundedMessageQueueSemantics;
import akka.dispatch.RequiresMessageQueue;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.esri.core.geometry.Point;
import com.poc.osm.messages.MessageNodes;
import com.poc.osm.model.OSMBlock;
import com.poc.osm.model.OSMContext;
import com.poc.osm.model.OSMEntity;
import com.poc.osm.model.OSMEntityGeometry;
import com.poc.osm.model.OSMEntityPoint;
import com.poc.osm.model.WayToConstruct;
import com.poc.osm.parsing.actors.messages.MessageWayToConstruct;
import com.poc.osm.regulation.MessageRegulation;

import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Way;

/**
 * This actor construct object from the OSM Blocks reading
 * 
 * @author pfreydiere
 * 
 */
public class OSMObjectGenerator extends UntypedActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private ActorRef dispatcher;
	
	private ActorRef flowRegulator;

	public OSMObjectGenerator(ActorRef dispatcher, ActorRef flowRegulator) {
		this.dispatcher = dispatcher;
		this.flowRegulator = flowRegulator;
	}

	/**
	 * Construct the objects, with fields
	 * 
	 * @param b
	 * @throws Exception
	 */
	protected void parseObjects(OSMBlock b) throws Exception {

		if (log.isDebugEnabled())
			log.debug(getSelf() + " " + System.currentTimeMillis()
					+ " handle block " + b.getCounter());

		List<OSMEntity> allNodes = constructNodesTreeSet(b);
		if (allNodes != null) {
			dispatcher.tell(new MessageNodes(allNodes), getSelf());
		}

		// emit the nodes received ...

		ArrayList<WayToConstruct> ways = constructWays(b);
		if (ways != null) {
			if (allNodes != null) {
				dispatcher.tell(
						new MessageWayToConstruct(b.getCounter(), ways),
						getSelf());
			}
		}
		
		// TODO relations
		
		// regulate
		flowRegulator.tell(new MessageRegulation(- ParsingSystemActorsConstants.RECORDS_BLOC_EQUIVALENCE ), getSelf());


	}

	/**
	 * @param b
	 * @param ws
	 */
	protected ArrayList<WayToConstruct> constructWays(OSMBlock b) {
		List<Way> ws = b.getWays();

		ArrayList<WayToConstruct> ret = null;

		if (ws != null && ws.size() > 0) {
			ret = new ArrayList<WayToConstruct>();
			for (Way w : ws) {

				long lastRef = 0;
				List<Long> l = w.getRefsList();

				long[] refids = new long[l.size()];
				// liste des references ...
				int cpt = 0;
				for (Long theid : l) {
					lastRef += theid;
					refids[cpt++] = lastRef;
				}

				OSMContext ctx = b.getContext();

				HashMap<String, Object> flds = null;

				for (int i = 0; i < w.getKeysCount(); i++) {
					String k = ctx.getStringById(w.getKeys(i));
					String v = ctx.getStringById(w.getVals(i));
					if (flds == null) {
						flds = new HashMap<String, Object>();
					}
					flds.put(k, v);
				}

				WayToConstruct wayToConstruct = new WayToConstruct(w.getId(),
						refids, flds);

				// System.out.println("polyline done !");
				// emit the way to construct
				ret.add(wayToConstruct);
			}
		}
		return ret;
	}

	/**
	 * @param b
	 */
	protected List<OSMEntity> constructNodesTreeSet(OSMBlock b) {

		List< OSMEntity> parsedNodes = null;

		OSMContext ctx = b.getContext();

		DenseNodes denseNodes = b.getDenseNodes();
		if (denseNodes != null) {

			if (parsedNodes == null) {
				parsedNodes = constructEmptyTreeMap();
			}

			long lastId = 0;
			long lastLat = 0;
			long lastLon = 0;

			int j = 0;
			for (int i = 0; i < denseNodes.getIdCount(); i++) {

				lastId += denseNodes.getId(i);
				lastLat += denseNodes.getLat(i);
				lastLon += denseNodes.getLon(i);

				// construction

				//  Optim
				
				// Point p = new Point();
				// p.setX(lastLon);
				// p.setY(lastLat);

				Map<String, Object> flds = null;

				try {
					if (denseNodes.getKeysValsCount() > 0) {
						while (denseNodes.getKeysVals(j) != 0) {
							int keyid = denseNodes.getKeysVals(j++);
							int valid = denseNodes.getKeysVals(j++);
							String k = "";
							if (keyid < ctx.getStringLength())
								k = ctx.getStringById(keyid);
							else
								System.out.println("error in encoding");

							String v = "";
							if (valid < ctx.getStringLength())
								v = ctx.getStringById(valid);
							else
								System.out.println("error in encoding");

							if (flds == null)
								flds = new HashMap<String, Object>();

							flds.put(k, v);

							// System.out.println(k + "->" + v);

						}
						j++; // Skip over the '0' delimiter.
					}
				} catch (Exception ex) {
					System.out.println("error : "+ex.getMessage());
				}
				OSMEntity o = new OSMEntityPoint(lastId, ctx.parseLon(lastLon), ctx.parseLat(lastLat), flds);
				parsedNodes.add(o);

			}
		}

		List<Node> n = b.getNodes();
		if (n != null) {

			if (parsedNodes == null) {
				parsedNodes = constructEmptyTreeMap();
			}

			for (Node thenode : n) {

				Point p = new Point();
				p.setXY(ctx.parseLon(thenode.getLon()),
						ctx.parseLat(thenode.getLat()));

				Map<String, Object> fields = null;

				for (int i = 0; i < thenode.getKeysCount(); i++) {
					int keys = thenode.getKeys(i);
					String k = ctx.getStringById(keys);
					String v = ctx.getStringById(thenode.getVals(i));
					if (fields == null) {
						fields = new HashMap<String, Object>();
					}
					fields.put(k, v);
				}

				OSMEntity o = new OSMEntityGeometry(thenode.getId(), p, fields);
				parsedNodes.add(o);

			}
		}

		// System.out.println(getSelf() + " " + System.currentTimeMillis() + " "
		// + (parsedNodes == null ? " None " : "" + parsedNodes.size())
		// + "  nodes processed");

		return parsedNodes;

	}

	/**
	 * @return
	 */
	protected List<OSMEntity> constructEmptyTreeMap() {
		return new ArrayList<OSMEntity>();
	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof OSMBlock) {
			OSMBlock nds = (OSMBlock) message;
			log.debug(nds + " Block receive in actor " + getSelf());
			parseObjects(nds);
			log.debug("block done !");
		} else {
			unhandled(message);
		}

	}

}

package com.poc.osm.tools;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.Logger;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.poc.osm.parsing.model.PolygonToConstruct.Role;

/**
 * This tool class create a multi part polygon from OSM parts
 * 
 * @author use
 * 
 */
public class PolygonCreator {
	

	public static class MultiPathAndRole {

		private MultiPath multiPath;
		private Role role;

		public MultiPathAndRole(MultiPath p, Role r) {
			this.multiPath = p;
			this.role = r;
		}

		public Role getRole() {
			return role;
		}

		public MultiPath getMultiPath() {
			return multiPath;
		}

	}

	/**
	 * create a polygon from multi path elements, passed arrays must have the
	 * same number of elements
	 * 
	 * @param multiPath
	 * @param roles
	 * @return
	 * @throws Exception
	 */
	public static Polygon createPolygon(MultiPath[] multiPath, Role[] roles)
			throws Exception {

		assert multiPath != null;
		assert roles != null;
		assert multiPath.length == roles.length;

		Polygon finalPolygon = new Polygon();

		List<MultiPath> currentPath = new ArrayList<MultiPath>();
		List<MultiPathAndRole> pathLeft = new ArrayList<>();
		for (int i = 0; i < multiPath.length; i++) {
			pathLeft.add(new MultiPathAndRole(multiPath[i], roles[i]));
		}

		MultiPathAndRole current = null;
		current = pop(pathLeft);

		while (current != null) {

			while (current != null && isClosed(current.getMultiPath())) {
				// add to polygon
				finalPolygon.add(current.getMultiPath(), false);
				current = pop(pathLeft);
			}

			// if (pathLeft.size() == 0)
			// {
			// // no more elements,
			// return finalPolygon;
			// }

			// current might be null

			if (current == null) {
				return finalPolygon;
			}

			assert current != null && !current.getMultiPath().isClosedPath(0);

			MultiPath p = (MultiPath) current.getMultiPath().copy();

			boolean finished = false;

			while (!finished) {

				int pathEnd = p.getPathEnd(0) - 1;
				Point joinPoint = p.getPoint(pathEnd); // the join point

				MultiPath followingPathWithCorrectOrder = findExtremisAndIfEndPointReverseTheMultiPath(
						pathLeft, joinPoint, current.getRole()); // search for
																	// the
																	// next

				if (followingPathWithCorrectOrder != null) {
					// add the path to the current multipath
					p.insertPoints(0, pathEnd + 1,
							followingPathWithCorrectOrder, 0, 0,
							followingPathWithCorrectOrder.getPathCount(), true);

				} else {
					// don't find a following path, and not closed !!!

					System.out.println("elements left :" + pathLeft);
					for (int i = 0; i < pathLeft.size(); i++) {
						MultiPathAndRole e = pathLeft.get(i);
						dump(e.getMultiPath());
					}

					throw new Exception("path cannot be closed");
				}

				// closed ???

				if (areCoincident(p.getPoint(p.getPathStart(0)),
						p.getPoint(p.getPathEnd(0) - 1))) {

					// yes this is closed, add the part

					// FIXME reverse path ??? -> inner / outer, the proper
					// orientation

					finalPolygon.add(p, false);
					finished = true;
				}

			}

			current = pop(pathLeft);

		}

		return finalPolygon;

	}

	public static boolean isClosed(MultiPath p) {
		assert p != null;
		int start = p.getPathStart(0);
		int end = p.getPathEnd(0) - 1;

		return areCoincident(p.getPoint(start), p.getPoint(end));

	}

	public static MultiPath findExtremisAndIfEndPointReverseTheMultiPath(
			List<MultiPathAndRole> left, Point joinPoint, Role searchRole) {
		assert left != null;

		if (left.size() == 0) {
			return null;
		}

		int bestindex = -1;
		double distance = Double.MAX_VALUE;

		for (int i = 0; i < left.size(); i++) {

			MultiPathAndRole e = left.get(i);

			if (e.getRole() != searchRole)
				continue;

			MultiPath p = e.getMultiPath();
			assert p != null;
			assert p.getPathCount() == 1;

			int indexStart = p.getPathStart(0);
			int indexStop = p.getPathEnd(0) - 1;

			Point startPoint = p.getPoint(indexStart);
			Point entPoint = p.getPoint(indexStop);
			if (areCoincident(startPoint, joinPoint)) {
				// remove point in the collection
				left.remove(i);
				return p;

			} else if (areCoincident(entPoint, joinPoint)) {

				// reverse the order
				MultiPath newGeometry = (MultiPath) p.copy();
				newGeometry.reverseAllPaths();

				left.remove(i);
				return p;
			}

			double d = Math.min(euclidianDistance(startPoint, joinPoint),
					euclidianDistance(entPoint, joinPoint));
			if (d < distance) {
				distance = d;
				bestindex = i;
			}

		}

		System.out.println("best probable match for point "
				+ joinPoint
				+ " index "
				+ bestindex
				+ "("
				+ GeometryEngine.geometryToJson(4623, left.get(bestindex)
						.getMultiPath()) + ") distance :" + distance);

		return null;
	}

	public static boolean areCoincident(Point p1, Point p2) {
		assert p1 != null;
		assert p2 != null;

		double euclidianDistance = euclidianDistance(p1, p2);
		return euclidianDistance < 1e-20;

	}

	private static double euclidianDistance(Point p1, Point p2) {
		assert p1 != null;
		assert p2 != null;
		return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2)
				+ Math.pow(p2.getY() - p2.getY(), 2));
	}

	/**
	 * pop the first multipath in the collection, null if none
	 */
	public static MultiPathAndRole pop(List<MultiPathAndRole> l) {
		if (l == null)
			return null;

		boolean finished = false;
		MultiPathAndRole e = null;
		while (!finished) {
			if (l.size() == 0)
				return null;

			e = l.get(0);
			l.remove(0);

			MultiPath m = e.getMultiPath();
			if (m != null && !m.isEmpty()) {
				finished = true;
			}
		}

		return e;
	}

	public static void dump(MultiPath p) {
		assert p != null;
		String jsong = GeometryEngine.geometryToJson(4623, p);
		System.out.println(jsong);
	}

}

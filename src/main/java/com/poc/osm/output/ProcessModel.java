package com.poc.osm.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.poc.osm.output.actors.LBActor;
import com.poc.osm.output.actors.StreamProcessingActor;
import com.poc.osm.regulation.FlowRegulator;
import com.poc.osm.regulation.MessageRegulatorRegister;
import com.poc.osm.tools.Tools;

/**
 * Model of the processing chain
 * 
 * @author pfreydiere
 * 
 */
public class ProcessModel {

	private static final String SEPARATOR = "\n  ";

	public Stream mainStream;

	public Collection<Stream> streams = new ArrayList<Stream>();

	public Collection<OutCell> outs = new ArrayList<OutCell>();

	private Map<Stream, Set<ModelElement>> childrens;
	private Map<Stream, Set<ModelElement>> childrenOthers;

	public void addStream(Stream s) {
		assert s != null;
		streams.add(s);
	}

	public void addOut(OutCell r) {
		assert r != null;
		outs.add(r);
	}

	/**
	 * 
	 * @param s
	 * @param children
	 */
	private void reverse(Stream s, Map<Stream, Set<ModelElement>> children) {

		if (s == null)
			return;

		Stream parent = s.parentStream;
		if (parent == null) {
			return;
		}

		addInChildren(s, parent, children);

		reverse(parent, children);
	}

	/**
	 * Reverse the model
	 * 
	 * @return
	 */
	public void computeChildrens() {

		Map<Stream, Set<ModelElement>> childrens = new HashMap<Stream, Set<ModelElement>>();

		for (OutCell c : outs) {
			for (Stream s : c.streams) {
				addInChildren(c, s, childrens);
				reverse(s, childrens);
			}
		}

		this.childrens = childrens;

	}

	public void compactAndExtractOthers() {
		this.childrenOthers = new HashMap<Stream, Set<ModelElement>>();
		compactAndExtractOthers(mainStream);
	}

	private void compactAndExtractOthers(Stream s) {

		Set<ModelElement> children = getChildren(s);
		for (ModelElement me : children) {
			if (!(me instanceof Stream)) {
				continue;
			}
			compactAndExtractOthers((Stream) me);
		}

		HashSet<ModelElement> to_remove = new HashSet<ModelElement>();

		boolean found = false; // sanity
		for (ModelElement me : children) {

			if (!(me instanceof Stream)) {
				continue;
			}

			Stream consideredStream = (Stream) me;

			if (consideredStream.isOther) {
				assert consideredStream.parentStream == s;

				assert !found; // sanity
				found = true; // sanity

				Set<ModelElement> children2 = getChildren(consideredStream);
				if (children2 != null) {
					childrenOthers.put(s, children2);
				}
				to_remove.add(me);
			} // else nothing to do

		}

		// remove elements in the main childrens
		for (ModelElement m : to_remove) {
			childrens.remove(m);
		}
		// remove the other in the main childrens
		children.removeAll(to_remove);

	}

	/**
	 * return all the childrens of the passed stream
	 * 
	 * @param s
	 * @return
	 */
	public Set<ModelElement> getChildren(Stream s) {
		if (childrens == null) {
			throw new IllegalStateException("childrens must be computed");
		}
		return childrens.get(s);
	}

	/**
	 * get the others childrens
	 * 
	 * @param s
	 * @return
	 */
	public Set<ModelElement> getOthersChildrens(Stream s) {
		if (childrenOthers == null) {
			throw new IllegalStateException(
					"childrens must be compacted before"); 
		}
		return childrenOthers.get(s);
	}

	/**
	 * add a child in the set
	 * 
	 * @param s
	 * @param parent
	 * @param children
	 */
	private void addInChildren(ModelElement s, Stream parent,
			Map<Stream, Set<ModelElement>> children) {

		assert parent != null;

		assert s != null;

		Set<ModelElement> l = children.get(parent);
		if (l == null) {
			l = new HashSet<ModelElement>();
			children.put(parent, l);
		}

		l.add(s);
	}

	private List<ActorRef> getActorRefList(ActorSystem sys,
			Set<ModelElement> elements, ActorRef flowRegulator) {
		if (elements == null)
			return null;

		List<ActorRef> actors = new ArrayList<ActorRef>();
		for (ModelElement me : elements) {
			actors.add(getOrCreateActorRef(sys, me, flowRegulator));
		}
		return actors;
	}

	/**
	 * get Or Create the transform Actor
	 * 
	 * @param sys
	 * @param s
	 * @return
	 */
	public ActorRef getOrCreateActorRef(ActorSystem sys, ModelElement s,
			ActorRef flowRegulator) {
		if (s == null)
			throw new IllegalArgumentException();

		if (s._actorRef != null) {
			return s._actorRef;
		}

		assert s._actorRef == null;
		assert s instanceof Stream;

		Stream ss = (Stream) s;

		// create the actor

		List<ActorRef> childrenActorRefList = getActorRefList(sys,
				childrens.get(ss), flowRegulator);

		List<ActorRef> othersActorRefList = getActorRefList(sys,
				childrenOthers.get(ss), flowRegulator);

		System.out.println("Create actor " + ss.getKey() + " following "
				+ childrenActorRefList + "," + othersActorRefList);

		List<ActorRef> actors = new ArrayList<ActorRef>();

		for (int i = 0; i < 4; i++) {
			// create the stream processing actor
			ActorRef r = sys.actorOf(Props.create(StreamProcessingActor.class,
					ss.filter, ss.transform, childrenActorRefList,
					othersActorRefList), Tools.toActorName(ss.getKey()) + "_"
					+ i);
			flowRegulator.tell(new MessageRegulatorRegister(r),
					ActorRef.noSender());
			actors.add(r);
		}

		ActorRef a = sys.actorOf(Props.create(LBActor.class, actors), "D_"
				+ Tools.toActorName(ss.getKey()));

		// register the dispatcher
		flowRegulator
				.tell(new MessageRegulatorRegister(a), ActorRef.noSender());

		s._actorRef = a;

		return a;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ProcessModel :").append(SEPARATOR).append("streams :")
				.append(streams).append(SEPARATOR).append("outs :")
				.append(outs);

		if (childrens != null) {
			sb.append("\n");
			for (Entry<Stream, Set<ModelElement>> e : childrens.entrySet()) {
				sb.append(" child of " + e.getKey() + " ->")
						.append(e.getValue()).append("\n");
			}

			sb.append("\n");
			for (Entry<Stream, Set<ModelElement>> e : childrenOthers.entrySet()) {
				sb.append(" other child of " + e.getKey() + " ->")
						.append(e.getValue()).append("\n");
			}

		}

		return sb.toString();
	}

}

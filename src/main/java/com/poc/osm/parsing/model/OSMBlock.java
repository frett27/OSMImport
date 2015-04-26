package com.poc.osm.parsing.model;

import java.io.Serializable;
import java.util.List;

import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;

public class OSMBlock implements Serializable {

	private long counter; 
	private DenseNodes denseNodes;
	private List<Node> nodes;
	private List<Way> ways;
	private List<Relation> relations;

	private OSMContext ctx;

	public OSMBlock(int counter) {
		this.counter = counter;
	}
	
	public long getCounter() {
		return counter;
	}

	public void setContext(OSMContext ctx) {
		this.ctx = ctx;
	}
	
	public OSMContext getContext() {
		return ctx;
	}

	public void setDenseNodes(DenseNodes denseNodes) {
		this.denseNodes = denseNodes;
	}

	public DenseNodes getDenseNodes() {
		return denseNodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setWays(List<Way> ways) {
		this.ways = ways;
	}

	public List<Way> getWays() {
		return ways;
	}

	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}

	public List<Relation> getRelations() {
		return relations;
	}

}

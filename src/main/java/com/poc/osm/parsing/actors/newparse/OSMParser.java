package com.poc.osm.parsing.actors.newparse;

/** Copyright (c) 2010 Scott A. Crosby. <scott@sacrosby.com>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as 
 published by the Free Software Foundation, either version 3 of the 
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.poc.osm.model.OSMBlock;
import com.poc.osm.model.OSMContext;
import com.poc.osm.parsing.actors.OSMObjectGenerator;
import com.poc.osm.parsing.actors.ParsingSystemActorsConstants;
import com.poc.osm.regulation.MessageRegulation;

import crosby.binary.Fileformat.Blob;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.PrimitiveBlock;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;
import crosby.binary.BinaryParser;
import crosby.binary.Fileformat;
import crosby.binary.Osmformat;
import crosby.binary.file.BlockReaderAdapter;
import crosby.binary.file.FileBlock;
import crosby.binary.file.FileBlockPosition;

public class OSMParser extends UntypedActor {

	protected int granularity;
	private long lat_offset;
	private long lon_offset;
	protected int date_granularity;
	private String strings[];

	public interface VelGetter {
		double get();
	}

	/**
	 * Take a Info protocol buffer containing a date and convert it into a java
	 * Date object
	 */
	protected Date getDate(Osmformat.Info info) {
		if (info.hasTimestamp()) {
			return new Date(date_granularity * (long) info.getTimestamp());
		} else
			return NODATE;
	}

	public static final Date NODATE = new Date(-1);

	/**
	 * Get a string based on the index used.
	 * 
	 * Index 0 is reserved to use as a delimiter, therefore, index 1 corresponds
	 * to the first string in the table
	 * 
	 * @param id
	 * @return
	 */
	protected String getStringById(int id) {
		return strings[id];
	}

	/**
	 * Convert a latitude value stored in a protobuf into a double, compensating
	 * for granularity and latitude offset
	 */
	public double parseLat(long degree) {
		// Support non-zero offsets. (We don't currently generate them)
		return (granularity * degree + lat_offset) * .000000001;
	}

	/**
	 * Convert a longitude value stored in a protobuf into a double,
	 * compensating for granularity and longitude offset
	 */
	public double parseLon(long degree) {
		// Support non-zero offsets. (We don't currently generate them)
		return (granularity * degree + lon_offset) * .000000001;
	}

	/**
	 * Parse a Primitive block (containing a string table, other paramaters, and
	 * PrimitiveGroups
	 */
	public void parseBlock(Osmformat.PrimitiveBlock block) {

		Osmformat.StringTable stablemessage = block.getStringtable();
		strings = new String[stablemessage.getSCount()];

		for (int i = 0; i < strings.length; i++) {
			strings[i] = stablemessage.getS(i).toStringUtf8();
		}

		granularity = block.getGranularity();
		lat_offset = block.getLatOffset();
		lon_offset = block.getLonOffset();
		date_granularity = block.getDateGranularity();

		for (Osmformat.PrimitiveGroup groupmessage : block
				.getPrimitivegroupList()) {
			// Exactly one of these should trigger on each loop.
			parseNodes(groupmessage.getNodesList());
			parseWays(groupmessage.getWaysList());
			parseRelations(groupmessage.getRelationsList());
			if (groupmessage.hasDense())
				parseDense(groupmessage.getDense());
		}
	}

	public int cpt = 0;

	public Router digger;

	public OSMBlock currentBlock = new OSMBlock(cpt++);

	public OSMContext currentContext;
	

	public OSMParser(Router digger) {
		this.digger = digger;
		
	}

	protected void parseRelations(List<Relation> rels) {

		if (currentBlock.getRelations() != null) {
			ArrayList<Relation> nr = new ArrayList<Relation>(
					currentBlock.getRelations());
			nr.addAll(rels);
			currentBlock.setRelations(nr);
		} else {

			currentBlock.setRelations(rels);
		}
	}

	protected void parseDense(DenseNodes nodes) {

		assert currentBlock.getDenseNodes() == null;
		currentBlock.setDenseNodes(nodes);
	}

	protected void parseNodes(List<Node> nodes) {

		// System.out.println("parse nodes");
		if (nodes == null)
			return;

		if (currentBlock.getNodes() != null) {
			ArrayList<Node> no = new ArrayList<Osmformat.Node>(
					currentBlock.getNodes());
			no.addAll(nodes);
			currentBlock.setNodes(no);
		} else {
			currentBlock.setNodes(nodes);
		}

	}

	protected void parseWays(List<Way> ways) {
		// System.out.println("parseway");
		if (ways == null)
			return;

		if (currentBlock.getWays() != null) {
			ArrayList<Way> na = new ArrayList<Way>(currentBlock.getWays());
			na.addAll(ways);
			currentBlock.setWays(na);
		}
		currentBlock.setWays(ways);

	}

	public void parse(PrimitiveBlock block) {

		Osmformat.StringTable stablemessage = block.getStringtable();
		String[] strings = new String[stablemessage.getSCount()];

		for (int i = 0; i < strings.length; i++) {
			strings[i] = stablemessage.getS(i).toStringUtf8();
		}

		int granularity = block.getGranularity();
		long lat_offset = block.getLatOffset();
		long lon_offset = block.getLonOffset();
		int date_granularity = block.getDateGranularity();

		OSMContext ctx = new OSMContext(granularity, lat_offset, lon_offset,
				date_granularity, strings);
		currentContext = ctx;
		currentBlock.setContext(currentContext);

		parseBlock(block);

		flush();
	}

	protected void flush() {
		// System.out.println("flush");

		if (digger != null)
		{
			digger.route(currentBlock, ActorRef.noSender());
		
		}
		this.currentBlock = new OSMBlock(cpt++);
		this.currentBlock.setContext(currentContext);
	}

	public void complete() {
		System.out.println("Complete file read !");
	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof Blob) {
			parse(parseData((Blob) message));
		} else {
			unhandled(message);
		}

	}

	/** Parse out and decompress the data part of a fileblock helper function. */
	PrimitiveBlock parseData(Fileformat.Blob blob) throws Exception {
		if (blob.hasRaw()) {

			return parsePrimitiveBlock(blob.getRaw());

		} else if (blob.hasZlibData()) {
			byte buf2[] = new byte[blob.getRawSize()];
			Inflater decompresser = new Inflater();
			decompresser.setInput(blob.getZlibData().toByteArray());
			// decompresser.getRemaining();
			try {
				decompresser.inflate(buf2);
			} catch (DataFormatException e) {
				e.printStackTrace();
				throw new Error(e);
			}
			assert (decompresser.finished());
			decompresser.end();

			return parsePrimitiveBlock(ByteString.copyFrom(buf2));

		}

		throw new Exception("unsupported blob");
	}

	PrimitiveBlock parsePrimitiveBlock(ByteString datas) throws Exception {
		
	
		return PrimitiveBlock.parseFrom(datas);
	}

}
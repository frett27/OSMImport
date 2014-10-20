package com.poc.osm.model;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import akka.routing.Router;
import crosby.binary.BinaryParser;
import crosby.binary.Osmformat;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.PrimitiveBlock;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;
import crosby.binary.file.BlockInputStream;

public class OSMReader {

	public interface VelGetter {
		double get();
	}

	private class OSMBinaryParser extends BinaryParser {

		public int cpt = 0;

		public Router digger;

		public OSMBlock currentBlock = new OSMBlock(cpt++);

		public OSMContext currentContext;

		public OSMBinaryParser(Router digger) {
			this.digger = digger;
		}

		@Override
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

		@Override
		protected void parseDense(DenseNodes nodes) {

			assert currentBlock.getDenseNodes() == null;
			currentBlock.setDenseNodes(nodes);
		}

		@Override
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

		@Override
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

		@Override
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

			OSMContext ctx = new OSMContext(granularity, lat_offset,
					lon_offset, date_granularity, strings);
			currentContext = ctx;
			currentBlock.setContext(currentContext);

			super.parse(block);

			flush();
		}

		@Override
		protected void parse(HeaderBlock header) {
			// System.out.println("Got header block.");
		}

		protected void flush() {
			// System.out.println("flush");

			try {
				// Thread.sleep(1000); // pression nominale pour

				double v = currentVelGetter.get();
				// System.out.println("vel :" + v);
				long l = (1000 - (long) v);

				if (l > 0) {
					if (l > 2000)
						l = 5000;
					System.out.println("sleep :" + l);
					Thread.sleep(l);
				}
			} catch (Exception ex) {

			}

			digger.route(currentBlock, ActorRef.noSender());
			this.currentBlock = new OSMBlock(cpt++);
			this.currentBlock.setContext(currentContext);
		}

		public void complete() {
			System.out.println("Complete!");
		}

	}

	public OSMReader() {

	}

	VelGetter currentVelGetter;

	/**
	 * read the file and send it to the given router
	 * 
	 * @param is
	 *            the file inputStream
	 * @param r
	 *            the router to send the blocks to
	 * @throws Exception
	 */
	public void read(InputStream is, Router r, VelGetter velGetter)
			throws Exception {
		assert is != null;
		this.currentVelGetter = velGetter;
		System.out.println("Read file");
		InputStream input = new BufferedInputStream(is);
		OSMBinaryParser brad = new OSMBinaryParser(r);
		BlockInputStream blockInputStream = new BlockInputStream(input, brad);
		blockInputStream.process();

	}

}

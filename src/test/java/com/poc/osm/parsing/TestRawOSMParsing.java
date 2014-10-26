package com.poc.osm.parsing;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import junit.framework.TestCase;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.poc.osm.parsing.actors.newparse.OSMBlockParsingActor;
import com.poc.osm.parsing.actors.newparse.OSMParser.VelGetter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import crosby.binary.Fileformat;
import crosby.binary.Fileformat.Blob;
import crosby.binary.Osmformat.PrimitiveBlock;

public class TestRawOSMParsing extends TestCase {

	VelGetter currentVelGetter;

	public void testParsing() throws Exception {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");

		currentVelGetter = new VelGetter() {
			@Override
			public double get() {
				return 1000;
			}
		};

		Config config = ConfigFactory.load();

		ActorSystem sys = ActorSystem.create("osmcluster",
				config.getConfig("osmcluster"));

		ActorRef p = sys.actorOf(Props.create(OSMBlockParsingActor.class,
				(Object) null));

		File f = new File("france-latest.osm.pbf");
		BufferedInputStream fis = new BufferedInputStream(
				new FileInputStream(f), 10000000);
		try {
			System.out.println("Begin :" + simpleDateFormat.format(new Date()));
			DataInputStream datinput = new DataInputStream(fis);
			while (true) {
				readChunk(datinput, p);
			}

		} finally {
			fis.close();
			System.out.println("End :" + simpleDateFormat.format(new Date()));

		}

	}

	/**
	 * read data chunk
	 * 
	 * @param datinput
	 * @throws IOException
	 * @throws InvalidProtocolBufferException
	 */
	private void readChunk(DataInputStream datinput, ActorRef p)
			throws Exception {

		try {
			// Thread.sleep(1000); // pression nominale pour

			double v = currentVelGetter.get();
			// System.out.println("vel :" + v);
			long l = (1000 - (long) v);

			if (l > 0) {
				if (l > 2000)
					l = 5000;
				// System.out.println("sleep :" + l);
				Thread.sleep(l);
			}
		} catch (Exception ex) {

		}

		int headersize = datinput.readInt();

		byte buf[] = new byte[headersize];
		datinput.readFully(buf);

		// System.out.format("Read buffer for header of %d bytes\n",buf.length);
		Fileformat.BlobHeader header = Fileformat.BlobHeader.parseFrom(buf);

		int datasize = header.getDatasize();

		// System.out.println("block type :" + header.getType());
		// System.out.println("datasize :" + datasize);

		byte b[] = new byte[datasize];
		datinput.readFully(b);

		Blob blob = Fileformat.Blob.parseFrom(b);

		// System.out.println(blob);
		// System.out.println("has lzmaData :" + blob.hasLzmaData());
		// System.out.println("has zlibData :" + blob.hasZlibData());
		// System.out.println("raw size :" + blob.getRawSize());
		// ByteString zlibData = blob.getZlibData();
		// System.out.println("zlibdata :" + zlibData.size());

		if ("OSMData".equals(header.getType())) {
			p.tell(blob, ActorRef.noSender());
		}

	}

	

}

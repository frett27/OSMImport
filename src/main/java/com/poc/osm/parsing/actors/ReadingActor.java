package com.poc.osm.parsing.actors;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

import com.google.protobuf.InvalidProtocolBufferException;
import com.poc.osm.model.OSMReader;
import com.poc.osm.model.OSMReader.VelGetter;
import com.poc.osm.parsing.actors.messages.MessageClusterRegistration;
import com.poc.osm.parsing.actors.messages.MessageParsingSystemStatus;
import com.poc.osm.parsing.actors.messages.MessageReadFile;
import com.poc.osm.parsing.actors.newparse.OSMBlockParsingActor;
import com.poc.osm.regulation.MessageRegulatorStatus;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Timer;

import crosby.binary.Fileformat;
import crosby.binary.Fileformat.Blob;

/**
 * Actor handling the file handling
 * 
 * @author pfreydiere
 * 
 */
public class ReadingActor extends UntypedActor {

	private static final String TICK_FOR_PID_UPDATE = "tick";

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private Router generatorRouter;

	private ActorRef dispatcher;

	private ActorRef flowRegulator;

	private ActorRef newParsingActors;

	private long REG_TIME = 200;

	private Counter nbofRead;

	private Timer timer;

	public ReadingActor(ActorRef dispatcher, ActorRef flowRegulator) {

		log.debug("starting actor " + getClass().getName());

		this.dispatcher = dispatcher;

		this.flowRegulator = flowRegulator;

		List<Routee> routees = new ArrayList<Routee>();
		for (int i = 0; i < 5; i++) {
			ActorRef r = getContext().actorOf(
					Props.create(OSMObjectGenerator.class, dispatcher, flowRegulator));
			getContext().watch(r);
			routees.add(new ActorRefRoutee(r));
		}

		generatorRouter = new Router(new RoundRobinRoutingLogic(), routees);

		newParsingActors = getContext().actorOf(
				Props.create(OSMBlockParsingActor.class, generatorRouter, flowRegulator));

		nbofRead = Metrics
				.newCounter(ReadingActor.class, "Number of file read");

		timer = Metrics.newTimer(ReadingActor.class, "ReadFile time");

	}

	private enum State {
		AVAILABLE, RUNNING
	}

	private State currentState = State.AVAILABLE;

	private File currentFile = null;

	private double currentVel = 100;

	@Override
	public void preStart() throws Exception {
		super.preStart();

		getContext()
				.system()
				.scheduler()
				.scheduleOnce(
						Duration.create(REG_TIME * 3, TimeUnit.MILLISECONDS),
						getSelf(), TICK_FOR_PID_UPDATE,
						getContext().dispatcher(), null);
	}

	// ioc for velocity getting in the reader
	private VelGetter currentVelGetter = new VelGetter() {
		@Override
		public double get() {
			return currentVel;
		}
	};

	public void postStop() throws Exception {
		log.debug("stopping async reading");
		asyncReading.shutdown();
	};

	@Override
	public void onReceive(Object message) throws Exception {

		if (TICK_FOR_PID_UPDATE.equals(message)) {
			getContext()
					.system()
					.scheduler()
					.scheduleOnce(
							Duration.create(REG_TIME, TimeUnit.MILLISECONDS),
							getSelf(), TICK_FOR_PID_UPDATE,
							getContext().dispatcher(), null);

			// ask for the regulation status
			flowRegulator.tell(new MessageRegulatorStatus(), getSelf());

		} else if (message instanceof MessageRegulatorStatus) {

			MessageRegulatorStatus s = (MessageRegulatorStatus) message;

			// the regulator tell us the new velocity
			currentVel = s.getNewVelocity();

		} else if (message instanceof MessageReadFile) {

			MessageReadFile mrf = (MessageReadFile) message;

			log.info("start reading file " + mrf.getFileToRead());

			currentState = State.RUNNING;

			readFile(mrf.getFileToRead());

		} else if (message == MessageClusterRegistration.NEED_MORE_READ) {

			log.info("One more read");

			asyncReading.submit(new Callable() {
				@Override
				public Object call() throws Exception {
					log.info("start reading file");
					dispatcher.tell(
							MessageParsingSystemStatus.START_READING_FILE,
							getSelf());
					nbofRead.inc();
					timer.time();

					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
							"hh:mm:ss");
					String start = "Begin :"
							+ simpleDateFormat.format(new Date());

					FileInputStream fis = new FileInputStream(currentFile);
					try {

						BufferedInputStream bfis = new BufferedInputStream(fis,
								10000000);
						try {

							DataInputStream datinput = new DataInputStream(bfis);
							while (true) {
								readChunk(datinput, newParsingActors);
							}

						} catch (EOFException eof) {
							// end of the read
						} finally {
							fis.close();

							System.out.println(start);
							System.out.println("End :"
									+ simpleDateFormat.format(new Date()));

						}

						log.info("reading file ended");

						dispatcher.tell(
								MessageParsingSystemStatus.END_READING_FILE,
								getSelf());

						log.info("Ask for more read ?");

						getContext()
								.system()
								.scheduler()
								.scheduleOnce(
										Duration.create(100,
												TimeUnit.SECONDS),
										dispatcher,
										MessageClusterRegistration.ASK_IF_NEED_MORE_READ,
										getContext().dispatcher(), getSelf());

					} catch (Throwable ex) {
						log.error("error in reading :" + ex.getMessage(), ex);

					} finally {
						log.info("closing file");
						fis.close();
						timer.stop();
					}
					return null;
				}
			});
		} else {
			unhandled(message);
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

			// velocity between 0 and 1000

			double maxTimeToWaitIf0 = 5.0;
			
			double t = maxTimeToWaitIf0 - maxTimeToWaitIf0 / 1000.0 * v;
			if (t > maxTimeToWaitIf0)
				t = maxTimeToWaitIf0;
			if (t < 0)
				t = 0;

			if (t > 0) {
				// System.out.println("sleep :" + t);
				Thread.sleep((long) (t * 1000) + 20);
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

	private ExecutorService asyncReading = Executors.newSingleThreadExecutor();

	void readFile(File f) throws Exception {

		log.info("Read file :" + f);
		this.currentFile = f;

		dispatcher.tell(MessageParsingSystemStatus.START_JOB, getSelf());

		onReceive(MessageClusterRegistration.NEED_MORE_READ);
	}

}

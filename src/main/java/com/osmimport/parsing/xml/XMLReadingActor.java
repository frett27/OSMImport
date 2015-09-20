package com.osmimport.parsing.xml;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.dispatch.ControlMessage;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.osmimport.actors.MeasuredActor;
import com.osmimport.messages.ParsingObjects;
import com.osmimport.parsing.pbf.actors.OSMParser.VelGetter;
import com.osmimport.parsing.pbf.actors.ReadingSubSystemActor;
import com.osmimport.parsing.pbf.actors.messages.MessageClusterRegistration;
import com.osmimport.parsing.pbf.actors.messages.MessageParsingSystemStatus;
import com.osmimport.parsing.pbf.actors.messages.MessageReadFile;
import com.osmimport.regulation.MessageRegulatorStatus;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Timer;

public class XMLReadingActor extends MeasuredActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private static final RegulationMessage TICK_FOR_PID_UPDATE = new RegulationMessage();

	private static class RegulationMessage implements ControlMessage {

	}

	private ActorRef dispatcher;

	private ActorRef flowRegulator;

	private long REG_TIME = 100;

	private Counter nbofRead;

	private Timer timer;

	private File currentFile = null;

	private double currentVel = 100;

	private OSMXmlParsing parser = new OSMXmlParsing();

	private int READ_BUFFER_SIZE = 1000000;

	public XMLReadingActor(ActorRef dispatcher, ActorRef flowRegulator) {
		assert dispatcher != null;
		assert flowRegulator != null;

		this.dispatcher = dispatcher;
		this.flowRegulator = flowRegulator;

		nbofRead = Metrics.newCounter(ReadingSubSystemActor.class,
				"Number of file read");

		timer = Metrics.newTimer(ReadingSubSystemActor.class, "ReadFile time");
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();

		getContext()
				.system()
				.scheduler()
				.scheduleOnce(Duration.create(10, TimeUnit.SECONDS), getSelf(),
						TICK_FOR_PID_UPDATE, getContext().dispatcher(), null);
	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (TICK_FOR_PID_UPDATE.equals(message)) {
			// don't count
			getContext()
					.system()
					.scheduler()
					.scheduleOnce(
							Duration.create(REG_TIME, TimeUnit.MILLISECONDS),
							getSelf(), TICK_FOR_PID_UPDATE,
							getContext().dispatcher(), null);

			// ask for the regulation status
			tell(flowRegulator, new MessageRegulatorStatus(), getSelf());
			return;
		}

		super.onReceive(message);
	}

	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		if (message instanceof MessageRegulatorStatus) {

			MessageRegulatorStatus s = (MessageRegulatorStatus) message;

			// the regulator tell us the new velocity
			currentVel = s.getNewVelocity();

		} else if (message instanceof MessageReadFile) {

			MessageReadFile mrf = (MessageReadFile) message;

			log.info("start reading file " + mrf.getFileToRead());

			readFile(mrf.getFileToRead());

		} else if (message == MessageClusterRegistration.NEED_MORE_READ) {

			log.info("One more read");

			asyncReading.submit(new Callable() {
				@Override
				public Object call() throws Exception {
					log.info("start reading file");
					tell(dispatcher,
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
						int cpt = 0;
						BufferedInputStream bfis = new BufferedInputStream(fis,
								READ_BUFFER_SIZE);
						try {

							parser.parseFile(bfis,
									new ParsingObjectsListener() {

										@Override
										public void emit(ParsingObjects objects) {
											regulate();
											tell(dispatcher, objects,
													ActorRef.noSender());
										}
									}

							);
							
							

						} catch (EOFException eof) {
							// end of the read
						} finally {
							fis.close();

							System.out.println(start);
							System.out.println("End :"
									+ simpleDateFormat.format(new Date()));

						}

						log.info("reading file ended");

						tell(dispatcher,
								MessageParsingSystemStatus.END_READING_FILE,
								getSelf());

						log.info("Ask for more read ?");

						getContext()
								.system()
								.scheduler()
								.scheduleOnce(
										Duration.create(10, TimeUnit.SECONDS),
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

	private ExecutorService asyncReading = Executors.newSingleThreadExecutor();

	void readFile(File f) throws Exception {

		log.info("Read file :" + f);
		this.currentFile = f;

		tell(dispatcher, MessageParsingSystemStatus.START_JOB, getSelf());

		onReceive(MessageClusterRegistration.NEED_MORE_READ);
	}

	private VelGetter currentVelGetter = new VelGetter() {
		@Override
		public double get() {
			return currentVel;
		}
	};

	/**
	 * regulate input
	 */
	private void regulate() {
		try {

			double v = currentVelGetter.get();
			// System.out.println("vel :" + v);

			// velocity between 0 and 1000

			double maxTimeToWaitIf0 = 1.0;
			double timeToWaitif1000 = 0.005;

			double t = maxTimeToWaitIf0 - maxTimeToWaitIf0 / 1000.0 * v
					+ timeToWaitif1000;
			if (t > maxTimeToWaitIf0)
				t = maxTimeToWaitIf0;
			if (t < 0)
				t = 0;

			do {

				if (t > 0.1) {
					System.out.println("Velocity :" + v);
					// System.out.println("SlowDown the input reading :" + t);
				}
				Thread.sleep((long) (t * 1000));

			} while (currentVelGetter.get() < 1);

		} catch (Exception ex) {

		}
	}

}

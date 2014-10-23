package com.poc.osm.parsing.actors;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
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

import com.poc.osm.model.OSMReader;
import com.poc.osm.model.OSMReader.VelGetter;
import com.poc.osm.parsing.actors.messages.MessageClusterRegistration;
import com.poc.osm.parsing.actors.messages.MessageParsingSystemStatus;
import com.poc.osm.parsing.actors.messages.MessageReadFile;
import com.poc.osm.regulation.MessageRegulatorStatus;

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

	private long REG_TIME = 2000;

	public ReadingActor(ActorRef dispatcher, ActorRef flowRegulator) {

		log.debug("starting actor " + getClass().getName());

		this.dispatcher = dispatcher;

		this.flowRegulator = flowRegulator;

		List<Routee> routees = new ArrayList<Routee>();
		for (int i = 0; i < 5; i++) {
			ActorRef r = getContext().actorOf(
					Props.create(OSMObjectGenerator.class, dispatcher));
			getContext().watch(r);
			routees.add(new ActorRefRoutee(r));
		}
		
		generatorRouter = new Router(new RoundRobinRoutingLogic(), routees);

		// create dispatcher

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

					FileInputStream fis = new FileInputStream(currentFile);
					try {

						final OSMReader reader = new OSMReader();
						reader.read(fis, generatorRouter, currentVelGetter);

						log.info("reading file ended");

						dispatcher.tell(
								MessageParsingSystemStatus.END_READING_FILE,
								getSelf());

						log.info("Ask for more read ?");

						dispatcher
								.tell(MessageClusterRegistration.ASK_IF_NEED_MORE_READ,
										getSelf());

					} catch (Throwable ex)
					{
						log.error("error in reading :" + ex.getMessage(), ex);

					} finally {
						log.info("closing file");
						fis.close();
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

		dispatcher.tell(MessageParsingSystemStatus.START_JOB, getSelf());

		onReceive(MessageClusterRegistration.NEED_MORE_READ);
	}

}

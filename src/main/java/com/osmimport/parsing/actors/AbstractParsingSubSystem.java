package com.osmimport.parsing.actors;

import java.util.concurrent.TimeUnit;

import scala.collection.Iterator;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.osmimport.actors.MeasuredActor;
import com.osmimport.parsing.pbf.actors.RelationPolygonDispatcher;
import com.osmimport.parsing.pbf.actors.RelationPolygonWorkerActor;
import com.osmimport.parsing.pbf.actors.WayConstructWorkerActor;
import com.osmimport.parsing.pbf.actors.WayParsingDispatcher;
import com.osmimport.parsing.pbf.actors.messages.MessageParsingSystemStatus;
import com.osmimport.parsing.pbf.actors.messages.MessageReadFile;
import com.osmimport.tools.polygoncreator.IInvalidPolygonConstructionFeedBack;

/**
 * abstract class for defining a parsing sub system, strategy might differ
 * depending of the file
 * 
 * @author pfreydiere
 * 
 */
public abstract class AbstractParsingSubSystem extends MeasuredActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	protected ActorRef reading; // reading actor

	protected ActorRef dispatcher; // dispatcher

	protected ActorRef polygonDispatcher;

	protected ActorRef flowRegulator;

	public AbstractParsingSubSystem(
			ActorRef flowRegulator,
			ActorRef output,
			Long maxWaysToCreateForWorker,
			IInvalidPolygonConstructionFeedBack invalidPolygonConstructionFeedBack,
			ParsingLevel parsingLevel) {

		this.flowRegulator = flowRegulator;

		polygonDispatcher = getContext().actorOf(
				Props.create(RelationPolygonDispatcher.class, output,
						flowRegulator), "polygon_dispatcher");

		dispatcher = getContext().actorOf(
				Props.create(WayParsingDispatcher.class, output,
						polygonDispatcher, flowRegulator), "dispatcher");

		// reading actor
		reading = createReadingActor();

		// init the worker for ways

		final long nbofworkers = Runtime.getRuntime().availableProcessors();

		long nbways4worker;
		if (maxWaysToCreateForWorker != null) {
			nbways4worker = maxWaysToCreateForWorker;
		} else {
            // thumbs rules, may not be correct if heavy transforms
			nbways4worker = (long) ((Runtime.getRuntime().maxMemory() * 1.0 - 3_000_000_000.0) / (1_000_000_000.0) * 240_000 / nbofworkers);
		}

		final long maxwaysToConstruct = nbways4worker;

		log.info("maxways to construct for all actors :" + maxwaysToConstruct);

		if (parsingLevel.getValue() >= ParsingLevel.PARSING_LEVEL_LINE
				.getValue()) {

			for (int i = 0; i < nbofworkers; i++) {
				ActorRef worker = getContext().actorOf(
						Props.create(WayConstructWorkerActor.class, dispatcher,
								polygonDispatcher, output, maxwaysToConstruct
										/ nbofworkers), "worker_" + i);
				// initialize the worker
				tell(worker, MessageParsingSystemStatus.INITIALIZE,
						ActorRef.noSender());
			}

		}

		if (parsingLevel.getValue() >= ParsingLevel.PARSING_LEVEL_POLYGON
				.getValue()) {
			// init the works for polygons
			for (int i = 0; i < nbofworkers; i++) {
				ActorRef worker = getContext().actorOf(
						Props.create(RelationPolygonWorkerActor.class,
								polygonDispatcher, output,
								invalidPolygonConstructionFeedBack),
						"polygon_worker_" + i);
				// initialize the worker
				tell(worker, MessageParsingSystemStatus.INITIALIZE,
						ActorRef.noSender());
			}
		}

	}

	protected abstract ActorRef createReadingActor();

	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		if (message instanceof MessageParsingSystemStatus) {

			Iterator<ActorRef> it = getContext().children().iterator();
			for (ActorRef a = it.next(); it.hasNext(); a = it.next()) {
				tell(a, message, getSelf());
			}

			if (MessageParsingSystemStatus.END_JOB == message) {
				log.info("terminate the process");

				// wait for all remaining events to be handled
				getContext()
						.system()
						.scheduler()
						.scheduleOnce(new FiniteDuration(1, TimeUnit.MINUTES),
								new Runnable() {

									@Override
									public void run() {
										System.out
												.println("ShutDown the process");
										getContext().system().shutdown();
									}
								}, getContext().system().dispatcher());
			}

		} else if (message instanceof MessageReadFile) {

			tell(reading, message, getSelf());

		} else {
			unhandled(message);
		}
	}

}

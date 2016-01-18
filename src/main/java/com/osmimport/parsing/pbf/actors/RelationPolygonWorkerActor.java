package com.osmimport.parsing.pbf.actors;

import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.osmimport.actors.MeasuredActor;
import com.osmimport.messages.MessageWay;
import com.osmimport.model.OSMEntity;
import com.osmimport.parsing.model.BaseEntityToConstruct;
import com.osmimport.parsing.model.OSMEntityConstructListener;
import com.osmimport.parsing.model.OSMEntityConstructRegistry;
import com.osmimport.parsing.model.PolygonToConstruct;
import com.osmimport.parsing.pbf.actors.messages.MessageClusterRegistration;
import com.osmimport.parsing.pbf.actors.messages.MessageParsingSystemStatus;
import com.osmimport.parsing.pbf.actors.messages.MessagePolygonToConstruct;
import com.osmimport.regulation.FlowRegulator;
import com.osmimport.tools.polygoncreator.IInvalidPolygonConstructionFeedBack;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;

public class RelationPolygonWorkerActor extends MeasuredActor {

	/**
	 * logger
	 */
	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	/**
	 * the registry to know which points are necessary for a constructing way
	 */
	private OSMEntityConstructRegistry reg;

	/**
	 * handled blocks, list of handled blocks for ways
	 */
	private Set<Long> handledBlocks = new HashSet<Long>();

	private enum State {
		REGISTRATION_PHASE, PROCESSING_PHASE
	}

	/**
	 * current actor state
	 */
	private State currentState = State.REGISTRATION_PHASE;

	/**
	 * output stream chain
	 */
	private ActorRef output;

	private ActorRef polygonDispatcher;

	private boolean hasInformed = false;

	/**
	 * polygon left to construct
	 */
	private Counter polygonsMetrics;

	private IInvalidPolygonConstructionFeedBack invalidPolygonConstructionFeedBack;

	public RelationPolygonWorkerActor(
			ActorRef polygonDispatcher,
			ActorRef output,
			IInvalidPolygonConstructionFeedBack invalidPolygonConstructionFeedBack) {

		this.polygonDispatcher = polygonDispatcher;
		this.output = output;
		this.invalidPolygonConstructionFeedBack = invalidPolygonConstructionFeedBack;
		reg = new OSMEntityConstructRegistry(invalidPolygonConstructionFeedBack);

		polygonsMetrics = Metrics.newCounter(FlowRegulator.class, getSelf()
				.path().name() + " polygons number left to construct");

	}

	@Override
	public void preStart() throws Exception {
		super.preStart();

		// in case some polygon are correctly constructed, this listener is
		// called
		reg.setEntityConstructListener(new OSMEntityConstructListener() {

			int c = 0;

			@Override
			public void signalOSMEntity(OSMEntity e) {
				if (log.isDebugEnabled()) {
					log.debug("emit polygon " + e);
				}
				if (c++ % 100 == 0) {
					log.info("" + c + " polygons constructed from relations");
				}

				// tell the output there is a new constructed polygon
				tell(output, e, getSelf());
				polygonsMetrics.dec();
			}
		});
	}

	@Override
	public void postStop() throws Exception {
		reg.setEntityConstructListener(null);
		if (reg.getEntitiesRegistered() > 0) {
			log.warning("" + reg.getEntitiesRegistered()
					+ " entities are still registered and not finished");
			reg.dumpTS(new OutputStreamWriter(System.out));
		}

		super.postStop();
	}

	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		if (currentState == State.REGISTRATION_PHASE) {

			if (message instanceof MessageParsingSystemStatus) {

				MessageParsingSystemStatus m = (MessageParsingSystemStatus) message;
				if (m == MessageParsingSystemStatus.INITIALIZE) {
					// launch registration

					tell(polygonDispatcher,
							MessageClusterRegistration.ASK_FOR_POLYGONCONSTRUCT_REGISTATION,
							getSelf());

					currentState = State.PROCESSING_PHASE;

				}

			} else {
				unhandled(message);
			}

		} else if (currentState == State.PROCESSING_PHASE) {

			if (message instanceof MessageParsingSystemStatus) {

				if (message == MessageParsingSystemStatus.END_READING_FILE) {

					if (reg.getEntitiesRegistered() > 0) {
						tell(polygonDispatcher,
								MessageClusterRegistration.NEED_MORE_READ,
								getSelf());
						log.info("I need more read, ");
					} else {
						tell(polygonDispatcher,
								MessageClusterRegistration.ALL_BLOCKS_READ,
								getSelf());
					}

					hasInformed = false;

				} else if (message == MessageParsingSystemStatus.TERMINATE) {

					reset();

				} else {
					unhandled(message);
				}

			} else if (message instanceof MessageWay) {

				MessageWay mn = (MessageWay) message;
				reg.giveEntity(Arrays.asList(new OSMEntity[] { mn.getEntity() }));

			} else if (message instanceof MessagePolygonToConstruct) {

				MessagePolygonToConstruct mw = (MessagePolygonToConstruct) message;

				// if block is already handled, skip it
				if (handledBlocks.contains(mw.getBlockid())) {
					// skipped, already processed, we save time
					return;
				}

				// register the block, and prepare for parsing
				List<PolygonToConstruct> waysToConstruct = mw
						.getPolygonsToConstruct();
				for (BaseEntityToConstruct w : waysToConstruct) {
					reg.register(w);
					polygonsMetrics.inc();
				}

				if (!hasInformed) {
					// in case we miss some ways registration,
					// tell the cluster that we need more reads of the input
					// file
					tell(polygonDispatcher,
							MessageClusterRegistration.NEED_MORE_READ,
							getSelf());
					hasInformed = true;
				}

				// the bloc has been handled
				handledBlocks.add(mw.getBlockid());

				log.info(reg.getEntitiesRegistered()
						+ " Polygons relation registered for actor "
						+ getSelf().path());

			} else {
				unhandled(message);
			}

		} else {
			unhandled(message);
		}

	}

	/**
	 * @throws Exception
	 */
	protected void reset() throws Exception {
		reg = new OSMEntityConstructRegistry(invalidPolygonConstructionFeedBack);
		preStart();
		currentState = State.REGISTRATION_PHASE;
		log.debug("current state :" + currentState);
	}

}

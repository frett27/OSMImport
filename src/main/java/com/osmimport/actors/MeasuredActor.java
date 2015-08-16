package com.osmimport.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import scala.collection.immutable.Iterable;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * base class for all actors that need to compute the mailboxes
 * 
 * @author pfreydiere
 * 
 */
public abstract class MeasuredActor extends UntypedActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	/**
	 * the current object metrics
	 */
	private MessageMetrics mymessagemetrics = new MessageMetrics();

	/**
	 * the children metrics must be consistent
	 */
	private MessageMetrics currentConsistentChildrenMetrics = new MessageMetrics();

	/**
	 * collect metrics
	 */
	private Map<Long, CollectingTransaction> ct = new HashMap<Long, CollectingTransaction>();

	/**
	 * timer for collecting children metrics
	 */
	private Timer timer = Metrics.newTimer(getClass(), getSelf().path().name());

	/**
	 * cancellable object for the children metrics collecting
	 */
	private Cancellable metricsScheduleCancellable;

	/**
	 * is this actor a terminal one
	 */
	protected boolean setTerminal = false;

	/**
	 * current collect transaction Id
	 */
	private long transactionId = 0;

	/**
	 * default constructor
	 */
	protected MeasuredActor() {

	}

	@Override
	public void preStart() throws Exception {
		super.preStart();

		metricsScheduleCancellable = getContext()
				.system()
				.scheduler()
				.schedule(Duration.create(100, TimeUnit.MILLISECONDS),
						Duration.create(100, TimeUnit.MILLISECONDS), getSelf(),
						AskMetrics.CHILDREN_ASK, getContext().dispatcher(),
						getSelf());

	}

	protected List<ActorRef> metricsChildrens() {

		ArrayList<ActorRef> l = new ArrayList<ActorRef>();
		for (scala.collection.Iterator<ActorRef> iterator = getContext()
				.children().iterator(); iterator.hasNext();) {
			ActorRef a = iterator.next();
			l.add(a);
		}
		return l;
	}

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof AskMetrics) {

			if (message == AskMetrics.CHILDREN_ASK) {

				log.debug("ask for children metrics");

				long currentTransaction = transactionId++;

				CollectingTransaction t = new CollectingTransaction(
						currentTransaction);

				// timer ask for collecting the metrics
				List<ActorRef> metricsChildrens = metricsChildrens();

				if (metricsChildrens.size() == 0)
					return;

				for (ActorRef a : metricsChildrens) {
					t.registerPath(a.path().toString());
				}

				ct.put(currentTransaction, t);

				// send
				for (ActorRef a : metricsChildrens) {
					a.tell(new AskMetrics(currentTransaction), getSelf());
				}

			} else {

				// we were asked for the object metrics
				synchronized (this) {
					long transaction = ((AskMetrics) message)
							.getCorrelationId();

					MessageMetrics consolidateAllMessages = consolidateAllMessages();

					log.debug(
							"return the metrics to actor {} for transaction {}, nb of messages {}",
							getSender().path().toString(), transaction,
							consolidateAllMessages.computeMailBoxMessageCount());

					getSender().tell(
							new ControlMessageMetrics(transaction,
									consolidateAllMessages), getSelf());
				}
			}
			return;
		}

		if (message instanceof ControlMessageMetrics) {
			synchronized (this) {

				ControlMessageMetrics cmm = (ControlMessageMetrics) message;

				// collect
				long arrivedMetricsMessage = cmm.getCorrelated();

				log.debug(
						"received response from childrens , in transaction {}",
						arrivedMetricsMessage);

				CollectingTransaction collectingTransaction = ct
						.get(arrivedMetricsMessage);
				assert collectingTransaction != null;

				String childPath = getSender().path().toString();
				log.debug("collecting metrics from " + childPath + " :"
						+ cmm.getMessageMetrics().computeMailBoxMessageCount());

				collectingTransaction.collect(childPath,
						cmm.getMessageMetrics());

				if (collectingTransaction.areAllCollected()) {
					log.debug(
							"all messages received for transaction {}, defining the current state",
							arrivedMetricsMessage);
					currentConsistentChildrenMetrics = collectingTransaction
							.getMergedMetrics();

					if (log.isDebugEnabled()) {
						log.debug("message count from children :"
								+ currentConsistentChildrenMetrics
										.computeMailBoxMessageCount());
						collectingTransaction.getMergedMetrics().dump();
					}

					// remove transaction
					ct.remove(arrivedMetricsMessage);
				}
			}
			return;
		}

		/**
		 * timing the treatment
		 */
		TimerContext t = timer.time();
		try {
			if (!setTerminal && getSender() != ActorRef.noSender())
				mymessagemetrics.addMetricFor(getSelf().path().toString(), -1L);

			onReceiveMeasured(message);
		} finally {
			t.stop();
		}
	}

	/**
	 * received method for messages in this actor
	 * 
	 * @param message
	 * @throws Exception
	 */
	public abstract void onReceiveMeasured(Object message) throws Exception;

	/**
	 * protected method for sending a message and collecting metrics
	 * 
	 * @param destination
	 * @param message
	 * @param sender
	 */
	protected void tell(ActorRef destination, Object message, ActorRef sender) {
		assert destination != null;

		mymessagemetrics.addMetricFor(destination.path().toString(), 1);
		destination.tell(message, sender);
	}

	/**
	 * compute the number of messages
	 * @return
	 */
	public long computeMessageNumber() {
		return consolidateAllMessages().computeMailBoxMessageCount();
	}

	/**
	 * compute the addition of all messages (children and current)
	 * @return
	 */
	public MessageMetrics consolidateAllMessages() {
		MessageMetrics m = new MessageMetrics();
		m.mergeWith(mymessagemetrics);
		m.mergeWith(currentConsistentChildrenMetrics);
		return m;
	}

}

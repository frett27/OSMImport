package com.poc.osm.regulation;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;

/**
 * Actor that compute a pid regulation for the streams
 *
 * @author pfreydiere
 */
public class FlowRegulator extends UntypedActor {

	private static final String TICK2 = "tick";

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	/**
	 * actors that provide some measures
	 */
	private ActorRef[] actors;

	/**
	 * current number of elements
	 */

	private long consigne;

	private Meter elements;

	private double vel;
	private double Kp = 0.02;
	private double Ki = 0.000001;
	private double Kd = 0.0005;

	public FlowRegulator(String counterName, long consigne) {

		this.consigne = consigne;

		elements = Metrics.newMeter(FlowRegulator.class, counterName,
				"Pipeline", TimeUnit.SECONDS);

	}

	@Override
	public void preStart() throws Exception {
		super.preStart();

	}

	long previousError = 0;
	long previousTime = System.nanoTime();
	double integral = 0;

	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof MessageRegulation) {
			
			MessageRegulation mr = (MessageRegulation) message;
			elements.mark(mr.getCounter());

		} else if (message instanceof MessageRegulatorStatus) {

			long s = System.nanoTime();
			double dt = (s - previousTime) / 1000000.0;
			long c = elements.count();
			log.info("current elements count :" + c);

			long error = consigne - c;
			integral += error * dt;
			double derivative = (error - previousError) / dt;
			vel = Kp * error + Ki * integral + Kd * derivative;

			log.info("new computed velocity :" + vel);

			previousError = error;
			previousTime = s;

			MessageRegulatorStatus messageRegulatorStatus = new MessageRegulatorStatus();
			messageRegulatorStatus.setNewVelocity(vel);

			getSender().tell(messageRegulatorStatus, getSelf());

		} else {
			unhandled(message);
		}

	}
}

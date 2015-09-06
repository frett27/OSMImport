package com.osmimport.regulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.osmimport.actors.ControlMessageMetrics;
import com.osmimport.actors.MeasuredActor;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;

/**
 * Actor that compute regulation for the streams
 * 
 * @author pfreydiere
 */
public class FlowRegulator extends MeasuredActor {

	private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	/**
	 * current number of elements
	 */
	private long consigne;

	private double vel;
	private double Kp = 0.000004;
	private double Ki = 0.0000004;
	private double Kd = 0.000005;

	public FlowRegulator(String counterName, long consigne) {
		this.consigne = consigne;
	}

	@Override
	public void preStart() throws Exception {
		super.preStart();

	}

	double previousError = 0;
	long previousTime = System.nanoTime();
	double integral = 0;

	private int cpt = 0;

	private ArrayList<ActorRef> registeredActors = new ArrayList<ActorRef>();

	@Override
	protected List<ActorRef> metricsChildrens() {
		List<ActorRef> l = super.metricsChildrens();
		l.addAll(registeredActors);
		return l;
	}

	@Override
	public void onReceiveMeasured(Object message) throws Exception {

		if (message instanceof MessageRegulatorRegister) {

			registeredActors.add(((MessageRegulatorRegister) message)
					.getActor());

		} else if (message instanceof MessageRegulatorStatus) {

			long s = System.nanoTime();
			double dt = (s - previousTime) / 1000000.0; // per second

			long c = computeMessageNumber();

			double error = 1.0 * consigne - c;

			if (error < -consigne) {
				// fast fall back
				vel = 0;

			} else {

				integral += error * dt;
				double derivative = (error - previousError) / dt;
				vel = Kp * error + Ki * integral + Kd * derivative;

				if (vel > 1000)
					vel = 1000;

				if (vel < 0)
					vel = 0;

			}

			if (cpt++ % 100 == 0) {
				log.info("current read velocity :" + vel + "(" + c
						+ " elements)");
				// consolidateAllMessages().dump();
			}

			previousError = error;
			previousTime = s;

			MessageRegulatorStatus messageRegulatorStatus = new MessageRegulatorStatus();
			messageRegulatorStatus.setNewVelocity(vel);

			tell(getSender(), messageRegulatorStatus, getSelf());

		} else {
			unhandled(message);
		}

	}
}

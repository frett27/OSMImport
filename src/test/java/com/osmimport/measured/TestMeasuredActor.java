package com.osmimport.measured;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.actor.Props;
import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import com.osmimport.actors.AskMetrics;
import com.osmimport.actors.ControlMessageMetrics;
import com.osmimport.actors.MessageMetrics;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TestMeasuredActor extends TestCase {

	public void test() throws Exception {

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
	
		Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.setLevel(Level.DEBUG);
		
		BasicConfigurator.configure(lc);

	
		Config config = ConfigFactory.load();

		ActorSystem sys = ActorSystem.create("osmcluster",
				config.getConfig("osmcluster"));
		
		
		ActorRef cont = sys.actorOf(Props.create(TestMContainer.class), "a1");

		Inbox inbox = Inbox.create(sys);

		for (int i = 0; i < 1000; i++) {
			cont.tell("monmessage", inbox.getRef());
			Thread.sleep(2000);
			cont.tell(new AskMetrics(i), inbox.getRef());
			Object message = inbox.receive(Duration
					.create(10, TimeUnit.MINUTES));
			if (message instanceof ControlMessageMetrics) {
				ControlMessageMetrics mm = (ControlMessageMetrics) message;
				MessageMetrics messageMetrics = mm.getMessageMetrics();
				messageMetrics.dump();
				System.out.println("total :"
						+ messageMetrics.computeMailBoxMessageCount() + " in transaction :" + mm.getCorrelated());
				messageMetrics.dump();
			}
		}

	}

}
